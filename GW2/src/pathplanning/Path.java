package pathplanning;

import node.Location;

/**
 * Path を表現するクラス
 * @author taku
 *
 */
public class Path {

	private Location locA;
	private Location locB;
	
	public Path(Location locA, Location locB) {
		this.locA = locA;
		this.locB = locB;
	}

	public Location getLocA() {
		return locA;
	}

	public void setLocA(Location locA) {
		this.locA = locA;
	}

	public Location getLocB() {
		return locB;
	}

	public void setLocB(Location locB) {
		this.locB = locB;
	}
	
}
