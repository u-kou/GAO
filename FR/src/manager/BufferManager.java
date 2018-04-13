package manager;

import java.util.ArrayList;
import java.util.List;

import datastructure.BufferStore;
import forwarder.RMICNFD;
//import ndn.message.Data;
import net.named_data.jndn.Data;
//import ndn.message.Interest;
import net.named_data.jndn.Interest;
import ndn.message.MessageType;
import ndn.message.NDNMessage;
//import ndn.nfd.face.Face;
import net.named_data.jndn.Face;

/**
 * Buffer Manager
 * @author taku
 *
 */
public class BufferManager extends MyManager implements FaceConnectedListener {

	private BufferStore bs;

	public BufferManager(RMICNFD nfd) {
		super(nfd);
		this.bs = nfd.getBufferStore();
		nfd.getConnectionManager().addFaceConnectedListener(this);
	}

	public void store(Face face, NDNMessage message) {
		if (nfd.getInfoManager().isConnected(face)) {
			// 接続されている場合 Interest のみ送る
			//(これはうそ？)（Data の場合、Forwarding Pipeline にて既に送られるため）
			//if (message.getType() == MessageType.INTEREST)
			send(face, message);
		} else {
			// 接続されていない場合
			storeBufferStore(face, message);
		}
	}

	public void send(Face face, NDNMessage message) {
		// PIT の処理を行うため，Face から直接送らずにきちんと Forwarding Pipeline を通す
		if (message.getType() == MessageType.INTEREST) {
			this.getNFD().getForwarding().sendInterest(new Interest(message), face);
		} else {
			this.getNFD().getForwarding().sendData(new Data(message), face);
		}
	}

	public void storeBufferStore(Face face, NDNMessage message) {
		List<NDNMessage> messages = bs.getStore().get(face.getFaceID());
		if (messages == null) {
			messages = new ArrayList<NDNMessage>();
			bs.getStore().put(face.getFaceID(), messages);
		}
		messages.add(message);
	}

	@Override
	public void onFaceConnected(Face face) {
		sendBufferStore(face);
	}

	@Override
	public void onFaceDisconnected(Face face) {
	}

	private void sendBufferStore(Face face) {
		List<NDNMessage> messages = bs.getStore().get(face.getFaceID());
		if (messages == null)
			return;
		int i,n;
		n = messages.size();
		for (i=0; i<n; i++) {
			send(face, messages.get(i));
			messages.remove(i);
			i--; n--;
		}
	}

	public BufferStore getBufferStore() {
		return bs;
	}

	public void setBufferStore(BufferStore bs) {
		this.bs = bs;
	}

}
