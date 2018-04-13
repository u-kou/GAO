package node;

import environment.RMICNEnvironment;
import forwarder.GatewayNFD;

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
