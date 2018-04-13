package main;

import manager.DRPathManager;
import node.FlyingRouter;
import node.Location;
import display.Area;
//import display.MainWindow;
import environment.ParamConfiguration;
import environment.RMICNConfiguration;
import environment.RMICNEnvironment;

/**
 * 地点間ネットワークを算出する
 * @author taku
 *
 */
public class InterRegionNetworkMain {

	public static void main(String[] args) {
		String filename = ParamConfiguration.filename;
		//MainWindow mainWindow = new MainWindow();
		//mainWindow.initArea();
		Area RMICNArea = new Area();
		//RMICNEnvironment e = new RMICNEnvironment(mainWindow.getArea());
		RMICNEnvironment e = new RMICNEnvironment(RMICNArea);
		RMICNConfiguration nc = new RMICNConfiguration(e);
		nc.inputGateways(filename + ".txt");
		
		Location depot = nc.caluculateDepot();
		
		// DR
		String drName = "DR";
		String drInterfaceURI = "udp:/192.168.1.1:6565";
		FlyingRouter dr = 
				new FlyingRouter(drName, drInterfaceURI, e, true, depot);
		e.addNode(dr);
		
		// FR
		for (int k=0; k<ParamConfiguration.frNum; k++) {
			String frName = "FR" + (k+1);
			String frInterfaceURI = "udp:/192.168.2." + (k+1) + ":6565";
			FlyingRouter fr =
					new FlyingRouter(frName, frInterfaceURI, e, false, new Location(depot));
			e.addNode(fr);
		}
		
		nc.initCustomTables();
		DRPathManager pathManager = ((DRPathManager) dr.getNFD().getPathManager());
		long past = System.currentTimeMillis();
		pathManager.outputNetwork(filename);
		long now = System.currentTimeMillis();
		System.out.println((now - past)/1000.0);
		
	}
	
}
