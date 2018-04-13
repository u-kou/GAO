package manager;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

import datastructure.BufferStore;
import datastructure.NodeTableEntry;
import environment.ParamConfiguration;
import main.AutonomousPathChangeTimeMain;
import ndn.message.NDNMessage;
import node.Location;
import node.RMICNNode;
import forwarder.FlyingRouterNFD;

/**
 * Path Manager
 * @author taku
 *
 */
public class PathManager extends MyManager implements ArrivedListener {

	public static final int DISCOVERY_PHASE = 0;
	public static final int CRAWLING_PHASE = 1;
	public static final int INITIAL_PHASE = 2;
	
	protected int phase;
	protected boolean arrived;
	protected int waitTime;
	private int detourTime;
	private String nowGWName;			// 今いる GW
	private List<String> nextGWNames;	// 今から巡回する GW のリスト
	private Map<String, Boolean> visitedGWs;	// 訪問済み GW のリスト
	private int autonomousNum;
	private boolean isMoving;
	
	public PathManager(FlyingRouterNFD nfd) {
		super(nfd);
		this.waitTime = ParamConfiguration.waitTime; // sec
		this.nextGWNames = new ArrayList<String>();
		this.visitedGWs = new ConcurrentHashMap<String, Boolean>();
		this.phase = INITIAL_PHASE;
		this.autonomousNum = 0;
		this.isMoving = false;
	}

	@Override
	public FlyingRouterNFD getNFD() {
		return (FlyingRouterNFD) super.getNFD();
	}

	public void setNFD(FlyingRouterNFD nfd) {
		super.setNFD(nfd);
	}

	public int getPhase() {
		return phase;
	}

	public void setPhase(int phase) {
		this.phase = phase;
	}
	
	public void discover(List<Location> locs) {
		this.phase = PathManager.DISCOVERY_PHASE;
		// 今回は実装しない
	}
	
	public void crawl(int detourTime, List<String> gwNames) {
		if (isMoving)
			return;
		String myName = this.getNFD().getNode().getName();
		// null = not depot
		this.getNFD().getInfoManager().updateNodeTable(myName, RMICNNode.TYPE_FR, null);
		this.getNFD().getInfoManager().updatePathTable(myName, gwNames);
		isMoving = true;
		this.phase = PathManager.CRAWLING_PHASE;
		this.detourTime = detourTime;
		
		this.nextGWNames.clear();
		this.visitedGWs.clear();
		for (int i=0; i<gwNames.size(); i++) {
			nextGWNames.add(gwNames.get(i));
		}
		move();
	}
	
	private void move() {
		this.arrived = false;
		FRInfoManager infoManager = this.getNFD().getInfoManager();
		MovementManager movementManager = this.getNFD().getMovementManager();
		movementManager.setArrivedListener(this);
		if (nextGWNames.isEmpty()) {
			String myName = this.getNFD().getNode().getName();
			infoManager.updateNodeTable(myName, RMICNNode.TYPE_FR, 
					this.getNFD().getNode().getNowLocation());
			movementManager.moveToDepot();
			return;
		}
		movementManager.moveTo(nextGWNames.get(0));
	}
	
	@Override
	public void onArrived() {
		this.arrived = true;
		this.autonomousNum--;
		if (nextGWNames.isEmpty()) {
			isMoving = false;
			return;
		}
		nowGWName = nextGWNames.get(0);
		nextGWNames.remove(0);	
		visitedGWs.put(nowGWName, true);
		waitForExchange();
		if (!ParamConfiguration.DTN && autonomousNum <= 0)
			searchAlternativePath();
		move();
	}

	public void waitForExchange() {
		try {
			Thread.sleep(waitTime * 1000);
		} catch (InterruptedException e) {
			e.printStackTrace();
		}
	}

	private void searchAlternativePath() {
		long past = System.currentTimeMillis();
		float speed = this.getNFD().getMovementManager().getSpeed();
		FlyingRouterNFD nfd = this.getNFD();
		FRInfoManager infoManager = nfd.getInfoManager();
		List<String> gwNames = infoManager.getPathTable().getTable().get(nfd.getNode().getName());
		Map<String, NodeTableEntry> ntes = infoManager.getNodeTable().getTable();
		BufferStore bs = nfd.getBufferManager().getBufferStore();
		List<String> candidates = new ArrayList<String>();
		Location nowLoc = ntes.get(nowGWName).getLocation();
		Location gwLoc = null;
		
		// 候補のリストアップ（時間的に寄り道できて、バッファがあるやつ、かつ訪問済みのやつ）
		for (String gwName: gwNames) {
			gwLoc = ntes.get(gwName).getLocation();
			double moveTime = Location.calculateDistance(nowLoc, gwLoc) / speed;
			if (detourTime > 2 * (moveTime + waitTime)) {
				List<NDNMessage> messages = bs.getMessages(infoManager.getFaceID(gwName));
				if (messages != null && !messages.isEmpty() && isVisited(gwName))
					candidates.add(gwName);
			}
		}
		
		if (candidates.isEmpty()) {

			long now = System.currentTimeMillis();
			double t = now - past;
			AutonomousPathChangeTimeMain.add(t);
			return;
		} else if (candidates.size() == 1) {
			addDetour(candidates.get(0));

			long now = System.currentTimeMillis();
			double t = now - past;
			AutonomousPathChangeTimeMain.add(t);
			return;
		}
		
		// 候補が複数ある場合取捨選択（訪問済みのやつ）
		int i,n,max,temp,sel;
		n = candidates.size();	
		max = 0;
		sel = 0;
		
		// 候補が複数ある場合取捨選択（バッファが最も多いやつ）
		for (i=0; i<n; i++) {
			temp = bs.getMessages(infoManager.getFaceID(candidates.get(i))).size();
			if (max < temp) {
				max = temp;
				sel = i;
			}
		}
		
		addDetour(candidates.get(sel));
		long now = System.currentTimeMillis();
		double t = now - past;
		AutonomousPathChangeTimeMain.add(t);
	}
	
	private void addDetour(String gwName) {
		float speed = this.getNFD().getMovementManager().getSpeed();
		FlyingRouterNFD nfd = this.getNFD();
		FRInfoManager infoManager = nfd.getInfoManager();
		Map<String, NodeTableEntry> ntes = infoManager.getNodeTable().getTable();
		Location nowLoc = ntes.get(nowGWName).getLocation();
		Location gwLoc = ntes.get(gwName).getLocation();
		double moveTime = Location.calculateDistance(nowLoc, gwLoc) / speed;
		detourTime -= 2 * (moveTime + waitTime);
		
		nextGWNames.add(0, nowGWName);
		nextGWNames.add(0, gwName);
		this.autonomousNum = 2;
	}
	
	private boolean isVisited(String gwName) {
		return visitedGWs.get(gwName) != null;
	}
	
	public void calcurateVortex() {
		// 実装しない
	}

}
