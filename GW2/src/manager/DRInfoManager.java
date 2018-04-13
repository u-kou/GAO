package manager;

import java.util.Map.Entry;

import datastructure.FaceTableEntry;
import datastructure.NodeTableEntry;
import info.Info;
import ndn.layer2.Interface;
//import ndn.message.Interest;
import net.named_data.jndn.Interest;
//import ndn.nfd.face.Face;
import net.named_data.jndn.Face;
import node.FlyingRouter;
import node.Location;
import node.RMICNNode;
import forwarder.DepotRouterNFD;

/**
 * DRInfo Manager
 * @author taku
 *
 */
public class DRInfoManager extends FRInfoManager {
	
	public DRInfoManager(DepotRouterNFD nfd) {
		super(nfd);
	}
	
	@Override
	public void onL2Connected(Interface iFace) {
		// 何もしない
	}
	
	@Override
	public void onFaceConnected(Face face) {
		FaceTableEntry fte = ft.getTable().get(face.getFaceID());
		// 初期化のタイミングで fte が null になってる場合がある
		if (fte == null)
			return;
		String nodeName = ft.getTable().get(face.getFaceID()).getNodeName();
		NodeTableEntry nte = nt.getTable().get(nodeName);
		if (nte != null && nte.getNodeType() == RMICNNode.TYPE_FR) {
			// 相手が FR なら retrieveInfo する
			/*
			String retrieveInfoName = "/Neighbor/" + nte.getNodeName() + "/RetrieveInfo";
			Interest interest = new Interest(retrieveInfoName, false);
			this.getNFD().getForwarding().sendInterest(interest, face);
			*/
		}
	}
	
	@Override
	public String registerFR(Face face, String frName) {
		// DR は FR の登録を受け付ける
		updateFaceTable(face, frName, true);
		FlyingRouter dr = this.getNFD().getNode();
		NodeTableEntry nte = nt.getTable().get(dr.getName());
		// 初期化のタイミングで Node Table の DR のエントリが null になってることがある
		if (nte == null)
			return "NG";
		// コンマ区切りで「ノードのタイプ」「ノードの名前」「ノードの位置」を送信
		Location loc = nte.getLocation();
		return "DR," + dr.getName() + "," + loc.x + "," + loc.y;
	}
	
	@Override
	public void updateInfo(Face face, Info info) {
		DRPathManager pathManager = (DRPathManager) this.getNFD().getPathManager();
		// Initial Phase の場合はアップデートしない
		if (pathManager.getPhase() == PathManager.INITIAL_PHASE)
			return;
		super.updateInfo(face, info);
		System.out.println("=====================");
		for (Entry<String, NodeTableEntry> e: this.nt.getTable().entrySet()) {
			if (e.getValue().getNodeType() == RMICNNode.TYPE_FR) {
				System.out.print(e.getValue().getNodeName() + ": ");
				if (e.getValue().getLocation() != null) {
					System.out.println("Depot");
				} else {
					System.out.println("Not Depot");
				}
			}
		}
		System.out.println("=====================");
		
		if (pathManager.isAllFRsAtDepot()) {
			// 初期状態でなく全てのFRから情報を取得した時
			System.out.println("全ての情報げっと");
			onDRReady();
		}
	}
	
	/**
	 * 全てのFRから情報を取得したときに呼ばれる
	 */
	public void onDRReady() {
		DRPathManager pathManager = (DRPathManager) this.getNFD().pathManager;
		pathManager.setPhase(PathManager.INITIAL_PHASE);
		// 管理している全てのFRに onDRReady を通知
		for (Entry<Integer, FaceTableEntry> e: ft.getTable().entrySet()) {
			Face face = this.getNFD().getFaceManager().getFace(e.getKey());
			String onDRReadyName = "/Neighbor/" + face.getFaceID() + "/onDRReady";
			Interest interest = new Interest(onDRReadyName, false);
			this.getNFD().getForwarding().sendInterest(interest, face);
		}
		pathManager.onDRReady();
	}

	@Override
	public String retrieveRoutes(Face face) {
		// retrieveRoutesには空文字列を返送
		return "";
	}
	
	public DepotRouterNFD getNFD() {
		return (DepotRouterNFD) nfd;
	}
	
}
