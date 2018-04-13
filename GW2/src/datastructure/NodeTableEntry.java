package datastructure;

import node.Location;

/**
 * Node Table のエントリ
 * @author taku
 *
 */
public class NodeTableEntry {
	
	private String nodeName;
	private int nodeType;
	private Location location;
	
	public NodeTableEntry() {
		
	}
	
	public NodeTableEntry(String nodeName, int nodeType, Location location) {
		this.nodeName = nodeName;
		this.nodeType = nodeType;
		this.location = location;
	}

	public String getNodeName() {
		return nodeName;
	}

	public void setNodeName(String nodeName) {
		this.nodeName = nodeName;
	}

	public int getNodeType() {
		return nodeType;
	}

	public void setNodeType(int nodeType) {
		this.nodeType = nodeType;
	}

	public Location getLocation() {
		return location;
	}

	public void setLocation(Location location) {
		this.location = location;
	}
	
}
