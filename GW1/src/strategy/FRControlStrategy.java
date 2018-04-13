package strategy;

import java.util.ArrayList;
import java.util.List;

import forwarder.FlyingRouterNFD;
import manager.PathManager;
//import ndn.message.Data;
//import ndn.message.Interest;
import ndn.nfd.datastructure.PITEntry;
//import ndn.nfd.face.Face;
import net.named_data.jndn.*;
import ndn.nfd.forwarder.Forwarding;
import ndn.nfd.strategy.Strategy;
import ndn.utility.NameUtility;
import node.Location;

/**
 * FRControl Strategy
 * @author taku
 *
 */
public class FRControlStrategy extends Strategy {

	// /FRControl/<FRName>/<function>/<arguments>
	
	public static final String prefix = "/FRControl";
	
	public FRControlStrategy(Forwarding forwarding) {
		super("FRControlStrategy", forwarding);
	}
	
	@Override
	public void afterReceiveInterest(Face inFace, Interest interest, PITEntry pitEntry) {
		super.afterReceiveInterest(inFace, interest, pitEntry);
		String name = interest.getNDNName();
		String[] nameComponents = NameUtility.split(name);
		// FRControl Strategy の場合は先に空の Data を返送する
		Data data = new Data(interest.getNDNName(), false);
		this.forwarding.sendData(data, inFace);
		
		if (!nameComponents[1].equals(this.forwarding.getNFD().getNode().getName())) {
			// 制御対象が自分でなければ何もしない
		} else if (nameComponents[2].equals("Discover")) {
			// Discover要求に対する処理（FR のみ）
			List<Location> locs = new ArrayList<Location>();
			for (int i=3; i<nameComponents.length; i++) {
				String[] s = nameComponents[i].split(",");
				float x = Float.parseFloat(s[0]);
				float y = Float.parseFloat(s[1]);
				Location loc = new Location(x, y);
				locs.add(loc);
			}
			discover(locs);
		} else if (nameComponents[2].equals("Crawl")) {
			// Crawl要求に対する処理（FR のみ）
			List<String> gwNames = new ArrayList<String>();
			int detourTime = Integer.parseInt(nameComponents[3]);
			for (int i=4; i<nameComponents.length; i++) {
				gwNames.add(nameComponents[i]);
			}
			crawl(detourTime, gwNames);
		}
	
	}

	@Override
	public void beforeSatisfyInterest(PITEntry pitEntry, Face inFace, Data data) {
		super.beforeSatisfyInterest(pitEntry, inFace, data);
		// 何もしない
	}
	
	private void discover(List<Location> locs) {
		// FR でしか呼ばれない
		PathManager pathManager = ((FlyingRouterNFD) this.forwarding.getNFD()).getPathManager();
		pathManager.discover(locs);
	}
	
	private void crawl(int detourTime, List<String> gwNames) {
		// FR でしか呼ばれない
		PathManager pathManager = ((FlyingRouterNFD) this.forwarding.getNFD()).getPathManager();
		pathManager.crawl(detourTime, gwNames);
	}

}
