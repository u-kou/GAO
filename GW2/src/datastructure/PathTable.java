package datastructure;

import java.util.List;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

/**
 * Path Table
 * @author taku
 *
 */
public class PathTable {

	// FR name -> GW List
	private Map<String, List<String>> table;

	public PathTable() {
		this.table = new ConcurrentHashMap<String, List<String>>();
	}

	public Map<String, List<String>> getTable() {
		return table;
	}

}
