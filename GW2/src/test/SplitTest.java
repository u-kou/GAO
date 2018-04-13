package test;

public class SplitTest {

	public static void main(String[] args) {
		String s = "/FRControl/FR1/Crawl";
		String[] nameSpaces = s.split("/");
		for (String nameSpace: nameSpaces) {
			System.out.println("*" + nameSpace);
		}
	}
	
}
