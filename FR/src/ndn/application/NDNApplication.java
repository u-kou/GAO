package ndn.application;

//import ndn.message.Data;
import net.named_data.jndn.Data;
//import ndn.message.Interest;
import net.named_data.jndn.Interest;
import ndn.nfd.face.DataFilter;
//import ndn.nfd.face.Face;
import net.named_data.jndn.Face;
//import ndn.nfd.face.InterestFilter;
import net.named_data.jndn.InterestFilter;
import ndn.node.Node;

/**
 * NDN アプリケーションクラス（アプリを作るときはこれを継承する） 
 * @author taku
 *
 */
public class NDNApplication extends InterestFilter implements DataFilter {

	protected Node node;
	protected String interestFilterName;
	protected String dataFilterName;

	public NDNApplication(Node node, String interestFilterName, String dataFilterName) {
		super();
		this.node = node;
		this.interestFilterName = interestFilterName;
		this.dataFilterName = dataFilterName;
	}

	@Override
	public void onInterest(Face receiver, Interest interest) {}

	@Override
	public void onData(Face receiver, Data data) {}

	public String getInterestFilterName() {
		return interestFilterName;
	}

	public void setInterestFilterName(String interestFilterName) {
		this.interestFilterName = interestFilterName;
	}

	public String getDataFilterName() {
		return dataFilterName;
	}

	public void setDataFilterName(String dataFilterName) {
		this.dataFilterName = dataFilterName;
	}

	public Node getNode() {
		return node;
	}

	public void setNode(Node node) {
		this.node = node;
	}

	@Override
	public void setDataFilter(Face face) {
		face.addDataFilter(dataFilterName, this);
	}

	@Override
	public void setInterestFilter(Face face) {
		face.addInterestFilter(interestFilterName, this);
	}



}
