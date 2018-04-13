package ndn.message;

/**
 * Message のタイプ
 * @author taku
 *
 */
public final class MessageType {
	public static final int INTEREST = 0;
	public static final int DATA = 1;
	
	public static String toStr(int type) {
		switch(type) {
		case INTEREST:
			return "Interest";
		case DATA:
			return "Data";
		}
		return "";
	}
	
}
