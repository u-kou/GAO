package node;

import java.util.ArrayList;
import java.util.List;

import application.RMICNApplication;
import environment.RMICNEnvironment;
import forwarder.RMICNFD;
import ndn.node.Node;

/**
 * RMICN Node
 * @author taku
 *
 */
public class RMICNNode extends Node {

	public static final int TYPE_NODE = 0;
	public static final int TYPE_GW = 1;
	public static final int TYPE_FR = 2;
	public static final int TYPE_DR = 3;
	
	public List<RMICNApplication> applications;
	
	// RMICNでは通常のノードに位置情報が追加される
	protected static final int DIMENSION = 2;
	protected Location nowLocation;
	
	protected int nodeType;

	// 継承用コンストラクタ
	public RMICNNode(String name, String interfaceURI, RMICNEnvironment environment
			, Location nowLocation, boolean extended) {
		super(name, interfaceURI, environment, true);
		this.nowLocation = nowLocation;
		this.applications = new ArrayList<RMICNApplication>();
	}
	
	// ロケーション設定なし
	public RMICNNode(String name, String interfaceURI, RMICNEnvironment environment) {
		this(name, interfaceURI, environment, new Location());
	}

	// ロケーション設定あり
	public RMICNNode(String name, String interfaceURI, RMICNEnvironment environment
			, Location nowLocation) {
		this(name, interfaceURI, environment, nowLocation, false);
		this.nfd = new RMICNFD(this);
		this.nodeType = TYPE_NODE;
		this.iFace.start();
	}

	public RMICNFD getNFD() {
		return (RMICNFD) super.getNFD();
	}

	public Location getNowLocation() {
		return nowLocation;
	}

	public void setNowLocation(float x, float y) {
		this.nowLocation.setLocation(x, y);
	}

	public int getNodeType() {
		return nodeType;
	}

	public void setNodeType(int nodeType) {
		this.nodeType = nodeType;
	}
	
	public RMICNEnvironment getEnvironment() {
		return (RMICNEnvironment) environment;
	}

	public List<RMICNApplication> getApplications() {
		return applications;
	}

	public void setApplications(List<RMICNApplication> applications) {
		this.applications = applications;
	}
	
}
