package strategy;

import info.Info;
import info.RouteInfo;

import java.io.IOException;

import com.fasterxml.jackson.databind.ObjectMapper;

import datastructure.FaceTableEntry;
import datastructure.NodeTableEntry;
import forwarder.FlyingRouterNFD;
import forwarder.DepotRouterNFD;
import forwarder.RMICNFD;
import manager.ConnectionManager;
import manager.FRInfoManager;
import manager.DRInfoManager;
import manager.InfoManager;
import manager.PathManager;
//import ndn.message.Data;
//import ndn.message.Interest;
import ndn.nfd.datastructure.PITEntry;
//import ndn.nfd.face.Face;
import net.named_data.jndn.*;
import ndn.nfd.forwarder.Forwarding;
import ndn.nfd.strategy.Strategy;
import ndn.utility.NameUtility;
import node.FlyingRouter;
import node.Location;
import node.RMICNNode;

/**
 * Neighbor Strategy
 * @author taku
 *
 */
public class NeighborStrategy extends Strategy {

	// /Neighbor/<faceID>/<function>/<arguments>
	
	public static final String prefix = "/Neighbor";
	
	public NeighborStrategy(Forwarding forwarding) {
		super("NeighborStrategy", forwarding);
	}

	@Override
	public void afterReceiveInterest(Face inFace, Interest interest, PITEntry pitEntry) {
		super.afterReceiveInterest(inFace, interest, pitEntry);
		String name = interest.getNDNName();
		String[] nameComponents = NameUtility.split(name);
		String content = "";
		if (nameComponents[2].equals("RegisterFR")) {
			// FR の登録要求に対する処理
			content = registerFR(inFace, interest, nameComponents[3]);			
		} else if (nameComponents[2].equals("onFaceConnected")) {
			// onFaceConnected のイベント通知要求に対する処理
			onFaceConnected(inFace, interest);
		} else if (nameComponents[2].equals("RetrieveRoutes")) {
			// ルート情報の取得要求に対する処理
			content = retrieveRoutes(inFace, interest);
		} else if (nameComponents[2].equals("RetrieveInfo")) {
			// 情報の要求に対する処理（FR、DR のみ）
			retrieveInfo(inFace, interest);
			content = "";
		} else if (nameComponents[2].equals("onDRReady")) {
			// DRの情報が利用可能になったことを広告する要求に対する処理（FR のみ）
			onDRReady(inFace, interest);
		} else if (nameComponents[2].equals("onDepotUpdated")) {
			// Depotの更新要求に対する処理（FR のみ）
			String[] s = nameComponents[3].split(",");
			Location location = 
					new Location(Float.parseFloat(s[0]), Float.parseFloat(s[1]));
			onDepotUpdated(inFace, interest, location);
		}
		
		// 必ず Data を返送する
		Data data = new Data(interest.getNDNName(), false);
		data.setContent(content);
		this.forwarding.sendData(data, inFace);
	}

	@Override
	public void beforeSatisfyInterest(PITEntry pitEntry, Face inFace, Data data) {
		super.beforeSatisfyInterest(pitEntry, inFace, data);
		String name = data.getNDNName();
		String[] nameComponents = NameUtility.split(name);
		if (nameComponents[2].equals("RegisterFR")) {
			// FRの登録結果に対する処理（FRでしか呼ばれない）
			registerFR(inFace, data);
			FlyingRouter fr = (FlyingRouter) this.forwarding.getNFD().getNode();
			if (fr.getNFD().getPathManager().getPhase() == PathManager.DISCOVERY_PHASE) {
				// Discovery Phase のとき，FR の移動を再開する
				fr.getNFD().getMovementManager().resume();
			}
		} else if (nameComponents[2].equals("onFaceConnected")) {
			// onFaceConnectedイベントの通知要求に対する結果の処理
			onFaceConnected(inFace, data);
		} else if (nameComponents[2].equals("RetrieveRoutes")) {
			// 返送されてきたルート情報に対する処理
			updateRoutes(inFace, data);
		} else if (nameComponents[2].equals("RetrieveInfo")) {
			// 返送されてきた情報に対する処理
			updateInfo(inFace, data);
		} else if (nameComponents[2].equals("onDRReady")) {
			// onDRReadyイベントを通知した際の返送データに対する処理（DR のみ）	
			onDRReady(inFace, data);
		} else if (nameComponents[2].equals("onDepotUpdated")) {
			// Depot の更新を通知した際の返送データに対する処理（DR のみ）
			onDepotUpdated(inFace, data);
		}
	}
	
