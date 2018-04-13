package forwarder;

import strategy.FRControlStrategy;
import datastructure.FaceTableEntry;
import datastructure.NodeTable;
import datastructure.PathTable;
import manager.FRInfoManager;
import manager.MovementManager;
import manager.PathManager;
import node.FlyingRouter;
import net.named_data.jndn.*;
import ndn.nfd.face.*;

/**
 * FR 用 NFD
 * @author taku
 *
 */
public class FlyingRouterNFD extends RMICNFD {
	
	// My data structures
	protected PathTable pt;
	protected NodeTable nt;
	
	// My managers
	public PathManager pathManager;
	public MovementManager movementManager;

	// 継承用コンストラクタ
	public FlyingRouterNFD(FlyingRouter node, boolean extended) {
		super(node, true);
		this.movementManager = new MovementManager(this);
		this.pt = new PathTable();
		this.nt = new NodeTable();
		// Strategy 登録
		FRControlStrategy frControlStrategy = new FRControlStrategy(this.forwarding);
		this.scManager.insert(FRControlStrategy.prefix, frControlStrategy);
		
		//Create Face for /FRControl/{NodeName}
		
	}

	public FlyingRouterNFD(FlyingRouter node) {
		this(node, false);
		this.infoManager = new FRInfoManager(this);
		this.pathManager = new PathManager(this);
	}

	public FlyingRouter getNode() {
		return (FlyingRouter) super.getNode();
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
	
	public FRInfoManager getInfoManager() {
		return (FRInfoManager) infoManager;
	}
	
	public PathManager getPathManager() {
		return pathManager;
	}

	public void setPathManager(PathManager pathManager) {
		this.pathManager = pathManager;
	}

	public MovementManager getMovementManager() {
		return movementManager;
	}

	public void setMovementManager(MovementManager movementManager) {
		this.movementManager = movementManager;
	}

}
