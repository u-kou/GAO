package datastructure;

import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

/**
 * Node Table
 * @author taku
 *
 */
public class NodeTable {

	private Map<String, NodeTableEntry> table;

	public NodeTable() {
		this.table = new ConcurrentHashMap<String, NodeTableEntry>();
	}

	public Map<String, NodeTableEntry> getTable() {
		return table;
	}

	public void setTable(Map<String, NodeTableEntry> table) {
		this.table = table;
	}

}
