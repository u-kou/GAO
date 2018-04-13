package forwarder;


import manager.DRInfoManager;
import manager.DRPathManager;
import node.FlyingRouter;



/**
 * DR 用 NFD
 * @author taku
 *
 */
public class DepotRouterNFD extends FlyingRouterNFD {

	public DepotRouterNFD(FlyingRouter node) {
		super(node, true);
		this.infoManager = new DRInfoManager(this);
		this.pathManager = new DRPathManager(this);
	}
	
	public DRInfoManager getInfoManager() {
		return (DRInfoManager) infoManager;
	}
	
	
	
	
	
}
