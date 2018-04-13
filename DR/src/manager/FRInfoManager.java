package manager;

import info.Info;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;

import datastructure.FaceTableEntry;
import datastructure.NodeTable;
import datastructure.NodeTableEntry;
import datastructure.PathTable;
import ndn.layer2.Interface;
//import ndn.message.Interest;
import net.named_data.jndn.Interest;
//import ndn.nfd.face.Face;
import net.named_data.jndn.Face;
import node.Location;
import node.RMICNNode;
import forwarder.FlyingRouterNFD;

/**
 * FRInfo Manager
 * @author taku
 *
 */
public class FRInfoManager extends InfoManager {

	protected PathTable pt;
	protected NodeTable nt;
	
	public FRInfoManager(FlyingRouterNFD nfd) {
		super(nfd);
		this.pt = new PathTable();
		this.nt = new NodeTable();
	}
	
	@Override
	public void onL2Connected(Interface iFace) {
		// Discovery Phase の場合，ドローンを一時停止させる
		if (this.getNFD().pathManager.getPhase() == PathManager.DISCOVERY_PHASE) {
			this.getNFD().movementManager.stop();
		}
		// Face を作って RegisterFR する
		/*
		Face face = this.getNFD().getFaceManager().findOrCreateFace(iFace.getInterfaceURI());
		String registerFRName = "/Neighbor/" + face.getFaceID() + "/RegisterFR/" + this.getNFD().getNode().getName();
		Interest interest = new Interest(registerFRName, false);
		this.getNFD().getForwarding().sendInterest(interest, face);
		*/
	}
	
	@Override
	public void onFaceConnected(Face face) {
		FaceTableEntry fte = ft.getTable().get(face.getFaceID());
		// 初期化のタイミングで fte が null になってる場合がある
		if (fte == null)
			return;
		NodeTableEntry nte = nt.getTable().get(fte.getNodeName());
		if (nte != null && nte.getNodeType() == RMICNNode.TYPE_GW) {
			// 相手が GW なら InfoManager と同様にルート交換（但し、自分の担当のGWのみ）
			// （相手が FR，DR，Node なら何もしない）
			if (isMyGateway(fte.getNodeName())) {
				super.onFaceConnected(face);
			}
		}
	}
	
	@Override
	public String registerFR(Face face, String frName) {
		// FR は FR の登録要求があると Face Table と Node Table をアップデートする
		// 但し、NGを返す
		updateFaceTable(face, frName, true);
		updateNodeTable(frName, RMICNNode.TYPE_FR, null);
		return "NG";
	}
	
	public void registerFR(Face face, int nodeType, String nodeName, Location location) {
		// FR は自身を相手ノードに登録完了すると相手の情報をテーブルに更新する
		updateFaceTable(face, nodeName, true);
		updateNodeTable(nodeName, nodeType, location);
		if (nodeType == RMICNNode.TYPE_DR) {
			this.getNFD().getFibManager().insert("/", face);
		}
		String onFaceConnectedName = "/Neighbor/" + face.getFaceID() + "/onFaceConnected";
		Interest interest = new Interest(onFaceConnectedName, false);
		this.getNFD().getForwarding().sendInterest(interest, face);
	}
	
	public void updatePathTable(String frName, List<String> gwNames) {
		pt.getTable().put(frName, gwNames);
	}
	
	public void updatePathTable(Map<String, List<String>> pts) {
		for (Entry<String, List<String>> e: pts.entrySet()) {
			updatePathTable(e.getKey(), e.getValue());
		}
	}
	
	public void updateNodeTable(String nodeName, int nodeType, Location location) {
		NodeTableEntry nte = nt.getTable().get(nodeName);
		if (nte == null) {
			nte = new NodeTableEntry(nodeName, nodeType, location);
		} else {
			nte.setNodeType(nodeType);
			nte.setLocation(location);
		}
		updateNodeTable(nte);
	}
	
	public void updateNodeTable(NodeTableEntry nte) {
		nt.getTable().put(nte.getNodeName(), nte);
	}
	
	public void updateNodeTable(List<NodeTableEntry> ntes) {
		for (NodeTableEntry nte: ntes) 
			updateNodeTable(nte);
	}
	
	public void updateInfo(Face face, Info info) {
		updateRIB(face, info.names);
		updateNodeTable(info.ntes);
	}
	
	public String retrieveInfo(Face face) {
		// DR からのみしか送られない
		Info info = new Info();
		info.names = makeRoutes(face);
		info.ntes = makeNodeTableEntries(face);
		ObjectMapper mapper = new ObjectMapper();
		String json = null;
        try {
			json = mapper.writeValueAsString(info);
		} catch (JsonProcessingException e1) {
			e1.printStackTrace();
		}
        return json;
	}
	
	@Override
	public String retrieveRoutes(Face face) {
		// 相手が担当のGWの場合のみルートを返す
		FaceTableEntry fte = ft.getTable().get(face.getFaceID());
		if (fte == null || !isMyGateway(fte.getNodeName()))
			return "";
		return super.retrieveRoutes(face);
	}
	
	protected List<NodeTableEntry> makeNodeTableEntries(Face face) {
		// 自分または全てのGWのエントリのみ通知
		String nodeName = this.getNFD().getNode().getName();
		List<NodeTableEntry> ntes = new ArrayList<NodeTableEntry>();
		for (Entry<String, NodeTableEntry> e: nt.getTable().entrySet()) {
			if (e.getKey().equals(nodeName)) 
				ntes.add(e.getValue());
			else if (e.getValue().getNodeType() == RMICNNode.TYPE_GW)
				ntes.add(e.getValue());
		}
		return ntes;
	}
	
	public void onDRReady(Face face) {
		// DR が準備できたら情報を取得
		String retrieveInfoName = "/Neighbor/" + face.getFaceID() + "/RetrieveInfo";
		Interest interest = new Interest(retrieveInfoName, false);
		this.getNFD().getForwarding().sendInterest(interest, face);
	}
	
	public PathTable getPathTable() {
		return pt;
	}

	public void setPathTable(PathTable pt) {
		this.pt = pt;
	}

	public NodeTable getNodeTable() {
		return nt;
	}

	public void setNodeTable(NodeTable nt) {
		this.nt = nt;
	}
	
	public Location getNodeLocation(String nodeName) {
		NodeTableEntry nte = nt.getTable().get(nodeName);
		if (nte == null)
			return null;
		return nte.getLocation();
	}
	
	public String getDepotRouterName() {
		for (NodeTableEntry nte: nt.getTable().values()) {
			if (nte.getNodeType() == RMICNNode.TYPE_DR)
				return nte.getNodeName();
		}
		return null;
	}
	
	@Override
	public FlyingRouterNFD getNFD() {
		return (FlyingRouterNFD) super.getNFD();
	}
	
	private boolean isMyGateway(String gwName) {
		List<String> gwNames = pt.getTable().get(this.getNFD().getNode().getName());
		if (gwNames != null) {
			for (int i=0; i<gwNames.size(); i++) {
				if (gwNames.get(i).equals(gwName))
					return true;
			}
		}
		return false;
	}
	
	/**
	 * ノード名からFace IDを取得
	 * @param nodeName
	 * @return
	 */
	public int getFaceID(String nodeName) {
		int faceID = -1;
		Map<Integer, FaceTableEntry> ftes =
				this.getNFD().getInfoManager().getFaceTable().getTable();
		for (FaceTableEntry e: ftes.values()) {
			if (e.getNodeName().equals(nodeName))
				faceID = e.getFaceID();
		}
		return faceID;
	}

}
