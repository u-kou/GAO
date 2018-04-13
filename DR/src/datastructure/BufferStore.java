package datastructure;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

import ndn.message.NDNMessage;

/**
 * Buffer Store
 * @author taku
 *
 */
public class BufferStore {

	// Face ID -> Message List
	private Map<Integer, List<NDNMessage>> store;

	public BufferStore() {
		this.store = new ConcurrentHashMap<Integer, List<NDNMessage>>();
	}

	public Map<Integer, List<NDNMessage>> getStore() {
		return store;
	}

	public void setStore(Map<Integer, List<NDNMessage>> store) {
		this.store = store;
	}

	public void addMessage(int faceID, NDNMessage message) {
		List<NDNMessage> messages = store.get(faceID);
		if (messages == null) {
			messages = new ArrayList<NDNMessage>();
		}
		messages.add(message);
		store.put(faceID, messages);
	}
	
	public List<NDNMessage> getMessages(int faceID) {
		return store.get(faceID);
	}

}
