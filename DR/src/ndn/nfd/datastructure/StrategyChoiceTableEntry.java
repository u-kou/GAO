package ndn.nfd.datastructure;

import ndn.nfd.strategy.Strategy;

/**
 * Strategy Choice Table のエントリ
 * @author taku
 *
 */
public class StrategyChoiceTableEntry extends TableEntry {
	
	private Strategy strategy;
	
	public StrategyChoiceTableEntry(String name, Strategy strategy) {
		super(name);
		this.strategy = strategy;
	}

	public Strategy getStrategy() {
		return strategy;
	}

	public void setStrategy(Strategy strategy) {
		this.strategy = strategy;
	}


}
