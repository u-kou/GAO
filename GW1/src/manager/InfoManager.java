package manager;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;

import datastructure.FaceTable;
import datastructure.FaceTableEntry;
import forwarder.FlyingRouterNFD;
import forwarder.RMICNFD;
import info.RouteInfo;
import ndn.layer2.Interface;
//import ndn.message.Interest;
import net.named_data.jndn.Interest;
import node.FlyingRouter;
import ndn.nfd.datastructure.FIBEntry;
import ndn.nfd.datastructure.TableEntry;
//import ndn.nfd.face.Face;
import net.named_data.jndn.Face;

/**
 * Info Manager
 * @author taku
 *
 */
public class InfoManager extends MyManager implements L2ConnectedListener, FaceConnectedListener {

	protected FaceTable ft;

	public InfoManager(RMICNFD nfd) {
		super(nfd);
		this.ft = nfd.getFaceTable();
		nfd.getConnectionManager().addFaceConnectedListener(this);
		nfd.getConnectionManager().addL2ConnectedListener(this);
	}

	@Override
	public void onL2Connected(Interface iFace) {
		// 何も実装しない
	}

	@Override
	public void onFaceConnected(Face face) {
		// 接続したらルート情報を取得
		/*
		String retrieveRoutesName = "/Neighbor/" + face.getFaceID() + "/RetrieveRoutes";
		Interest interest = new Interest(retrieveRoutesName, false);
		this.getNFD().getForwarding().sendInterest(interest, face);
		*/
	}

	public String registerFR(Face face, String frName) {
		// FRの登録要求が来たら拒否
		return "NG";
	}

	/**
	 * RIBをアップデート
	 * @param names
	 * @param nexthop
	 */
	public void updateRIB(Face face, List<String> names) {
		// 現在は RIB を考えてないので、直接 FIB をアップデート
		updateFIB(face, names);
	}

	/**
	 * Face Table に エントリを追加
	 * @param face
	 * @param nodeName
	 * @param connected
	 */
	public void updateFaceTable(Face face, String nodeName, boolean connected) {
		updateFaceTable(new FaceTableEntry(face.getFaceID(), nodeName, connected));
	}

	/**
	 * Face Table に エントリを追加
	 * @param faceTableEntry
	 */
	public void updateFaceTable(FaceTableEntry fte) {
		ft.getTable().put(fte.getFaceID(), fte);
	}

	/**
	 * ルート情報を取得
	 * @param face
	 * @return
	 */
	public String retrieveRoutes(Face face) {

		RouteInfo routeInfo = new RouteInfo();

		// ルート作成
		routeInfo.names = makeRoutes(face);

		ObjectMapper mapper = new ObjectMapper();
		String json = null;
        try {
			json = mapper.writeValueAsString(routeInfo);
		} catch (JsonProcessingException e1) {
			e1.printStackTrace();
		}
        return json;
	}

	/**
	 * 未実装
	 * @param face
	 */
	public void retrieveName(Face face) {
	}

	/**
	 * 未実装
	 * @param face
	 */
	public void retrieveNetwork(Face face) {
	}

	/**
	 * 未実装
	 * @param face
	 */
	public void retrieveTime(Face face) {
	}


	/**
	 * ルート情報の作成
	 *
	 * @param destinationNFD
	 * @return
	 */
	protected List<String> makeRoutes(Face face) {
		// FIB と FFT から作成
		List<String> names = new ArrayList<String>();
		// FIB
		for (Entry<String, TableEntry> e: nfd.getForwarding().getFIB().getEntries().entrySet()) {

			List<Face> nexthops = ((FIBEntry) e.getValue()).getNextHops();
			boolean b = true;
			for (Face nexthop: nexthops) {
				// nexthop が相手じゃなければルート通知
				b &= nexthop.getFaceID() != face.getFaceID();
			}
			if (b) {
				names.add(e.getKey());
			}
		}

		// InterestFilterTable
		for (Map.Entry<String, TableEntry> e: face.getInterestFilterTable().getEntries().entrySet()) {
			names.add(e.getKey());
		}

		return names;

	}

	private void updateFIB(Face face, List<String> names) {
    	for (String name: names) {
    		FIBEntry fibEntry = (FIBEntry) nfd.getForwarding().getFIB().find(name);
    		if (fibEntry == null) {
    			fibEntry = new FIBEntry(name, new ArrayList<Face>());
    			nfd.getForwarding().getFIB().insert(fibEntry);
    		}
    		fibEntry.addNexthop(face);
    	}
	}

	public FaceTable getFaceTable() {
		return ft;
	}

	public void setFaceTable(FaceTable ft) {
		this.ft = ft;
	}

	public void setConnected(Face face, boolean connected) {
		FaceTableEntry fte = ft.getTable().get(face.getFaceID());
		if (fte != null)
			fte.setConnected(connected);
	}

	public boolean isConnected(Face face) {
		FaceTableEntry fte = ft.getTable().get(face.getFaceID());
		if (fte == null || !fte.isConnected())
			return false;
		return true;
	}

	public FaceTableEntry getFaceTableEntry(Face face) {
		return ft.getTable().get(face.getFaceID());
	}

	@Override
	public void onFaceDisconnected(Face face) {
		// 何も実装しない
		
		if(this.getNFD().getNode().getName() == "FR0"){

			this.nfd.setStatusRetrieveInfo(RMICNFD.NOTSendInterest);

		}
		
	}



	@Override
	public void onL2Disconnected(Interface iFace) {
		// 何も実装しない
	}

}
