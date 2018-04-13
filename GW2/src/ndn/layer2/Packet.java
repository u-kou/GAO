package ndn.layer2;

/**
 * Layer2 で扱うパケット
 * @author taku
 *
 */
public class Packet {

	private String sourceURI;
	private String destinationURI;
	private L2Message message;

	public Packet(String senderURI, String receiverURI, L2Message message) {
		this.sourceURI = senderURI;
		this.destinationURI = receiverURI;
		this.message = message;
	}
	
	public String getSourceURI() {
		return sourceURI;
	}

	public void setSourceURI(String sourceURI) {
		this.sourceURI = sourceURI;
	}

	public String getDestinationURI() {
		return destinationURI;
	}

	public void setDestinationURI(String destinationURI) {
		this.destinationURI = destinationURI;
	}

	public L2Message getMessage() {
		return message;
	}

	public void setMessage(L2Message message) {
		this.message = message;
	}
	
}
