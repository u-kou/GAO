package ndn.nfd.manager;

import java.util.List;

import ndn.nfd.datastructure.FIB;
import ndn.nfd.datastructure.FIBEntry;

import net.named_data.jndn.Face;
//import ndn.nfd.face.Face;
import ndn.nfd.forwarder.NFD;

/**
 * FIB Manager
 * @author taku
 *
 */
public class FIBManager extends Manager {

	private FIB fib;

	public FIBManager(NFD nfd) {
		super(nfd);
		this.name = "FIBManager";
		this.fib = nfd.getForwarding().getFIB();
	}

	// RIB を使用しないため直接 FIB に書き込む
	public FIBEntry insert(String name, Face nexthop) {
		FIBEntry fibEntry = new FIBEntry(name, nexthop);
		fib.insert(fibEntry);

		// LOG
		nfd.log(this.name, "Insert", "name: " + fibEntry.getName() + " faceID: " + nexthop.getFaceID()
				+ " local: " + nexthop.getInterface().getInterfaceURI()
				+ " remote: " + nexthop.getRemoteURI());
		return fibEntry;
	}

	public void delete(String name) {
		FIBEntry fibEntry = (FIBEntry) fib.getEntry(name);
		List<Face> nexthops = fibEntry.getNextHops();
		String nexthopsStr = "";
		for (Face nexthop: nexthops)
			nexthopsStr += nexthop.getFaceID() + ", ";
		fib.delete(name);

		// LOG
		nfd.log(this.name, "Delete", "name: " 
				+ fibEntry.getName() 
				+ " faceID: " + nexthopsStr);

	}

	// RIB を使用しないため実装しない
	public void update() {}

}