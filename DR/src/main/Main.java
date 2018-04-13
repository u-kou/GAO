package main;

//import display.MainWindow;
import environment.ParamConfiguration;
import environment.RMICNConfiguration;
import environment.RMICNEnvironment;
import manager.DRPathManager;
import manager.PathManager;
import node.FlyingRouter;
import node.Location;
import display.Area;
import java.util.Scanner;

/**
 * 地点間ネットワークを算出するところから開始
 * @author taku
 *
 */
public class Main {

	public static void main(String[] args) {
		//Scanner scanner = new Scanner(System.in);
		//System.out.println("(DR program)\nPlease input network address(e.g. 192.168.1)");
		System.out.println("(DR program)");
		//String NetworkAddress = scanner.nextLine();
		//ParamConfiguration.NetworkAddress = NetworkAddress;

		
//		System.out.println("Please input DR's address(e.g. 101)");
//		String DRAddress = scanner.nextLine();
//		ParamConfiguration.DRAddress = NetworkAddress + "." + DRAddress;
		ParamConfiguration.DRAddress = "127.0.0.1";
		
//		System.out.println("Please input FR's address(e.g. 102)");
//		String FRAddress = scanner.nextLine();
//		ParamConfiguration.FRAddress = NetworkAddress + "." + FRAddress;
//		scanner.close();
		ParamConfiguration.FRAddress = "127.0.0.2";
		
		
		String filename = ParamConfiguration.filename;
		//MainWindow mainWindow = new MainWindow();
		//mainWindow.initArea();
		Area RMICNArea = new Area();
		//RMICNEnvironment e = new RMICNEnvironment(mainWindow.getArea());
		RMICNEnvironment e = new RMICNEnvironment(RMICNArea);
		RMICNConfiguration nc = new RMICNConfiguration(e);
		nc.inputGateways(filename + ".txt");

		//Location depot = nc.caluculateDepot();
		Location depot = new Location(0, 0);

		
		// DR
		String drName = "DR";
		String drInterfaceURI = ParamConfiguration.DRAddress;
		FlyingRouter dr =
				new FlyingRouter(drName, drInterfaceURI, e, true, depot);
		e.addNode(dr);

		// FR
		//for (int k=0; k<ParamConfiguration.frNum; k++) {
			//String frName = "FR" + (k+1);
		
		String frName = "FR0";
		String frInterfaceURI = ParamConfiguration.FRAddress; 
		FlyingRouter fr =
				new FlyingRouter(frName, frInterfaceURI, e, false, new Location(depot));
		e.addNode(fr);
		
		
		//}

		nc.initCustomTables();
		nc.initApplication();
		
		
		DRPathManager pathManager = ((DRPathManager) dr.getNFD().getPathManager());
		pathManager.setPhase(PathManager.CRAWLING_PHASE);
		pathManager.calculateNetwork();
		//pathManager.deliver();

		/*
		if (ParamConfiguration.network == 0)
			// センサネットワーク
			nc.sensorNetworkPattern(ParamConfiguration.packetNum);
		else if (ParamConfiguration.network == 1)
			// 災害ネットワーク
			nc.disasterNetworkPattern(ParamConfiguration.packetNum);
			*/
			
	}

}
