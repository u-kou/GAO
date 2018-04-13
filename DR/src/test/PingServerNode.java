package test;

import ndn.application.PingServer;
import ndn.environment.Environment;
import ndn.node.Node;

public class PingServerNode extends Node {

	public PingServer pingServer;

	public PingServerNode(String name, String interfaceURI
			, Environment environment, String filterName) {
		super(name, interfaceURI, environment);
		pingServer = new PingServer(this, filterName);
	}

}
