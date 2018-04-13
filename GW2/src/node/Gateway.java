package node;

import environment.ParamConfiguration;
import environment.RMICNEnvironment;
import forwarder.GatewayNFD;
import net.named_data.jndn.*;
import ndn.nfd.face.FRCFaceListener;
import java.util.Map;
import ndn.layer2.*;
import datastructure.FaceTableEntry;


/**
 * Gateway
 * @author taku
 *
 */
public class Gateway extends RMICNNode {

	private int interestNum;
	private int contentNum;
	private int contentRetrivalTimeAvg;
	
	// ロケーション設定なし
	public Gateway(String name, String interfaceURI, RMICNEnvironment environment) {
		this(name, interfaceURI, environment, new Location());
	}

	// ロケーション設定あり
	public Gateway(String name, String interfaceURI, RMICNEnvironment environment
			, Location nowLocation) {
		super(name, interfaceURI, environment, nowLocation, true);
		this.nfd = new GatewayNFD(this);
		this.nodeType = TYPE_GW;
		this.interestNum = 0;
		this.contentNum = 0;
		this.contentRetrivalTimeAvg = 0;
		
		
		if(this.getName().equals("GW2")){
			int NFaceID = ((GatewayNFD)this.nfd).getFaceTable().getTable().size();
			
			Face NeighborFace = new Face(NFaceID, ParamConfiguration.FRAddress, this.nfd.getForwarding());
			Name NeighborName = new Name("/Neighbor/" + this.getName());
			FRCFaceListener NeighborListener = new FRCFaceListener(
					NeighborFace, NeighborName);
			
			Face GWContentFace = new Face(NFaceID, ParamConfiguration.FRAddress, this.nfd.getForwarding());
			Name GWContentName = new Name("/RMICN" + "/" + this.getName() + "Service");
			FRCFaceListener GWContentListener = new FRCFaceListener(
					GWContentFace, GWContentName);
			
			try {
				NeighborFace.registerPrefix(NeighborName, 
						NeighborListener, NeighborListener);
				NeighborFace.processEvents();
				
				GWContentFace.registerPrefix(GWContentName, 
						GWContentListener, GWContentListener);
				GWContentFace.processEvents();
				
			}
		    catch (Exception e) {
		        System.out.println("exception: " + e.getMessage());
		    }
			
			Map<Integer, Face> tmpNFDFaceMap = this.nfd.getForwarding().getFaces();
			
			FaceTableEntry FTEN_Neighbor = new FaceTableEntry( NFaceID,
					"FR0", true, NeighborFace);
			this.getNFD().getFaceTable().getTable().put( this.getNFD().getFaceTable().getTable().size(), FTEN_Neighbor);
			tmpNFDFaceMap.put(this.getNFD().getFaceTable().getTable().size()-1, NeighborFace);
			
			NFaceID = ((GatewayNFD)this.nfd).getFaceTable().getTable().size();
			FaceTableEntry FTEN_GW = new FaceTableEntry( NFaceID,
					"FR0", true, GWContentFace);
			this.getNFD().getFaceTable().getTable().put( this.getNFD().getFaceTable().getTable().size(), FTEN_GW);
			tmpNFDFaceMap.put(this.getNFD().getFaceTable().getTable().size()-1, GWContentFace);
		}
		
		
		this.iFace.start();
		

	}

	public GatewayNFD getNFD() {
		return (GatewayNFD) super.getNFD();
	}

	public int getContentNum() {
		return contentNum;
	}

	public void setContentNum(int contentNum) {
		this.contentNum = contentNum;
	}

	public int getContentRetrivalTimeAvg() {
		return contentRetrivalTimeAvg;
	}

	public void setContentRetrivalTimeAvg(int contentRetrivalTimeAvg) {
		this.contentRetrivalTimeAvg = contentRetrivalTimeAvg;
	}

	public int getInterestNum() {
		return interestNum;
	}

	public void setInterestNum(int interestNum) {
		this.interestNum = interestNum;
	}
	
}
