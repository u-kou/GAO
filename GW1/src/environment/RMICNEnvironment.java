package environment;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;

import pathplanning.Path;
import datastructure.NodeTableEntry;
import display.Area;
import forwarder.FlyingRouterNFD;
import forwarder.RMICNFD;
import ndn.environment.Environment;
//import ndn.nfd.face.Face;
import net.named_data.jndn.Face;
import ndn.node.Node;
import node.FlyingRouter;
import node.RMICNNode;

/**
 * RMICN の環境情報を管理するクラス
 * @author taku
 *
 */
public class RMICNEnvironment extends Environment {

	private Area area;
	private List<Path> paths;
	
	public RMICNEnvironment(Area area) {
		this.area = area;
		this.paths = new ArrayList<Path>();
	}
	
	public void addNode(RMICNNode node) {
		super.addNode(node);
		this.area.addNode(node);
	}
	
	public void addPath(Path path) {
		if (paths != null) {
			paths.add(path);
			this.area.addPath(path);
		}
	}
	
	@Override
	public void deleteNode(String nodeName) {
		super.deleteNode(nodeName);
		this.area.deleteNode(nodeName);
	}
	
	public RMICNNode getNode(String nodeName) {
		return (RMICNNode) nodes.get(nodeName);
	}

	public List<Path> getPaths() {
		return paths;
	}

	public void setPaths(List<Path> paths) {
		this.paths = paths;
		this.area.setPaths(paths);
	}
	
	public void initPaths() {
		this.paths.clear();
		this.area.initPaths();
	}
	
	public void initNodes() {
		this.nodes.clear();
		this.area.initNodes();
	}
	
	/*
	public void onNodeMoved() {
		this.area.repaint();
	}
	
	public void onContentInfoUpdated() {
		this.area.repaint();
	}
	*/
	
	public void createFace(RMICNNode node, RMICNNode oppositeNode) {
		RMICNFD nfd = node.getNFD();
		Face face = nfd.getFaceManager().findOrCreateFace(oppositeNode);
		nfd.getInfoManager().updateFaceTable(face, oppositeNode.getName(), false);
	}
	
	public void createNodeTable(FlyingRouter fr, List<RMICNNode> nodes) {
		for (RMICNNode oppositeNode: nodes) {
			if (fr != oppositeNode 
					&& fr.getNodeType() == RMICNNode.TYPE_FR
					&& oppositeNode.getNodeType() == RMICNNode.TYPE_FR)
				continue;
			createNodeTableEntry(fr, oppositeNode);
		}
	}
	
	public void createNodeTable(FlyingRouter fr, Map<String, Node> nodes) {
		for (Node e: nodes.values()) {
			RMICNNode oppositeNode = (RMICNNode) e;
			createNodeTableEntry(fr, oppositeNode);
		}
	}
	
	public void createNodeTableEntry(FlyingRouter fr, RMICNNode oppositeNode) {
		FlyingRouterNFD nfd = fr.getNFD();
		NodeTableEntry nte = new NodeTableEntry(oppositeNode.getName()
					, oppositeNode.getNodeType(), oppositeNode.getNowLocation());
		nfd.getInfoManager().updateNodeTable(nte);
	}
	
}
