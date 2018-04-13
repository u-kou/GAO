package test;

import java.util.Scanner;

import ndn.environment.Environment;
import ndn.node.Node;

public class PingTest {

	public static void main(String[] args) {

		Environment network = new Environment();

		String clientName = "CLIENT";
		String routerName = "ROUTER";
		String serverName = "SERVER";
		String pingFilterName = "/fruits";

		PingClientNode client = new PingClientNode(clientName, "udp://192.168.1.1", network, pingFilterName);
		Node router = new Node(routerName, "udp://192.168.1.2", network);
		PingServerNode server = new PingServerNode(serverName, "udp://192.168.1.3", network, pingFilterName);

		// Node 追加
		network.addNode(client);
		network.addNode(router);
		network.addNode(server);

		// Table 登録
		network.setFIBEntry(router, pingFilterName, server);

		// FaceFilter 登録
		client.pingClient.setDataFilter(client.getNFD().getFaceManager().findOrCreateFace(router));
		server.pingServer.setInterestFilter(server.getNFD().getFaceManager().findOrCreateFace(router));

		boolean isEnd = false;
		Scanner scan;
		String s;

		while(!isEnd) {
			try {
				Thread.sleep(200);
				System.out.print("\nInput content name (exit for 'q') > ");
				scan = new Scanner(System.in);
				switch((s = scan.next())) {
				case "q":
					isEnd = true;
					break;
				default:
					client.pingClient.sendPingMessage(s, client.getNFD().getFaceManager().findOrCreateFace(router));
				}
			} catch (InterruptedException e) {
				e.printStackTrace();
			}
		}

		System.out.println("Terminated.");

	}

}
