package ndn.nfd.datastructure;

//import ndn.message.Data;
import net.named_data.jndn.Data;

/**
 * CS のエントリ
 * @author taku
 *
 */
public class CSEntry extends TableEntry {
	
	private Data data;
	
	public CSEntry(String name, Data data) {
		super(name);
		this.data = data;
	}

	public Data getData() {
		return data;
	}

	public void setData(Data data) {
		this.data = data;
	}
	
}

