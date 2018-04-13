package node;

import java.io.IOException;
import java.util.Map;

import datastructure.FaceTableEntry;
import environment.ParamConfiguration;
import environment.RMICNEnvironment;
import forwarder.DepotRouterNFD;
import forwarder.FlyingRouterNFD;
import ndn.nfd.face.FRCFaceListener;
import net.named_data.jndn.Face;
import net.named_data.jndn.Name;

/**
 * Flying Router
 * @author taku
 *
 */
public class FlyingRouter extends RMICNNode {

	// ロケーション設定なし
	public FlyingRouter(String name, String interfaceURI, RMICNEnvironment environment
			, boolean isDepotRouter) {
		this(name, interfaceURI, environment, isDepotRouter, new Location());
	}
	
	// ロケーション設定あり
	public FlyingRouter(String name, String interfaceURI, RMICNEnvironment environment
			, boolean isDepotRouter, Location nowLocation) {
		super(name, interfaceURI, environment, nowLocation, false);
		if (isDepotRouter) {
			this.nfd = new DepotRouterNFD(this);
			this.nodeType = TYPE_DR;
			
			
			DepotRouterNFD tmpNFD = (DepotRouterNFD)this.nfd;
			int NFaceID = tmpNFD.getFaceTable().getTable().size();
			Face NeighborFace = new Face(NFaceID, ParamConfiguration.FRAddress, this.nfd.getForwarding());
			FRCFaceListener NeighborListener = new FRCFaceListener(
					NeighborFace, new Name("/Neighbor/" + this.getName()));
			
			try {				
				NeighborFace.registerPrefix(new Name("/Neighbor/" + this.getName()), 
						NeighborListener, NeighborListener);
					NeighborFace.processEvents();
			}
		    catch (Exception e) {
		        System.out.println("exception: " + e.getMessage());
		     }
			
			
			Map<Integer, Face> tmpNFDFaceMap = this.nfd.getForwarding().getFaces();
			
			FaceTableEntry FTEN = new FaceTableEntry( NFaceID,
					"FR0", true, NeighborFace);
			tmpNFD.getFaceTable().getTable().put( tmpNFD.getFaceTable().getTable().size(), FTEN);
			tmpNFDFaceMap.put(tmpNFD.getFaceTable().getTable().size()-1, NeighborFace);
	
			
			
		} else {
			this.nfd = new FlyingRouterNFD(this);
			this.nodeType = TYPE_FR;		
			
			
		}
		
	
		this.iFace.start();
	}

	public FlyingRouterNFD getNFD() {
		return (FlyingRouterNFD) super.getNFD();
	}

}
