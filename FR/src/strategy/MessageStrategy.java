package strategy;

import java.util.List;

import forwarder.RMICNFD;
import manager.BufferManager;
import ndn.message.NDNMessage;
//import ndn.message.Data;
//import ndn.message.Interest;
import ndn.nfd.datastructure.FIBEntry;
import ndn.nfd.datastructure.PITEntry;
//import ndn.nfd.face.Face;
import net.named_data.jndn.*;
import ndn.nfd.forwarder.Forwarding;
import ndn.nfd.strategy.Strategy;

/**
 * Message Strategy
 * @author taku
 *
 */
public class MessageStrategy extends Strategy {

	public static final String prefix = "/";
	
	public MessageStrategy(Forwarding forwarding) {
		super("MessageStrategy", forwarding);
	}

	@Override
	public void afterReceiveInterest(Face inFace, Interest interest, PITEntry pitEntry) {
		super.afterReceiveInterest(inFace, interest, pitEntry);

		FIBEntry fibEntry = (FIBEntry) forwarding.getFIB().findByLM(interest.getNDNName());
		//FIBEntry fibEntry = (FIBEntry) forwarding.getFIB().findByLM("/RMICN/" + interest.getNDNName().substring(11));
		BufferManager bufferManager = ((RMICNFD) forwarding.getNFD()).getBufferManager();
		if (fibEntry != null) {
			for (Face nexthop: fibEntry.getNextHops()){
				bufferManager.store(nexthop, new NDNMessage(// /RMICN/GWXService
						interest.getNDNName(),interest.getType()));
			}
		} else {
			this.forwarding.getNFD().log(this.name, "Cannot forward Interest",
					interest.getNDNName());
		}
	}

	@Override
	public void beforeSatisfyInterest(PITEntry pitEntry, Face inFace, Data data) {
		super.beforeSatisfyInterest(pitEntry, inFace, data);
		BufferManager bufferManager = ((RMICNFD) forwarding.getNFD()).getBufferManager();
		List<Face> downstreams = pitEntry.getInRecord();
		for (Face downstream: downstreams)
			//bufferManager.store(downstream,  new NDNMessage(// /RMICN/GWXService
			//		"/RMICN" + "/" + "FR0" + "/" + data.getNDNName().substring(7), data.getType()));
			bufferManager.store(downstream,  new NDNMessage(data.getNDNName(), data.getType()) );
	}

}
