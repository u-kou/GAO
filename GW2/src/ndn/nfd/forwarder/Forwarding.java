package ndn.nfd.forwarder;

import java.util.List;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

//import ndn.message.Data;
import net.named_data.jndn.Data;
//import ndn.message.Interest;
import net.named_data.jndn.Interest;
import ndn.message.NDNMessage;
import ndn.nfd.datastructure.CS;
import ndn.nfd.datastructure.CSEntry;
import ndn.nfd.datastructure.FIB;
import ndn.nfd.datastructure.PIT;
import ndn.nfd.datastructure.PITEntry;
import ndn.nfd.datastructure.StrategyChoiceTable;
import ndn.nfd.datastructure.StrategyChoiceTableEntry;
//import ndn.nfd.face.Face;
import net.named_data.jndn.Face;
/**
 * Forwarding Pipeline
 * @author taku
 *
 */
public class Forwarding {

	private NFD nfd;
	private FIB fib;
	private PIT pit;
	private CS cs;
	private StrategyChoiceTable sct;

	// Face ID to Face
	private Map<Integer, Face> faces;

	public Forwarding(NFD nfd) {
		this.nfd = nfd;
		this.fib = new FIB();
		this.pit = new PIT();
		this.cs = new CS();
		this.sct = new StrategyChoiceTable();
		this.faces = new ConcurrentHashMap<Integer, Face>();
	}

	public void onReceiveInterest(Face receiver, Interest interest) {
		// operation for PIT
		PITEntry pitEntry = pitFind(interest.getNDNName());
		if (pitEntry == null) {
			pitEntry = new PITEntry(interest.getNDNName());
			pit.insert(pitEntry);
		}
		pitEntry.addInFace(receiver);

		// operation for CS
		CSEntry csEntry = csLookup(interest.getNDNName());
		if (interest.isCacheFlag() && csEntry != null) {
			// Log
			nfd.log("Forwarding", "Cache Hit", interest.getNDNName());
			// 要求時間更新して返送
			Data data = csEntry.getData();
			data.setRequestedTime(interest.getRequestedTime());
			List<Face> downstreams = pitEntry.getInRecord();
			sendData(data, downstreams);
			return;
		}

		// dispatchToStrategy
		StrategyChoiceTableEntry sctEntry = dispatchToStrategy(interest);
		if (sctEntry != null) {
			sctEntry.getStrategy().afterReceiveInterest(receiver, interest, pitEntry);
		} else {
			nfd.log("Forwarding", "No found strategy for the Interest",
					interest.getNDNName());
		}

	}

	public void onReceiveData(Face receiver, Data data) {
		// operation for PIT
		PITEntry pitEntry = pitFind(data.getNDNName());
		
		if (pitEntry == null || pitEntry.getOutRecord().isEmpty()) {
			// unsolicited data
			this.nfd.log("Forwarding", "unsolicited data",
			data.getNDNName());
			return;
		} 
		
		// operation for CS
		csInsert(data);

		// dispatchToStrategy
		StrategyChoiceTableEntry sctEntry = dispatchToStrategy(data);
		if (sctEntry != null) {
			sctEntry.getStrategy().beforeSatisfyInterest(pitEntry, receiver, data);
		} else {
			this.nfd.log("Forwarding", "No found strategy for the Data",
					data.getNDNName());
		}

		if (!pitEntry.getInRecord().isEmpty()) {
			// sendData
			sendData(data, pitEntry.getInRecord());
		}
	}

	/**
	 * Send Interest in pipeline
	 * @param interest
	 * @param upstreams
	 */
	public void sendInterest(Interest interest, List<Face> upstreams) {
		for (Face upstream: upstreams) {
			sendInterest(interest, upstream);
		}
	}
	
	/**
	 * Send Interest in pipeline
	 * @param interest
	 * @param upstream
	 */
	public void sendInterest(Interest interest, Face upstream) {
		interest.setName(interest.getNDNName() + "/" + System.currentTimeMillis());
		
		PITEntry pitEntry = this.pitFind(interest.getNDNName());
		if (pitEntry == null) {
			pitEntry = new PITEntry(interest.getNDNName());
			pit.insert(pitEntry);
		}
		pitEntry.addOutFace(upstream);
		upstream.send(new NDNMessage(interest.getNDNName(), interest.getType()));
	}
	
	/**
	 * Send Data
	 * @param data
	 * @param downstreams
	 */
	public void sendData(Data data, List<Face> downstreams) {
		// Satisfy Interest
		satisfyInterest(data);
		for (Face downstream: downstreams)
			downstream.send(new NDNMessage(data.getNDNName(), data.getType()));
	}
	
	/**
	 * Send Data
	 * @param data
	 * @param downstream
	 */
	public void sendData(Data data, Face downstream) {
		// Satisfy Interest
		satisfyInterest(data);
		downstream.send(new NDNMessage(data.getNDNName(), data.getType(), data.getNDNContent()));
	}

	public void satisfyInterest(Data data) {
		pit.delete(data.getNDNName());
	}

	public PITEntry pitFind(String name) {
		return (PITEntry) pit.find(name);
	}

	public void csInsert(Data data) {
		if (!data.isCacheFlag())
			return;
		CSEntry csEntry = new CSEntry(data.getNDNName(), data);
		cs.insert(csEntry);
	}

	public CSEntry csLookup(String name) {
		return (CSEntry) cs.find(name);
	}

	public StrategyChoiceTableEntry dispatchToStrategy(Interest message) {
		return (StrategyChoiceTableEntry) sct.findByLM(message.getNDNName());
	}
	
	public StrategyChoiceTableEntry dispatchToStrategy(Data message) {
		return (StrategyChoiceTableEntry) sct.findByLM(message.getNDNName());
	}

	public FIB getFIB() {
		return fib;
	}

	public void setFIB(FIB fib) {
		this.fib = fib;
	}

	public PIT getPIT() {
		return pit;
	}

	public void setPIT(PIT pit) {
		this.pit = pit;
	}

	public CS getCS() {
		return cs;
	}

	public void setCS(CS cs) {
		this.cs = cs;
	}

	public StrategyChoiceTable getSCT() {
		return sct;
	}

	public void setSCT(StrategyChoiceTable sct) {
		this.sct = sct;
	}

	public NFD getNFD() {
		return nfd;
	}

	public void setNFD(NFD forwarder) {
		this.nfd = forwarder;
	}

	public Map<Integer, Face> getFaces() {
		return faces;
	}

	public void setFaces(Map<Integer, Face> faces) {
		this.faces = faces;
	}

}
