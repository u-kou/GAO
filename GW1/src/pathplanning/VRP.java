package pathplanning;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

import datastructure.NodeTableEntry;
import environment.RMICNEnvironment;
import node.Location;
import node.RMICNNode;

/**
 * VRP
 * @author taku
 *
 */
public class VRP {

	private List<NodeTableEntry> frs;
	private List<NodeTableEntry> customers;
	private RMICNEnvironment env;
	private Map<String, List<String>> pts;
	private Map<String, Double> distances;
	
	/**
	 * Node Table を与えて VRP を設定
	 * @param ntes
	 */
	public VRP(Map<String, NodeTableEntry> ntes, RMICNEnvironment env) {
		this.env = env;
		this.pts = new ConcurrentHashMap<String, List<String>>();
		this.distances = new ConcurrentHashMap<String, Double>();
		this.frs = new ArrayList<NodeTableEntry>();
		this.customers = new ArrayList<NodeTableEntry>();
		for (NodeTableEntry nte: ntes.values()) {
			switch (nte.getNodeType()) {
			case RMICNNode.TYPE_GW:
				customers.add(nte);
				break;
			case RMICNNode.TYPE_FR:
				frs.add(nte);
				break;
			case RMICNNode.TYPE_DR:
				customers.add(0, nte);
				break;
			}
		}
		
	}
	
	public double getDistance(int i, int j) {
		if (i == j || i >= customers.size() || j >= customers.size())
			return 0;
		Location iLoc = customers.get(i).getLocation();
		Location jLoc = customers.get(j).getLocation();
		return Location.calculateDistance(iLoc, jLoc);
	}

	public List<NodeTableEntry> getFlyingRouters() {
		return frs;
	}

	public void setFlyingRouters(List<NodeTableEntry> frs) {
		this.frs = frs;
	}

	public List<NodeTableEntry> getCustomers() {
		return customers;
	}

	public void setCustomers(List<NodeTableEntry> customers) {
		this.customers = customers;
	}
	
	public void initPaths() {
		this.env.initPaths();
	}
	
	public void addPath(Path path) {
		this.env.addPath(path);
	}
	
	public void setPaths(List<Path> paths) {
		this.env.setPaths(paths);
	}
	
	public void setSolution(List<List<Integer>> paths) {
		for (int k=0; k<paths.size(); k++) {
			List<Integer> path = paths.get(k);
			String frName = frs.get(k).getNodeName();
			List<String> gwNames = new ArrayList<String>();
			for (int i=0; i<path.size() - 1; i++) {
				int c = path.get(i);
				if (c != 0)
					gwNames.add(customers.get(c).getNodeName());
			}
			pts.put(frName, gwNames);
		}
	}

	public Map<String, List<String>> getSolution() {
		return pts;
	}

	public Map<String, Double> getDistances() {
		return distances;
	}

	public void setDistances(double[] D) {
		for (int k=0; k<D.length; k++) {
			String frName = frs.get(k).getNodeName();
			distances.put(frName, D[k]);
		}
	}
	
}
