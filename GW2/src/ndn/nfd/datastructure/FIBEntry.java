package ndn.nfd.datastructure;

import java.util.ArrayList;
import java.util.List;

//import ndn.nfd.face.Face;
import net.named_data.jndn.Face;

/**
 * FIB のエントリ
 * @author taku
 *
 */
public class FIBEntry extends TableEntry {

	protected List<Face> nexthops;
	
	public FIBEntry() {}
	
	public FIBEntry(String name) {
		super(name);
		this.nexthops = new ArrayList<Face>();
	}
	
	public FIBEntry(String name, Face nexthop) {
		super(name);
		this.nexthops = new ArrayList<Face>();
		nexthops.add(nexthop);
	}
	
	public FIBEntry(String name, List<Face> nexthops) {
		super(name);
		this.nexthops = nexthops;
	}
	
	public boolean isNexthop(Face inFace) {
		for (int i=0; i<nexthops.size(); i++) {
			if (inFace.getFaceID() == nexthops.get(i).getFaceID())
				return true;
		}
		return false;
	}
	
	public List<Face> getNextHops() {
		return nexthops;
	}
	
	public void setNextHops(List<Face> nexthops) {
		this.nexthops = nexthops;
	}
	
	public void addNexthop(Face nexthop) {
		if (!isNexthop(nexthop))
			this.nexthops.add(nexthop);
	}
	
	public void removeNexthop(Face nexthop) {
		this.nexthops.remove(nexthop);
	}
	
}
