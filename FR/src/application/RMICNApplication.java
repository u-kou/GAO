package application;

import java.util.Map;

import ndn.message.NDNMessage;
import environment.ParamConfiguration;
import ndn.application.NDNApplication;
//import ndn.message.Data;
import net.named_data.jndn.Data;
//import ndn.message.Interest;
import net.named_data.jndn.Interest;
import ndn.nfd.datastructure.CSEntry;
import ndn.nfd.datastructure.FIBEntry;
import ndn.nfd.datastructure.StrategyChoiceTableEntry;
//import ndn.nfd.face.Face;
import net.named_data.jndn.Face;
import ndn.nfd.forwarder.Forwarding;
import node.Gateway;

/**
 * 評価用アプリケーション
 * GW の上で動かすから少しトリッキー
 * @author taku
 *
 */
public class RMICNApplication extends NDNApplication {

	// アプリだけどForwardingPipeline使う（GWで動かすため）
	private Forwarding forwarding;

	public RMICNApplication(Gateway gw, String interestFilterName, String dataFilterName) {
		super(gw, interestFilterName, dataFilterName);
		this.forwarding = this.getNode().getNFD().getForwarding();
		Map<Integer, Face> faces = this.forwarding.getFaces();
		for (Face face: faces.values()) {
			this.setInterestFilter(face);
			this.setDataFilter(face);
		}
	}

	@Override
	public void onInterest(Face receiver, Interest interest) {
		Data data = new Data(interest.getNDNName());
		data.setRequestedTime(interest.getRequestedTime());
		receiver.send(new NDNMessage(data.getNDNName(), data.getType()));
	}

	@Override
	public void onData(Face receiver, Data data) {
		if (forwarding.pitFind(data.getNDNName()) != null) {
			long receivingTime = System.currentTimeMillis();
			long sendingTime = data.getRequestedTime();
			int contentRetrievalTime = (int) (receivingTime - sendingTime);
			int n = getNode().getContentNum();
			int t = getNode().getContentRetrivalTimeAvg();
			int newContentNum = n + 1;
			int newContentRetrievalTimeAvg = (int) ((t * n + contentRetrievalTime) / (n + 1.0));
			getNode().setContentNum(newContentNum);
			getNode().setContentRetrivalTimeAvg(newContentRetrievalTimeAvg);
//			getNode().getEnvironment().onContentInfoUpdated();
			forwarding.satisfyInterest(data);
		}

		if (data.isCacheFlag())
			forwarding.csInsert(data);
	}

	public boolean sendInterest(String name) {
		Interest interest = new Interest(name, !ParamConfiguration.DTN);
		interest.setRequestedTime(System.currentTimeMillis());

		// operation for CS
		CSEntry csEntry = forwarding.csLookup(interest.getNDNName());
		if (interest.isCacheFlag() && csEntry != null) {
			// Log
			forwarding.getNFD().log("Forwarding", "Cache Hit", interest.getNDNName());
			getNode().setInterestNum(getNode().getInterestNum() + 1);
			int n = getNode().getContentNum();
			int t = getNode().getContentRetrivalTimeAvg();
			getNode().setContentNum(n + 1);
			getNode().setContentRetrivalTimeAvg((int)(t * n / (n + 1.0)));
//			getNode().getEnvironment().onContentInfoUpdated();
			return true;
		}

		StrategyChoiceTableEntry sctEntry = forwarding.dispatchToStrategy(interest);
		FIBEntry fibEntry = (FIBEntry) forwarding.getFIB().findByLM(interest.getNDNName());
		if (fibEntry == null) {
			return false;
		}
		if (sctEntry != null) {
			sctEntry.getStrategy().afterReceiveInterest(null, interest, null);
			getNode().setInterestNum(getNode().getInterestNum() + 1);
//			getNode().getEnvironment().onContentInfoUpdated();
			return true;
		} else {
			forwarding.getNFD().log("Forwarding", "No found strategy for the Interest",
					interest.getNDNName());
			return false;
		}
	}

	public Gateway getNode() {
		return (Gateway) node;
	}
}
