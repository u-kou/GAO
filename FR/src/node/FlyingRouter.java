package node;

import java.util.Map;
import environment.ParamConfiguration;
import java.io.IOException;
import ndn.layer2.*;
import datastructure.FaceTableEntry;
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
					
		} else {
			this.nfd = new FlyingRouterNFD(this);
			this.nodeType = TYPE_FR;		
			
			
			FlyingRouterNFD tmpNFD = (FlyingRouterNFD)this.nfd;
			int FRCFaceID = tmpNFD.getFaceTable().getTable().size();
			int NFaceID = FRCFaceID + 1;
			
			Face FRControlFace = new Face(FRCFaceID ,ParamConfiguration.DRAddress, this.nfd.getForwarding());
			FRCFaceListener FRControlListener = new FRCFaceListener(
					FRControlFace, new Name("/FRControl/" + this.getName()));
			
			Face NeighborFace = new Face(NFaceID, ParamConfiguration.DRAddress, this.nfd.getForwarding());
			FRCFaceListener NeighborListener = new FRCFaceListener(
					NeighborFace, new Name("/Neighbor/" + this.getName()));
			
			try {
				FRControlFace.registerPrefix(new Name("/FRControl/" + this.getName()), 
					FRControlListener, FRControlListener);
				FRControlFace.processEvents();
				
				NeighborFace.registerPrefix(new Name("/Neighbor/" + this.getName()), 
						NeighborListener, NeighborListener);
					NeighborFace.processEvents();
			}
		    catch (Exception e) {
		        System.out.println("exception: " + e.getMessage());
		     }
			
			
			Map<Integer, Face> tmpNFDFaceMap = this.nfd.getForwarding().getFaces();
			FaceTableEntry FTEFRC = new FaceTableEntry( FRCFaceID,
					"DR", true, FRControlFace);
			tmpNFD.getFaceTable().getTable().put( tmpNFD.getFaceTable().getTable().size(), FTEFRC);
			tmpNFDFaceMap.put(tmpNFD.getFaceTable().getTable().size()-1, FRControlFace);
			
			FaceTableEntry FTEN = new FaceTableEntry( NFaceID,
					"DR", true, NeighborFace);
			tmpNFD.getFaceTable().getTable().put( tmpNFD.getFaceTable().getTable().size(), FTEN);
			tmpNFDFaceMap.put(tmpNFD.getFaceTable().getTable().size()-1, NeighborFace);
			
		}
		
		this.iFace.start();
	}

	public FlyingRouterNFD getNFD() {
		return (FlyingRouterNFD) super.getNFD();
	}

}
