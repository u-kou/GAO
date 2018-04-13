package datastructure;

import net.named_data.jndn.Face;
/**
 * Face Table のエントリ
 * @author taku
 *
 */
public class FaceTableEntry {

	private Face face;
	private int faceID;
	private String nodeName;
	private boolean isConnected;
	
	public FaceTableEntry(int faceID, String nodeName, boolean isConnected, Face face) {
		this.faceID = faceID;
		this.nodeName = nodeName;
		this.isConnected = isConnected;
		this.face = face;
	}
	
	public FaceTableEntry(int faceID, String nodeName, boolean isConnected) {
		this.faceID = faceID;
		this.nodeName = nodeName;
		this.isConnected = isConnected;
		this.face = new Face();
	}

	public Face getFace(){
		return face;
	}
	
	public int getFaceID() {
		return faceID;
	}

	public void setFaceID(int faceID) {
		this.faceID = faceID;
	}

	public String getNodeName() {
		return nodeName;
	}

	public void setNodeName(String nodeName) {
		this.nodeName = nodeName;
	}

	public boolean isConnected() {
		return isConnected;
	}

	public void setConnected(boolean isConnected) {
		this.isConnected = isConnected;
	}
	
}
