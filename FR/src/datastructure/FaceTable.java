package datastructure;

import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

/**
 * Face Table
 * @author taku
 *
 */
public class FaceTable {

	private Map<Integer, FaceTableEntry> table;

	public FaceTable() {
		this.table = new ConcurrentHashMap<Integer, FaceTableEntry>();
	}

	public Map<Integer, FaceTableEntry> getTable() {
		return table;
	}

	public void setTable(Map<Integer, FaceTableEntry> table) {
		this.table = table;
	}
	
}
