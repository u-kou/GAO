package manager;

import forwarder.RMICNFD;

/**
 * 独自マネージャのベースクラス
 * @author taku
 *
 */
public class MyManager {

	protected RMICNFD nfd;

	public MyManager() {
		this(null);
	}

	public MyManager(RMICNFD nfd) {
		this.nfd = nfd;
	}

	public RMICNFD getNFD() {
		return nfd;
	}

	public void setNFD(RMICNFD nfd) {
		this.nfd = nfd;
	}



}
