package manager;

import java.io.File;
import java.io.FileWriter;
import java.io.IOException;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;
import java.util.Scanner;
import java.util.concurrent.ConcurrentHashMap;

import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.ObjectMapper;

import datastructure.NodeTableEntry;
import forwarder.DepotRouterNFD;
import forwarder.RMICNFD;
//import ndn.message.Interest;
import net.named_data.jndn.Interest;
import net.named_data.jndn.Name;

//import ndn.nfd.face.Face;
import net.named_data.jndn.Face;
import node.Location;
import node.RMICNNode;
import pathplanning.IACO;
import pathplanning.Path;
import pathplanning.VRP;

/**
 * DRPath Manager
 * @author taku
 *
 */
public class DRPathManager extends PathManager {

	private int worstCrawlingTime;
	private int autonomousMovingTime;
	private Map<String, Integer> crawlingTimes;

	public DRPathManager(DepotRouterNFD nfd) {
		super(nfd);
		this.crawlingTimes = new ConcurrentHashMap<String, Integer>();
		this.autonomousMovingTime = 0;
	}

	public void discover() {
		// 実装しない
		this.phase = PathManager.DISCOVERY_PHASE;
	}

	public void deliver() {
		calculateNetwork();
		crawl();
	}

	public void crawl() {
		DepotRouterNFD nfd = this.getNFD();
		DRInfoManager infoManager = nfd.getInfoManager();
		Map<String, List<String>> pt = infoManager.getPathTable().getTable();
		for (Entry<String, List<String>> e: pt.entrySet()) {
			String frName = e.getKey();
			List<String> gwNames = pt.get(frName);
			int detourTime = worstCrawlingTime
					- crawlingTimes.get(frName)  + autonomousMovingTime;

			String crawlName = "/FRControl/" + frName + "/Crawl/" + detourTime + "/";
			for (int i=0; i<gwNames.size(); i++) {
				crawlName += gwNames.get(i);
				if (i != gwNames.size() - 1)
					crawlName += "/";
			}

			this.getNFD().log("DRPathManager", "crawl", crawlName);

			Interest interest = new Interest(new Name(crawlName), false);
			int faceID = infoManager.getFaceID(frName);
			Face face = nfd.getFaceManager().getFace(faceID);
		
			
			try{
				while(nfd.getStatusRetrieveInfo() != RMICNFD.ReceivedData) Thread.sleep(200);
				nfd.getForwarding().sendInterest(interest, face);
			}
			catch(InterruptedException er){ System.out.println("Error in sending RetrieveInfo");}
			
			
			// FR の位置を Not Depot にセット
			nfd.getInfoManager().updateNodeTable(frName, RMICNNode.TYPE_FR, null);
		}
		// 最後にフェーズを設定
		this.phase = PathManager.CRAWLING_PHASE;
	}

	public void calculateNetwork() {
		this.getNFD().log("DRPathManager", "calculateNetwork", "");
		Map<String, NodeTableEntry> ntes = this.getNFD().getInfoManager().getNodeTable().getTable();

		VRP vrp = new VRP(ntes, this.getNFD().getNode().getEnvironment());
		IACO iaco = new IACO(vrp);
		iaco.caluculate();
		Map<String, List<String>> pts = vrp.getSolution();
		Map<String, Double> distances = vrp.getDistances();

		this.getNFD().getInfoManager().updatePathTable(pts);
		float speed = this.getNFD().getMovementManager().getSpeed();
		int max = 0;
		for (Entry<String, Double> e: distances.entrySet()) {
			int crawlingTime = (int)(e.getValue()/speed) + 1 +
					+ pts.get(e.getKey()).size() * waitTime;
			crawlingTimes.put(e.getKey(), crawlingTime);
			if (max < crawlingTime)
				max = crawlingTime;
		}
		worstCrawlingTime = max;
	}

