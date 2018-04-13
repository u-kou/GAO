package ndn.nfd.face;

import ndn.nfd.datastructure.TableEntry;

/**
 * Data Filter Table のエントリ
 * @author taku
 *
 */
public class DataFilterTableEntry extends TableEntry {

	private DataFilter dataFilter;
	
	public DataFilterTableEntry(String name, DataFilter dataFilter) {
		super(name);
		this.dataFilter = dataFilter;
	}

	public DataFilter getDataFilter() {
		return dataFilter;
	}

	public void setDataFilter(DataFilter dataFilter) {
		this.dataFilter = dataFilter;
	}
	
}
