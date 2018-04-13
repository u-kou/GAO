package ndn.nfd.manager;

import java.util.Map;

//import ndn.nfd.face.Face;
import net.named_data.jndn.Face;
import ndn.nfd.forwarder.NFD;
import ndn.node.Node;

/**
 * Face Manager
 * @author taku
 *
 */
public class FaceManager extends Manager {

	private Map<Integer, Face> faces;

	public FaceManager(NFD nfd) {
		super(nfd);
		this.name = "FaceManager";
		this.faces = nfd.getForwarding().getFaces();
	}

	/**
	 * Face を検索する、なければ作る
	 * @param remoteURI
	 * @return
	 */
	public Face findOrCreateFace(String remoteURI) {
		// 既に存在してたらそれを返す
		Face face;
		if ((face = getFace(remoteURI)) != null) return face;
		face = new Face(decideFaceID(), remoteURI, nfd.getForwarding());
		faces.put(face.getFaceID(), face);
		// Log
		nfd.log(this.name, "Create Face", "faceID: " + face.getFaceID()
				+ " local: " + face.getInterface().getInterfaceURI() 
				+ " remote: " + face.getRemoteURI());
		return face;
	}
	
	/**
	 * Face を検索する、なければ作る
	 * @param node
	 * @return
	 */
	public Face findOrCreateFace(Node node) {
		return findOrCreateFace(node.getInterface().getInterfaceURI());
	}

	public void destroyFace(int faceID) {
		Face face = faces.get(faceID);
		faces.remove(faceID);
		// Log
		nfd.log(this.name, "Destroy Face", "faceID: " + + face.getFaceID()
				+ " local: " + face.getInterface().getInterfaceURI()
				+ " remote: " + face.getRemoteURI());
	}

	private int decideFaceID() {
		int max = -1;
		for (Integer faceID: faces.keySet()) {
			if (max < faceID)
				max = faceID;
		}
		return max + 1;
	}

	/**
	 * Face ID から Face を取得
	 * @param faceID
	 * @return
	 */
	public Face getFace(int faceID) {
		return faces.get(faceID);
	}

	/**
	 * remoteURI から Face を取得
	 * @param remoteURI
	 * @return
	 */
	public Face getFace(String remoteURI) {
		for (Face face: faces.values()) {
			if (face.getRemoteURI().equals(remoteURI))
				return face;
		}
		return null;
	}

}
