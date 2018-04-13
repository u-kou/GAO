package main;

import java.io.File;
import java.io.FileWriter;
import java.io.IOException;
import java.util.ArrayList;
import java.util.List;
import java.util.Random;
import java.util.Scanner;

/**
 * 評価用元データから評価用データを作成する
 * @author taku
 *
 */
public class ConvertScaleMain {

	public static void main(String[] args) {
		String basePath = "./src/test/";
		nodeScale(basePath + "岡山_500m四方", 15);
		nodeScale(basePath + "埼玉_500m四方", 15);
		nodeScale(basePath + "青森_500m四方", 15);
		nodeScale(basePath + "鳥取_500m四方", 15);
		nodeScale(basePath + "北海道_500m四方", 15);
	}
	
	
	public static void nodeScale(String filename, int nodeNum) {
		
		try {
			File file = new File(filename + ".txt");
			Scanner scan;
			scan = new Scanner(file);
			scan.useDelimiter(System.lineSeparator());
			List<String> lines = new ArrayList<String>();
			while (scan.hasNext()) {
				String line = scan.next();
				if (line.isEmpty())
					continue;
				lines.add(line);
			}
			scan.close();
			
			double th = (double) nodeNum / lines.size();
			StringBuilder sb = new StringBuilder();
			for (String line: lines) {
				Random r = new Random();
				double p = r.nextDouble();
				if (p < th) {
					sb.append(line + System.lineSeparator());
				}
			}
			
			file = new File(filename + "_node" + nodeNum + ".txt");
			FileWriter filewriter = new FileWriter(file);
			filewriter.write(sb.toString());
			filewriter.close();
			
		} catch (IOException e) {
			// TODO 自動生成された catch ブロック
			e.printStackTrace();
		}
		
	}
	
}
