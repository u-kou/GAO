package manager;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;

import datastructure.FaceTableEntry;
import forwarder.RMICNFD;
import ndn.layer2.Interface;
//import ndn.nfd.face.Face;
import net.named_data.jndn.Face;

/**
 * Connection Manager
 * @author taku
 *
 */
public class ConnectionManager extends MyManager 
implements L2ConnectedListener, FaceConnectedListener {
	
	// dest interface URI - connected
	private List<L2ConnectedListener> l2ConnectedListeners;
	private List<FaceConnectedListener> faceConnectedListeners;

	public ConnectionManager(RMICNFD nfd) {
		super(nfd);
		this.l2ConnectedListeners = new ArrayList<L2ConnectedListener>();
		this.faceConnectedListeners = new ArrayList<FaceConnectedListener>();
		
	}

	
	@Override
	public void onL2Connected(Interface iFace) {
		// Face があるかどうか Face Table から検索
		Face face = null;
		Map<Integer, FaceTableEntry> ftes = 
				this.getNFD().getInfoManager().getFaceTable().getTable();
		for (Integer faceID: ftes.keySet()) {
			face = this.nfd.getFaceManager().getFace(faceID);
			if (face.getRemoteURI().equals(iFace.getInterfaceURI())) {
				break;
			}
			face = null;
		}
		if (face != null) {
			// Face がある場合
			onFaceConnected(face);
		} else {
			// Face がない場合
			for (L2ConnectedListener listener: l2ConnectedListeners)
				listener.onL2Connected(iFace);
		}
	}
	
	/**
	 * Faceが接続された時に呼び出されるイベント
	 * @param face
	 */
	public void onFaceConnected(Face face) {
		this.getNFD().getInfoManager().setConnected(face, true);
		
		try{
			face.processEvents();
		}
	    catch (Exception e) {
	        System.out.println("exception: " + e.getMessage());
	    }
		
		for (FaceConnectedListener listener: faceConnectedListeners)
			listener.onFaceConnected(face);
	}
	
	@Override
	public void onL2Disconnected(Interface iFace) {
		// Face があるかどうか検索
		Face face = this.nfd.getFaceManager().getFace(iFace.getInterfaceURI());
		if (face != null) {
			// Face がある場合
			onFaceDisconnected(face);
		} else {
			// Face がない場合
			for (L2ConnectedListener listener: l2ConnectedListeners)
				listener.onL2Disconnected(iFace);
		}
	}
	
	/**
	 * Faceが切断された時に呼び出されるイベント
	 * @param face
	 */
	public void onFaceDisconnected(Face face) {
		this.getNFD().getInfoManager().setConnected(face, false);
		for (FaceConnectedListener listener: faceConnectedListeners)
			listener.onFaceDisconnected(face);
	}
	
	public boolean isFaceConnected(Face face) {
		return this.getNFD().getInfoManager().isConnected(face);
	}
	
	public void setListeners(List<L2ConnectedListener> listeners) {
		this.l2ConnectedListeners = listeners;
	}

	public void addL2ConnectedListener(L2ConnectedListener listener) {
		l2ConnectedListeners.add(listener);
	}
	
	public void addFaceConnectedListener(FaceConnectedListener listener) {
		faceConnectedListeners.add(listener);
	}

}
