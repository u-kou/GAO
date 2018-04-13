package ndn.nfd.datastructure;

import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

import ndn.utility.NameUtility;

/**
 * Table
 * @author taku
 *
 */
public class Table {

	protected Map<String, TableEntry> entries;

	public Table() {
		this.entries = new ConcurrentHashMap<String, TableEntry>();
	}

	/**
	 * エントリがあれば値を返し，なければ null を返す
	 * @param name
	 * @return
	 */
	public TableEntry find(String name) {
		return entries.get(NameUtility.filter(name));
	}

	/**
	 * 最長一致でエントリを検索．あれば値，なければ null を返す．
	 * @return
	 */
	public TableEntry findByLM(String name) {
		TableEntry entry = null;
		String[] components = name.split("/");
		int length = components.length;
		for (int i=length; i>0; i--) {
			String s = "";
			for (int j=0; j<i; j++) {
				if (components[j].isEmpty())
					s += "/";
				else
					s += components[j] + "/";
			}
			entry = find(s);
			if (entry != null) break;
		}
		if (length == 0)
			entry = find("/");
		return entry;
	}

	public void insert(TableEntry entry) {
		entries.put(entry.getName(), entry);
	}

	public void delete(String name) {
		entries.remove(NameUtility.filter(name));
	}

	public Map<String, TableEntry> getEntries() {
		return entries;
	}

	public void setEntries(Map<String, TableEntry> entries) {
		this.entries = entries;
	}

	public TableEntry getEntry(String name) {
		return entries.get(NameUtility.filter(name));
	}

}
