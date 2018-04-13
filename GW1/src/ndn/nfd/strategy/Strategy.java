package ndn.nfd.strategy;

//import ndn.message.Data;
import net.named_data.jndn.Data;
//import ndn.message.Interest;
import net.named_data.jndn.Interest;
import ndn.nfd.datastructure.PITEntry;
//import ndn.nfd.face.Face;
import net.named_data.jndn.Face;
import ndn.nfd.forwarder.Forwarding;

/**
 * Strategy のベースクラス
 * @author taku
 *
 */
public class Strategy {

	// Strategy name
	protected String name;
	protected Forwarding forwarding;

	public Strategy(Forwarding forwarding) {
		this.forwarding = forwarding;
	}

	public Strategy(String name, Forwarding forwarding) {
		this(forwarding);
		this.name = name;
	}

	public void afterReceiveInterest(Face inFace, Interest interest, PITEntry pitEntry) {
	}

	public void beforeSatisfyInterest(PITEntry pitEntry, Face inFace, Data data) {
	}

	public String getName() {
		return name;
	}

	public void setName(String name) {
		this.name = name;
	}

}
