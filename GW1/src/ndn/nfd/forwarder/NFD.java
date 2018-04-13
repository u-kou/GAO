package ndn.nfd.forwarder;

import ndn.nfd.manager.FIBManager;
import ndn.nfd.manager.FaceManager;
import ndn.nfd.manager.RIBManager;
import ndn.nfd.manager.SCManager;
import ndn.nfd.strategy.NormalStrategy;
import ndn.node.Node;

/**
 * NFD
 * @author taku
 *
 */
public class NFD {

	// NFD を持つノード
	protected Node node;

	protected Forwarding forwarding;

	protected FaceManager faceManager;
	protected FIBManager fibManager;
	protected SCManager scManager;
	protected RIBManager ribManager;

	// 継承用コンストラクタ
	public NFD(Node node, boolean extended) {
		this.node = node;
		this.forwarding = new Forwarding(this);
		this.faceManager = new FaceManager(this);
		this.fibManager = new FIBManager(this);
		this.scManager = new SCManager(this);
		// RIB は使わない
		this.ribManager = null;
	}
	
	public NFD(Node node) {
		this(node, false);
		// Strategy登録
		NormalStrategy normalStrategy = new NormalStrategy(this.forwarding);
		this.scManager.insert(NormalStrategy.prefix, normalStrategy);
	}

	public Forwarding getForwarding() {
		return forwarding;
	}

	public void setForwarding(Forwarding forwarding) {
		this.forwarding = forwarding;
	}

	public FaceManager getFaceManager() {
		return faceManager;
	}

	public void setFaceManager(FaceManager faceManager) {
		this.faceManager = faceManager;
	}

	public FIBManager getFibManager() {
		return fibManager;
	}

	public void setFibManager(FIBManager fibManager) {
		this.fibManager = fibManager;
	}

	public SCManager getScManager() {
		return scManager;
	}

	public void setScManager(SCManager scManager) {
		this.scManager = scManager;
	}

	public RIBManager getRibManager() {
		return ribManager;
	}

	public void setRibManager(RIBManager ribManager) {
		this.ribManager = ribManager;
	}

	public Node getNode() {
		return node;
	}

	public void setNode(Node node) {
		this.node = node;
	}

	public void log(String cls, String subject, String detail) {
		/*
		if (((RMICNNode)getNode()).getNodeType() == RMICNNode.TYPE_FR
				&& subject.indexOf("tag")>=0) {
			node.print("(" + cls + ") " + subject + " > " + detail);
		}
		*/
		if (getNode().getName().equals("FR1") && detail.contains("/RMICN/"))
			node.print("(" + cls + ") " + subject + " > " + detail);
	}

}