	private String registerFR(Face inFace, Interest interest, String frName) {
		InfoManager infoManager = ((RMICNFD) this.forwarding.getNFD()).getInfoManager();
		return infoManager.registerFR(inFace, frName);
	}
	
	private void onFaceConnected(Face inFace, Interest interest) {
		ConnectionManager connectionManager = ((RMICNFD) this.forwarding.getNFD()).getConnectionManager();
		connectionManager.onFaceConnected(inFace);
	}
	
	private String retrieveRoutes(Face inFace, Interest interest) {
		InfoManager infoManager = ((RMICNFD) this.forwarding.getNFD()).getInfoManager();
		return infoManager.retrieveRoutes(inFace);
	}
	
	private void retrieveInfo(Face inFace, Interest interest) {
		// FR・DRで呼ばれる
		/*
		FRInfoManager frInfoManager =
				((FlyingRouterNFD) this.forwarding.getNFD()).getInfoManager();
		return frInfoManager.retrieveInfo(inFace);
		*/

			// 相手が FR なら retrieveInfo する
			
		String retrieveInfoName = "/Neighbor/FR0/RetrieveInfo";
		Interest SendInterest = new Interest(retrieveInfoName, false);
		this.forwarding.getNFD().getForwarding().sendInterest(SendInterest, inFace);
			
		
	}
	
	private void onDRReady(Face inFace, Interest interest) {
		// FRでのみ呼ばれる
		FRInfoManager frInfoManager =
				((FlyingRouterNFD) this.forwarding.getNFD()).getInfoManager();
		frInfoManager.onDRReady(inFace);
	}
	
	private void onDepotUpdated(Face inFace, Interest interest, Location location) {
		// FRでのみ呼ばれる
		FRInfoManager frInfoManager =
				((FlyingRouterNFD) this.forwarding.getNFD()).getInfoManager();
		String drName = frInfoManager.getFaceTable().getTable()
				.get(inFace.getFaceID()).getNodeName();
		frInfoManager.updateNodeTable(drName, RMICNNode.TYPE_DR, location);
	}
	
	private void registerFR(Face inFace, Data data) {
		// FR でのみ呼ばれる
		FRInfoManager frInfoManager =
				((FlyingRouterNFD) this.forwarding.getNFD()).getInfoManager();
		String content = data.getNDNContent();
		if (!content.equals("NG")) {
			String[] s = content.split(",");
			int nodeType = RMICNNode.TYPE_GW;
			if (s[0].equals("DR"))
				nodeType = RMICNNode.TYPE_DR;
			Location location = new Location(Float.parseFloat(s[2]), Float.parseFloat(s[3]));
			frInfoManager.registerFR(inFace, nodeType, s[1], location);
		}
	}
	
	private void onFaceConnected(Face inFace, Data data) {
		// 何もしない
	}
	
	private void updateRoutes(Face inFace, Data data) {
		InfoManager infoManager = ((RMICNFD) this.forwarding.getNFD()).getInfoManager();
		String content = data.getNDNContent();
		if (content.isEmpty())
			return;
		ObjectMapper mapper = new ObjectMapper();
		try {
			RouteInfo info = mapper.readValue(content, RouteInfo.class);
			infoManager.updateRIB(inFace, info.names);
		} catch (IOException e) {
			e.printStackTrace();
		}
	}
	
	private void updateInfo(Face inFace, Data data) {
		// FR・DR で呼ばれる
		FRInfoManager infoManager = 
				((FlyingRouterNFD) this.forwarding.getNFD()).getInfoManager();
		String content = data.getNDNContent();
		if (content == null)
			return;
		ObjectMapper mapper = new ObjectMapper();
		try {
			Info info = mapper.readValue(content, Info.class);
			infoManager.updateInfo(inFace, info);
		} catch (IOException e) {
			e.printStackTrace();
		}
	}
	
	private void onDRReady(Face inFace, Data data) {
		// 何もしない
	}
	
	private void onDepotUpdated(Face inFace, Data data) {
		// 何もしない
	}
	
}
