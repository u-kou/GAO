package ndn.nfd.manager;

import ndn.nfd.datastructure.StrategyChoiceTable;
import ndn.nfd.datastructure.StrategyChoiceTableEntry;
import ndn.nfd.forwarder.NFD;
import ndn.nfd.strategy.Strategy;

/**
 * Strategy Choice Manager
 * @author taku
 *
 */
public class SCManager extends Manager {

	private StrategyChoiceTable sct;

	public SCManager(NFD forwarder) {
		super(forwarder);
		this.name = "SCManager";
		this.sct = forwarder.getForwarding().getSCT();
	}

	public StrategyChoiceTableEntry insert(String name, Strategy strategy) {
		StrategyChoiceTableEntry sctEntry = new StrategyChoiceTableEntry(name, strategy);
		sct.insert(sctEntry);

		// LOG
		nfd.log(this.name, "Insert", "name: " + sctEntry.getName() + " strategy: " + strategy.getName());

		return sctEntry;

	}

	public void delete(String name) {
		StrategyChoiceTableEntry sctEntry = (StrategyChoiceTableEntry) sct.getEntry(name);
		Strategy strategy = sctEntry.getStrategy();
		sct.delete(name);

		// LOG
		nfd.log(this.name, "Delete", "name: " + sctEntry.getName() + " strategy: " + strategy.getName());

	}
}
