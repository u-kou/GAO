package manager;

import ndn.layer2.Interface;

/**
 * Layer2 で接続・切断された際のイベントリスナ
 * @author taku
 *
 */
public interface L2ConnectedListener {

	/**
	 * Interface が接続された時に呼ばれるイベントリスナ
	 * @param iFace
	 */
	public void onL2Connected(Interface iFace);
	
	/**
	 * Interface が切断された時に呼ばれるイベントリスナ
	 * @param iFace
	 */
	public void onL2Disconnected(Interface iFace);

}
