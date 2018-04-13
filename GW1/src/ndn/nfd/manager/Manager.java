package ndn.nfd.manager;

import ndn.nfd.forwarder.NFD;

/**
 * Manager のベースクラス
 * @author taku
 *
 */
public class Manager {

	protected String name;
	protected NFD nfd;
	
	public Manager() {}
	
	public Manager(NFD nfd) {
		this.name = null;
		this.nfd = nfd;
	}

	public String getName() {
		return name;
	}

	public void setName(String name) {
		this.name = name;
	}

	public NFD getNfd() {
		return nfd;
	}

	public void setNfd(NFD nfd) {
		this.nfd = nfd;
	}
	
}