	/**
	 * ファイルから読み込む
	 * @param filename
	 */
	public void inputNetwork(String filename) {
		this.getNFD().log("DRPathManager", "calculateNetwork", "");

		Map<String,List<String>> pts = null;
		Map<String, Double> distances = null;

		try {
			File file = new File(filename + "_path.txt");
			Scanner scan = new Scanner(file);
			scan.useDelimiter(System.lineSeparator());
			StringBuilder sb = new StringBuilder();
			while (scan.hasNext()) {
				sb.append(scan.next());
			}
			scan.close();

			ObjectMapper mapper = new ObjectMapper();
			pts = mapper.readValue(sb.toString(), new TypeReference<Map<String, List<String>>>(){});

			file = new File(filename + "_dist.txt");
			scan = new Scanner(file);
			scan.useDelimiter(System.lineSeparator());
			sb = new StringBuilder();
			while (scan.hasNext()) {
				sb.append(scan.next());
			}
			scan.close();


			mapper = new ObjectMapper();
			distances = mapper.readValue(sb.toString(), new TypeReference<Map<String, Double>>(){});

		} catch (IOException e) {
			System.out.println(e);
		}

		this.getNFD().getInfoManager().updatePathTable(pts);

		this.getNFD().getNode().getEnvironment().setPaths(getAllPaths());

		float speed = this.getNFD().getMovementManager().getSpeed();
		int max = 0;
		for (Entry<String, Double> e: distances.entrySet()) {
			int crawlingTime = (int)(e.getValue()/speed) + 1 +
					+ pts.get(e.getKey()).size() * waitTime;
			crawlingTimes.put(e.getKey(), crawlingTime);
			if (max < crawlingTime)
				max = crawlingTime;
		}
		worstCrawlingTime = max;
	}

	/**
	 * ファイルに書き込む
	 * @param filename
	 */
	public void outputNetwork(String filename) {
		this.getNFD().log("DRPathManager", "calculateNetwork", "");
		Map<String, NodeTableEntry> ntes = this.getNFD().getInfoManager().getNodeTable().getTable();

		VRP vrp = new VRP(ntes, this.getNFD().getNode().getEnvironment());
		IACO iaco = new IACO(vrp);
		iaco.caluculate();
		Map<String, List<String>> pts = vrp.getSolution();
		Map<String, Double> distances = vrp.getDistances();

		try {
			File file = new File(filename + "_path.txt");
			FileWriter filewriter = new FileWriter(file);
			ObjectMapper mapper = new ObjectMapper();
	        String json = mapper.writeValueAsString(pts);
			filewriter.write(json);
			filewriter.close();

			file = new File(filename + "_dist.txt");
			filewriter = new FileWriter(file);
			mapper = new ObjectMapper();
	        json = mapper.writeValueAsString(distances);
			filewriter.write(json);
			filewriter.close();

		} catch (IOException e) {
			System.out.println(e);
		}

	}

	private List<Path> getAllPaths() {
		DRInfoManager infoManager = this.getNFD().getInfoManager();
		Map<String, List<String>> pts = infoManager.getPathTable().getTable();
		Map<String, NodeTableEntry> ntes = infoManager.getNodeTable().getTable();

		List<Path> allPaths = new ArrayList<Path>();
		String drName = infoManager.getDepotRouterName();
		Location drLoc = ntes.get(drName).getLocation();

		for (List<String> paths: pts.values()) {
			if (paths.isEmpty())
				continue;

			String firstGWName = paths.get(0);
			String lastGWName = paths.get(paths.size() - 1);
			Location firstLoc = ntes.get(firstGWName).getLocation();
			Location lastLoc = ntes.get(lastGWName).getLocation();

			Path startPath = new Path(drLoc, firstLoc);
			Path lastPath = new Path(lastLoc, drLoc);
			allPaths.add(startPath);
			allPaths.add(lastPath);

			for (int i=0; i<paths.size() - 1; i++) {
				String gwName1 = paths.get(i);
				String gwName2 = paths.get(i+1);
				Location loc1 = ntes.get(gwName1).getLocation();
				Location loc2 = ntes.get(gwName2).getLocation();
				Path path = new Path(loc1, loc2);
				allPaths.add(path);
			}

		}

		return allPaths;

	}


	public void calculateDepot() {
		// 今回は実装しない
	}

	public void onDRReady() {
		waitForExchange();
		if (!isAllFRsAtDepot())
			return;
		if (this.phase == PathManager.DISCOVERY_PHASE) {
			// 今回ここには入らない
			calculateDepot();
			// TODO: 新しい Depot を Node Table に格納
			// TODO: "/Neighbor/" + face.getFaceID() + "/onDepotUpdated を全てのFRに送る
			deliver();
		} else {
			crawl();
		}

	}

	public boolean isAllFRsAtDepot() {
		Map<String, NodeTableEntry> ntes = this.getNFD().getInfoManager().getNodeTable().getTable();
		for (Entry<String, NodeTableEntry> e: ntes.entrySet()) {
			NodeTableEntry nte = e.getValue();
			if (nte.getNodeType() != RMICNNode.TYPE_FR)
				continue;
			if (nte.getLocation() == null) {
				return false;
			}
		}
		return true;
	}

	@Override
	public void onArrived() {
		// 何もしない
	}

	public DepotRouterNFD getNFD() {
		return (DepotRouterNFD) nfd;
	}
	
	public int getworstCrawlingTime(){
		return worstCrawlingTime;
	}

}
