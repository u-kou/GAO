package ndn.message;

import ndn.layer2.L2Message;
import ndn.utility.NameUtility;
import net.named_data.jndn.Name;

/**
 * NDN のメッセージ
 * @author taku
 *
 */
public class NDNMessage extends L2Message {

	
	protected int type;
	protected String name;
	protected boolean cacheFlag;
	protected long requestedTime;
	protected String prefix;
	protected String content;
	
	public NDNMessage() {}
	
	public NDNMessage(String name, int type) {
		this(name, type, true);
	}
	
	public NDNMessage(String name, int type, String content) {
		this(name, type, true);
		this.content = content;
	}
	
	public NDNMessage(String name, int type, boolean cacheFlag) {
		Name t_prefix = new Name(name);
		this.name = NameUtility.filter(name);
		this.type = type;
		this.cacheFlag = cacheFlag;
		this.requestedTime = 0;
		this.prefix = t_prefix.getPrefix(1).toUri();
	}
	
	public long getRequestedTime() {
		return requestedTime;
	}

	public void setRequestedTime(long generatedTime) {
		this.requestedTime = generatedTime;
	}

	public String getName() {
		return name;
	}
	
	public void setName(String name) {
		this.name = NameUtility.filter(name);
	}
	
	public int getType() {
		return type;
	}
	
	public void setType(int type) {
		this.type = type;
	}

	public boolean isCacheFlag() {
		return cacheFlag;
	}

	public void setCacheFlag(boolean cacheFlag) {
		this.cacheFlag = cacheFlag;
	}
	
	public String getPrefix(){
		return prefix;
	}
	
	public void setContent(String content){
		this.content = content;
	}
	
	public String getContent(){
		return content;
	}
	
}
