package ndn.nfd.datastructure;

import ndn.utility.NameUtility;

/**
 * Table のエントリ
 * @author taku
 *
 */
public class TableEntry {

	protected String name;
	
	public TableEntry() {}
	
	public TableEntry(String name) {
		this.name = NameUtility.filter(name);
	}

	public String getName() {
		return name;
	}

	public void setName(String name) {
		this.name = name;
	}
	
}
