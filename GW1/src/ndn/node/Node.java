package ndn.node;

import ndn.environment.Environment;
import ndn.layer2.Interface;
import ndn.nfd.forwarder.NFD;

/**
 * NDN のノード
 * @author taku
 *
 */
public class Node {

	protected String name;
	protected NFD nfd;
	protected Environment environment;
	
	// 簡単化のために Interface を一個しか持たない
	protected Interface iFace;

	// 継承用コンストラクタ
	public Node(String name, String interfaceURI, Environment environment, boolean extended) {
		this.name = name;
		this.iFace = new Interface(interfaceURI, this);
		this.environment = environment;
	}
	
	public Node(String name, String interfaceURI, Environment environment) {
		this(name, interfaceURI, environment, false);
		this.nfd = new NFD(this);
		this.iFace.start();
	}

	public String getName() {
		return name;
	}

	public void setName(String name) {
		this.name = name;
	}

	public void print(String content) {
		System.out.println("[" + name + "]");
		System.out.println(content);
	}

	public NFD getNFD() {
		return nfd;
	}

	public void setNFD(NFD nfd) {
		this.nfd = nfd;
	}

	public Interface getInterface() {
		return iFace;
	}

	public void setWirelessInterface(Interface iFace) {
		this.iFace = iFace;
	}

	public Environment getEnvironment() {
		return environment;
	}

	public void setEnvironment(Environment environment) {
		this.environment = environment;
	}
	
}
