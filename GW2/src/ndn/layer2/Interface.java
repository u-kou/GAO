package ndn.layer2;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;
import java.util.concurrent.ConcurrentHashMap;

import environment.ParamConfiguration;
//import ndn.nfd.face.Face;
import net.named_data.jndn.Face;
import ndn.node.Node;
import node.Location;
import node.RMICNNode;
import forwarder.RMICNFD;

/**
 * Layer2 のインターフェース
 * @author taku
 *
 */
public class Interface {
	
	private String interfaceURI;
	private Node node;
	private ArrayList<String> RegisteredPrefixList;
	
	// dest interface URI - connected
	private Map<String, L2Connection> l2Connections;
	private boolean isConnectionMonitoring;
	private boolean isBufferMonitoring;
	private ConnectionMonitor connectionMonitor;
	private BufferMonitor bufferMonitor;
	
	protected List<Packet> bufferredPackets;
	
	public Interface(String interfaceURI, Node node) {
		this.interfaceURI = interfaceURI;
		this.node = node;
		this.l2Connections = new ConcurrentHashMap<String, L2Connection>();
		this.isConnectionMonitoring = false;
		this.isBufferMonitoring = false;
		this.bufferredPackets = new ArrayList<Packet>();
		this.RegisteredPrefixList = new ArrayList<String>();
	}
	
	public void start() {
		if (isRMICN()) {
			this.connectionMonitor = new ConnectionMonitor();
			this.connectionMonitor.start();
		}
		this.bufferMonitor = new BufferMonitor();
		this.bufferMonitor.start();
	}
	
	public void send(Packet packet) {
		// 接続していたら送る（RMICNでなかったらどのみち送る）
		if (isConnected(packet.getDestinationURI()) || !isRMICN()) {
			Interface destIFace = node.getEnvironment().getInterface(packet.getDestinationURI());
			destIFace.bufferPacket(packet);
		} 
	}
	
	public void bufferPacket(Packet packet) {
		this.bufferredPackets.add(packet);
	}
	
	public void receive(Packet packet) {
		// packet の source を remoteURI とする Face を取得 or 作成
		Face face = node.getNFD().getFaceManager().findOrCreateFace(packet.getSourceURI());
		face.receive(packet);
	}

	public boolean isRMICN() {
		return node instanceof RMICNNode;
	}

	private float getDistanceBetweenNodes(RMICNNode nodeA, RMICNNode nodeB) {
		return Location.calculateDistance(nodeA.getNowLocation(), nodeB.getNowLocation());
	}

	public boolean containsRegisteredPrefix(String o){
		return this.RegisteredPrefixList.contains(o);
	}
	
	public void addRegisteredPrefix(String o){
		this.RegisteredPrefixList.add(o);
	}
	
	public Node getNode() {
		return node;
	}

	public void setNode(Node node) {
		this.node = node;
	}

	public String getInterfaceURI() {
		return interfaceURI;
	}

	public void setInterfaceURI(String interfaceURI) {
		this.interfaceURI = interfaceURI;
	}
	
	public boolean isConnected(String interfaceURI) {
		L2Connection l2Connection = l2Connections.get(interfaceURI);
		if (l2Connection == null) {
			l2Connection = new L2Connection(interfaceURI, 0, false);
			l2Connections.put(interfaceURI, l2Connection);
		}
		return l2Connection.connected;
	}
	
	private void setConnected(String interfaceURI, boolean connected) {
		boolean prevConnected = isConnected(interfaceURI);
		L2Connection l2Connection = l2Connections.get(interfaceURI);
		l2Connection.connected = connected;
		if (!prevConnected && connected) {
			// 切断 -> 接続
			l2Connection.onConnectedTime = System.currentTimeMillis();
			onL2Connected(node.getEnvironment().getInterface(interfaceURI));
		} else if (prevConnected && !connected) {
			// 接続 -> 切断
			l2Connection.onConnectedTime = 0;
			onL2Disconnected(node.getEnvironment().getInterface(interfaceURI));
		} else if (connected) {
			// 接続 -> 接続
			if ((System.currentTimeMillis() - l2Connection.onConnectedTime) / 1000
					> ParamConfiguration.interval)
				onL2Connected(node.getEnvironment().getInterface(interfaceURI));		
		}
		
	}

	private void onL2Connected(Interface iFace) {
		((RMICNNode) this.getNode()).getNFD().getConnectionManager().onL2Connected(iFace);
	}

	private void onL2Disconnected(Interface iFace) {
		((RMICNNode) this.getNode()).getNFD().getConnectionManager().onL2Disconnected(iFace);
	}
	
	// 接続を監視する
	class ConnectionMonitor extends Thread {
		
		@Override
		public void run() {
			isConnectionMonitoring = true;
			monitorConnection();
			isConnectionMonitoring = false;
		}
		
		public void monitorConnection() {
			try {
				RMICNNode myNode = (RMICNNode) node;
				
				while(isConnectionMonitoring) {
					
					for (Entry<String, Node> e: node.getEnvironment().getNodes().entrySet()) {
						// 相手が自分ならスキップ
						if (e.getKey().equals(node.getName()))
							continue;
		
						RMICNNode oppositeNode = (RMICNNode) e.getValue();
						float d = Interface.this.getDistanceBetweenNodes(myNode, oppositeNode);

						String oppositeInterfaceURI = oppositeNode.getInterface().getInterfaceURI();
						if (d > ParamConfiguration.commRange) {
							// 対象ノードとつながっていない
							setConnected(oppositeInterfaceURI, false);
						} else {
							// 対象ノードとつながっている
							setConnected(oppositeInterfaceURI, true);
						}
					}
					// 寝ないとスレッドが止まる
					Thread.sleep(200);
				}
			} catch(InterruptedException e) {
				e.printStackTrace();
			}
		}
	}

	// Buffer を監視する
	class BufferMonitor extends Thread {

		@Override
		public void run() {
			isBufferMonitoring = true;
			monitorBuffer();
			isBufferMonitoring = false;
		}
		
		public void monitorBuffer() {
			try {
				while(isBufferMonitoring) {
					while(!bufferredPackets.isEmpty()) {
						receive(bufferredPackets.get(0));
						bufferredPackets.remove(0);
					}
					// 寝ないとスレッドが止まる
					Thread.sleep(200);
				}
			} catch(InterruptedException e) {
				e.printStackTrace();
			}
		}
	}
	
}

