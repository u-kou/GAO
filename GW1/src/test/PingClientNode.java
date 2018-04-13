package test;

import ndn.application.PingClient;
import ndn.environment.Environment;
import ndn.node.Node;

public class PingClientNode extends Node {

	public PingClient pingClient;

	public PingClientNode(String name, String interfaceURI
			, Environment environment, String filterName) {
		super(name, interfaceURI, environment);
		this.pingClient = new PingClient(this, filterName);
	}


}
