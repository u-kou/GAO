package ndn.utility;

/**
 * 名前の処理に関するユーティリティー
 * @author taku
 *
 */
public class NameUtility {

	// 末尾の「/」を削除
	public static String filter(String name) {
		if (!name.equals("/") && name.endsWith("/"))
			return name.substring(0, name.length() - 1);
		return name;
	}
	
	// name component の取得
	public static String[] split(String name) {
		String[] cuts = filter(name).split("/");
		String[] nameComponents = new String[cuts.length - 1];
		for (int i=0; i<nameComponents.length; i++) {
			nameComponents[i] = cuts[i + 1];
		}
		return nameComponents;
	}

}
