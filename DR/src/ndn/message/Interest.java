package ndn.message;

/**
 * NDN における Interest メッセージ
 * @author taku
 *
 */
public class Interest extends NDNMessage {
	
	public Interest(String name) {
		super(name, MessageType.INTEREST);
	}
	
	public Interest(String name, boolean cacheFlag) {
		super(name, MessageType.INTEREST, cacheFlag);
	}

}
