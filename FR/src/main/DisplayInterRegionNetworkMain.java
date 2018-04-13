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
 * 地点間ネットワークを表示する
 * @author taku
 *
 */
public class DisplayInterRegionNetworkMain {

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

		nc.initCustomTables();
		nc.initApplication();
		DRPathManager pathManager = ((DRPathManager) dr.getNFD().getPathManager());
		pathManager.inputNetwork(filename);
	}
	
}
