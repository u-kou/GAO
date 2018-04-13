package ndn.environment;

import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

import ndn.layer2.Interface;
//import ndn.nfd.face.Face;
import net.named_data.jndn.Face;
import ndn.nfd.strategy.Strategy;
import ndn.node.Node;

/**
 * NDNの環境情報を管理する
 * @author taku
 *
 */
public class Environment {

	// Node name -> Node
	protected Map<String, Node> nodes;
	// InterfaceURI -> Interface
	protected Map<String, Interface> iFaces;
	
	public Environment() {
		nodes = new ConcurrentHashMap<String, Node>();
		iFaces = new ConcurrentHashMap<String, Interface>();
	}

	public void addNode(Node node) {
		this.nodes.put(node.getName(), node);
		// インターフェイスは1つと想定
		Interface iFace = node.getInterface();
		this.iFaces.put(iFace.getInterfaceURI(), iFace);
	}

	public void deleteNode(String nodeName) {

	}

	public void deleteFace(String nodeName, int faceID) {
		Node node = getNode(nodeName);
		node.getNFD().getFaceManager().destroyFace(faceID);
	}

	public Map<String, Node> getNodes() {
		return nodes;
	}

	public Map<String, Interface> getInterfaces() {
		return iFaces;
	}

	public Node getNode(String nodeName) {
		return nodes.get(nodeName);
	}
	
	public Interface getInterface(String interfaceURI) {
		return iFaces.get(interfaceURI);
	}

	public void setStrategy(Node node, String prefix, Strategy strategy) {
		node.getNFD().getScManager().insert(prefix, strategy);
	}

	public void setStrategy(String nodeName, String prefix, Strategy strategy) {
		Node node = getNode(nodeName);
		setStrategy(node, prefix, strategy);
	}

	public void setFIBEntry(Node node, String prefix, Node nexthopNode) {
		Face nexthopFace = node.getNFD().getFaceManager().findOrCreateFace(nexthopNode);
		node.getNFD().getFibManager().insert(prefix, nexthopFace);
	}

	public void setFIBEntry(String nodeName, String prefix, String nexthopNodeName) {
		Node node = getNode(nodeName);
		Node nexthopNode = getNode(nexthopNodeName);
		setFIBEntry(node, prefix, nexthopNode);
	}

}
