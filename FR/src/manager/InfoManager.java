package manager;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;

import datastructure.FaceTable;
import datastructure.FaceTableEntry;
import environment.ParamConfiguration;
import forwarder.FlyingRouterNFD;
import forwarder.GatewayNFD;
import forwarder.RMICNFD;
import info.RouteInfo;
import ndn.layer2.Interface;
//import ndn.message.Interest;
import net.named_data.jndn.Interest;
import net.named_data.jndn.Name;
import node.FlyingRouter;
import ndn.nfd.datastructure.FIBEntry;
import ndn.nfd.datastructure.TableEntry;
import ndn.nfd.face.FRCFaceListener;
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
		if(this.getNFD().getNode().getName().equals("FR0") && 
				this.getNFD().getStatusRetrieveRoutes() == RMICNFD.NOTSendInterest){
			String GWName = this.getFaceTableEntry(face).getNodeName();
			String retrieveRoutesName = "/Neighbor/" + GWName + "/RetrieveRoutes/" 
			+ this.getNFD().getNode().getName();
			Interest interest = new Interest(retrieveRoutesName, false);
			this.getNFD().getForwarding().sendInterest(interest, face);
			
			this.getNFD().setStatusRetrieveRoutes(RMICNFD.NOTReceiveData);
		}
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
    			
    			if(name.contains("/RMICN")){
    				int NFaceID = ((FlyingRouterNFD)this.nfd).getFaceTable().getTable().size();
    				
    				Face GWContentFace = new Face(NFaceID, "10.0.0.2", this.nfd.getForwarding());
    				Name GWContentName = new Name("/RMICN" + "/" + this.getNFD().getNode().getName() + "/" + 
    				name.substring(7, 10) + "Service");
//    				Name GWContentName = new Name(name);
    				
    				FRCFaceListener GWContentListener = new FRCFaceListener(GWContentFace, GWContentName);
    				//FRCFaceListener GWContentListener = new FRCFaceListener(face, GWContentName);
    				
    				try {
    					GWContentFace.registerPrefix(GWContentName, GWContentListener, GWContentListener, GWContentListener);
    					//face.registerPrefix(GWContentName, GWContentListener, GWContentListener, GWContentListener);
    					
    					while(!GWContentListener.RegisterSuccess) GWContentFace.processEvents();
    					//while(!GWContentListener.RegisterSuccess) face.processEvents();
    					
    				}
    			    catch (Exception e) {
    			        System.out.println("exception: " + e.getMessage());
    			    }
    				
    				
    				Map<Integer, Face> tmpNFDFaceMap = this.nfd.getForwarding().getFaces();
    				NFaceID = ((FlyingRouterNFD)this.nfd).getFaceTable().getTable().size();
    				FaceTableEntry FTEN_GW = new FaceTableEntry( NFaceID,name.substring(7, 10), true, GWContentFace);
    				//FaceTableEntry FTEN_GW = new FaceTableEntry( NFaceID,name.substring(7, 10), true, face);
    				this.getNFD().getFaceTable().getTable().put( this.getNFD().getFaceTable().getTable().size(), FTEN_GW);
    				tmpNFDFaceMap.put(this.getNFD().getFaceTable().getTable().size()-1, GWContentFace);
    				//tmpNFDFaceMap.put(this.getNFD().getFaceTable().getTable().size()-1, face);
    				
    			}
    			
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
