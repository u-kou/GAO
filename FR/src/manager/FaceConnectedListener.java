package manager;

//import ndn.nfd.face.Face;
import net.named_data.jndn.Face;

/**
 * Face が接続・切断された際のリスナ
 * @author taku
 *
 */
public interface FaceConnectedListener {

	/**
	 * Face が接続された時に呼び出されるイベントリスナ
	 * @param face
	 */
	public void onFaceConnected(Face face);
	
	/**
	 * Face が切断された時に呼び出されるイベントリスナ
	 * @param face
	 */
	public void onFaceDisconnected(Face face);
	
}
