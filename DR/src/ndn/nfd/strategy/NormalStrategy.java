package ndn.nfd.strategy;

//import ndn.message.Data;
import net.named_data.jndn.Data;
//import ndn.message.Interest;
import net.named_data.jndn.Interest;
import ndn.nfd.datastructure.FIBEntry;
import ndn.nfd.datastructure.PITEntry;
//import ndn.nfd.face.Face;
import net.named_data.jndn.Face;
import ndn.nfd.forwarder.Forwarding;

/**
 * FIB を見て Interest を転送する普通のストラテジ
 * @author taku
 *
 */
public class NormalStrategy extends Strategy {

	public static final String prefix = "/";
	
	public NormalStrategy(Forwarding forwarding) {
		super("NormalStrategy", forwarding);
	}

	@Override
	public void afterReceiveInterest(Face inFace, Interest interest, PITEntry pitEntry) {
		super.afterReceiveInterest(inFace, interest, pitEntry);
		FIBEntry fibEntry = (FIBEntry) forwarding.getFIB().findByLM(interest.getNDNName());
		if (fibEntry != null) {
			this.forwarding.sendInterest(interest, fibEntry.getNextHops());
		} else {
			this.forwarding.getNFD().log(this.name, "Cannot forward Interest",
					interest.getNDNName());
		}
	}

	@Override
	public void beforeSatisfyInterest(PITEntry pitEntry, Face inFace, Data data) {
		super.beforeSatisfyInterest(pitEntry, inFace, data);
	}

}
