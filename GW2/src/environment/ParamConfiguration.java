package environment;

/**
 * シミュレーション用のパラメータを設定するクラス
 * @author taku
 *
 */
public class ParamConfiguration {

	// DTN or RMICN
	public static final boolean DTN = false;
	
	// FR の数
	public static final int frNum = 1;
	
	// 評価用データ
	public static final String filename = "./src/test/岡山_500m四方_node15";

	// 評価用データのスケール（m）
	public static final int scale = 500;
	
	// 評価用ネットワーク network（0: センサネットワーク，1: 災害ネットワーク）	
	public static int network = 0;
	
	// 評価用パケット数
	public static final int packetNum = 1000;
	
	// 評価用パケットの発生速度（num/s）
	public static final int packetFrequency = 5;
	
	// 無線インターフェースの通信レンジ（m）
	public static final float commRange = 10;
	
	// 接続イベント発生間隔、これだけつながってたら onL2Connected する（s）
	public static final float interval = 2;
	
	// ドローンのスピード（m/s）
	public static final float droneSpeed = 50f; // 13.9f
	
	// ドローンの移動における表示更新単位（s）
	public static final float movingUnit = 0.1f;
	
	// FR の独立ネットワークでの待機時間（s）
	public static final int waitTime = 1;
	
	public static String NetworkAddress;
	public static String DRAddress;
	public static String FRAddress;
	
}
