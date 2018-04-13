package ndn.nfd.face;

import ndn.nfd.datastructure.TableEntry;
import net.named_data.jndn.InterestFilter;
/**
 * Interest Filter Table のエントリ
 * @author taku
 *
 */
public class InterestFilterTableEntry extends TableEntry {

	private InterestFilter interestFilter;
	
	public InterestFilterTableEntry(String name, InterestFilter interestFilter) {
		super(name);
		this.interestFilter = interestFilter;
	}

	public InterestFilter getInterestFilter() {
		return interestFilter;
	}

	public void setInterestFilter(InterestFilter interestFilter) {
		this.interestFilter = interestFilter;
	}
	
}
