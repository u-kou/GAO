package ndn.nfd.face;

//import ndn.message.Data;
import net.named_data.jndn.Face;
import net.named_data.jndn.Data;

/**
 * Face において Data をフィルタリングするリスナ
 * @author taku
 *
 */
public interface DataFilter {
	public void onData(Face receiver, Data data);
	public void setDataFilter(Face face);
}
