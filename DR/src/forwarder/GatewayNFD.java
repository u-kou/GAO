package forwarder;

import manager.GWInfoManager;
import node.Gateway;
import node.RMICNNode;

/**
 * Gateway 用 NFD
 * @author taku
 *
 */
public class GatewayNFD extends RMICNFD {

	private int interestNum;
	private int dataNum;

	public GatewayNFD(RMICNNode node) {
		super(node, true);
		this.infoManager = new GWInfoManager(this);
		this.interestNum = 0;
		this.dataNum = 0;
	}

	public int getInterestNum() {
		return interestNum;
	}

	public void setInterestNum(int interestNum) {
		this.interestNum = interestNum;
	}

	public int getDataNum() {
		return dataNum;
	}

	public void setDataNum(int dataNum) {
		this.dataNum = dataNum;
	}
	
	public Gateway getNode() {
		return (Gateway) node;
	}
	
	public GWInfoManager getInfoManager() {
		return (GWInfoManager) infoManager;
	}

}
