package ndn.message;

/**
 * NDN における Data メッセージ
 * @author taku
 *
 */
public class Data extends NDNMessage {

	private String content;
	
	public Data(String name) {
		this(name, null);
	}
	
	public Data(String name, String content) {
		super(name, MessageType.DATA);
		this.content = content;
	}

	public Data(String name, boolean cacheFlag) {
		super(name, MessageType.DATA, cacheFlag);
	}
	
	public String getContent() {
		return content;
	}

	public void setContent(String content) {
		this.content = content;
	}
	
	
}
