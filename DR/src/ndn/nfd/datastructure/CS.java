package ndn.nfd.datastructure;

import java.util.ArrayList;
import java.util.List;

/**
 * CS
 * @author taku
 *
 */
public class CS extends Table {

	private int entrySize;
	private List<String> fifoList;
	
	public CS() {
		this(Integer.MAX_VALUE);
	}
	
	public CS(int entrySize) {
		this.entrySize = entrySize;
		this.fifoList = new ArrayList<String>();
	}

	@Override
	public void insert(TableEntry entry) {
		super.insert(entry);
		fifoList.add(entry.getName());
		if (fifoList.size() > entrySize) {
			delete(fifoList.get(fifoList.size() - 1));
			fifoList.remove(fifoList.size() - 1);
		}
	}
	
}
