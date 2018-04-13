package node;

/**
 * Location 情報
 * @author taku
 *
 */
public class Location {

	public float x;
	public float y;

	public Location() {
		this.x = 0;
		this.y = 0;
	}

	public Location(float x, float y) {
		setLocation(x, y);
	}
	
	public Location(Location loc) {
		this.x = loc.x;
		this.y = loc.y;
	}

	public void setLocation(float x, float y) {
		this.x = x;
		this.y = y;
	}

	public static float calculateDistance(Location a, Location b) {
		float m = a.x - b.x;
		float n = a.y - b.y;
		return (float) Math.sqrt(m * m + n * n);
	}

}
