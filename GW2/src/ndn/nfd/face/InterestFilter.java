package ndn.nfd.face;

//import ndn.message.Interest;
import net.named_data.jndn.Interest;
import net.named_data.jndn.Face;

/**
 * Face において Data をフィルタリングするリスナ
 * @author taku
 *
 */
public interface InterestFilter {
	public void onInterest(Face receiver, Interest interest);
	public void setInterestFilter(Face face);
}
