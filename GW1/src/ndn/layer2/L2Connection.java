package ndn.layer2;

/**
 * Layer2 の接続情報を管理するクラス
 * @author taku
 *
 */
public class L2Connection {
	
	public String destInterfaceURI;
	public long onConnectedTime;
	public boolean connected;
	
	public L2Connection(String destInterfaceURI, long onConnectedTime, boolean connected) {
		this.destInterfaceURI = destInterfaceURI;
		this.onConnectedTime = onConnectedTime;
		this.connected = connected;
	}
	
}
