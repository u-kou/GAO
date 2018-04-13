package manager;

import java.util.List;

import application.RMICNApplication;
//import ndn.nfd.face.Face;
import net.named_data.jndn.Face;
import node.Gateway;
import forwarder.GatewayNFD;

/**
 * GWInfo Manager
 * @author taku
 *
 */
public class GWInfoManager extends InfoManager {

	public GWInfoManager(GatewayNFD nfd) {
		super(nfd);
	}
	
	@Override
	public String registerFR(Face face, String frName) {
		// GW は FR の登録を受け付ける
		updateFaceTable(face, frName, true);
		List<RMICNApplication> applications = this.getNFD().getNode().getApplications();
		
		// FilterTable にアプリを登録
		for (RMICNApplication application: applications) {
			application.setInterestFilter(face);
			application.setDataFilter(face);
		}
		System.out.println("registerFR");
		Gateway gw = this.getNFD().getNode();
		// コンマ区切りで「ノードのタイプ」「ノードの名前」「ノードの位置」を送信
		String ret = "GW," + gw.getName() + "," 
				+ gw.getNowLocation().x + "," + gw.getNowLocation().y;
		return ret;
	}
	
	@Override
	public GatewayNFD getNFD() {
		return (GatewayNFD) super.getNFD();
	}

}
