package ndn.nfd.manager;

import ndn.nfd.forwarder.NFD;

/**
 * RIB Manager
 * @author taku
 *
 */
public class RIBManager extends Manager {

	public RIBManager(NFD nfd) {
		super(nfd);
		this.name = "RIBManager";
	}
	
}
