package environment;

import java.io.File;
import java.io.FileNotFoundException;
import java.util.ArrayList;
import java.util.List;
import java.util.Random;
import java.util.Scanner;

import application.RMICNApplication;
import ndn.node.Node;
import node.FlyingRouter;
import node.Gateway;
import node.Location;
import node.RMICNNode;

/**
 * RMICN のノードに関する設定を行うクラス
 * @author taku
 *
 */
public class RMICNConfiguration {
	
	private List<RMICNApplication> applications;
	private FlyingRouter dr;
	private List<FlyingRouter> frs;
	private List<Gateway> gws;
	private RMICNEnvironment env;
	//private int scale;

	public RMICNConfiguration(RMICNEnvironment env) {
		this.env = env;
		//this.scale = ParamConfiguration.scale;
		this.applications = new ArrayList<RMICNApplication>();
	}

	public void inputGateways(String filepath) {
		try {
			File file = new File(filepath);
			Scanner scan = new Scanner(file);
			scan.useDelimiter(System.lineSeparator());
			int num = 1;
			
			while (scan.hasNext()) {
				String line = scan.next();
				if (line.isEmpty())
					continue;
				String[] s = line.split(",");
				float x = Float.parseFloat(s[0]) * 500;
				float y = Float.parseFloat(s[1]) * 500;
				//float x = Float.parseFloat(s[0]);
				//float y = Float.parseFloat(s[1]);
				Location location = new Location(x, y);
				String name = "GW" + num;
				String interfaceURI = "10.0.0." + num; //TODO change
				//String interfaceURI = ParamConfiguration.DRAddress;
				Gateway gateway = new Gateway(name, interfaceURI, env, location);
				env.addNode(gateway);
				num++;
			}
			
			scan.close();

		} catch (FileNotFoundException e) {
			System.out.println(e);
		}
	}
	
	public Location caluculateDepot() {
		/*
		int gwNum = 0;
		float x = 0;
		float y = 0;
		for (Node temp: env.getNodes().values()) {
			RMICNNode node = (RMICNNode) temp;
			if (node.getNodeType() != RMICNNode.TYPE_GW)
				continue;
			x += node.getNowLocation().x;
			y += node.getNowLocation().y;
			gwNum++;
		}
		
		return new Location(x/gwNum, y/gwNum);
		*/
		return new Location(0, 0);
	}
	
	public void initCustomTables() {
		dr = null;
		frs = new ArrayList<FlyingRouter>();
		gws = new ArrayList<Gateway>();
		for (Node e: env.getNodes().values()) {
			RMICNNode node = (RMICNNode) e;
			switch (node.getNodeType()) {
			case RMICNNode.TYPE_GW:
				gws.add((Gateway) node);
				break;
			case RMICNNode.TYPE_FR:
				frs.add((FlyingRouter) node);
				break;
			case RMICNNode.TYPE_DR:
				dr = (FlyingRouter) node;
				break;
			}
		}
		
		env.createNodeTable(dr, env.getNodes());
		for (FlyingRouter fr: frs) {
			env.createFace(dr, fr);
			env.createFace(fr, dr);
			env.createNodeTable(fr, env.getNodes());
			for (FlyingRouter oppositeFR: frs) {
				env.createFace(fr, oppositeFR);
			}
			for (Gateway gw: gws) {
				env.createFace(gw, fr);
				env.createFace(fr, gw);
			}
		}
		
	}
	
	public void initApplication() {
		String basePrefix = "/RMICN";
		// initCustomTablesの後で呼ぶ
		for (Gateway gw: gws) {
			String servicePrefix = basePrefix + "/" + gw.getName() + "Service";
			RMICNApplication a = new RMICNApplication(gw, servicePrefix, basePrefix);
			applications.add(a);
			gw.getApplications().add(a);
		}
	}
	
	// キャッシュが使えない
	public void sensorNetworkPattern(int max) {
		int n = applications.size();
		int k = 0;
		while (k < max) {
			Random r = new Random();
			int i = (int)(r.nextDouble() * n);
			int j = (int)(r.nextDouble() * n);
			if (i == j)
				j = (j + 1) % n;
			RMICNApplication a1 = applications.get(i);
			RMICNApplication a2 = applications.get(j);
			boolean ret = a1.sendInterest(a2.getInterestFilterName() + "/" + System.currentTimeMillis());
			if (ret)
				k++;
			try {
				Thread.sleep(200);
			} catch (InterruptedException e) {
				e.printStackTrace();
			}
		}
	}
	
	// キャッシュが使える
	public void disasterNetworkPattern(int max) {
		int n = applications.size();
		int k = 0;
		while (k < max) {
			Random r = new Random();
			int i,j;
			String name;
			if (r.nextDouble() < 0.1) {
				j = 0;
				name = applications.get(0).getInterestFilterName() + "/apple";
			} else {
				j = (int)(r.nextDouble() * (n-1)) + 1;
				name = applications.get(j).getInterestFilterName() + "/" + System.currentTimeMillis();
			}
			i = (int)(r.nextDouble() * n);
			if (i == j)
				i = (i + 1) % n;
			RMICNApplication a1 = applications.get(i);
			boolean ret = a1.sendInterest(name);
			if (ret)
				k++;
			try {
				Thread.sleep(200);
			} catch (InterruptedException e) {
				e.printStackTrace();
			}
		}
	}
	
//	public void OneFRNetworkPattern(){
//		RMICNApplication a1 = applications.get(0);
//		RMICNApplication a2 = applications.get(1);
//		boolean ret = a2.sendInterest(a1.getInterestFilterName() + "/" + System.currentTimeMillis());
//		if (ret)
//			return;
//		try {
//			Thread.sleep(200);
//		} catch (InterruptedException e) {
//			e.printStackTrace();
//		}
//	}

	public FlyingRouter getDR() {
		return dr;
	}

	public void setDR(FlyingRouter dr) {
		this.dr = dr;
	}

	public List<FlyingRouter> getFRs() {
		return frs;
	}

	public void setFRs(List<FlyingRouter> frs) {
		this.frs = frs;
	}

	public List<Gateway> getGWs() {
		return gws;
	}

	public void setGWs(List<Gateway> gws) {
		this.gws = gws;
	}
	
}
