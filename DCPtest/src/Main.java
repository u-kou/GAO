import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;

public class Main {

	public static final String[] DCPCommand = { "/home/u-kou/eclipse-workspace/FRControlScript.py", "-c",
			"tcp:127.0.0.1:5760", "-f", "FNPathRun" };

	public static void main(String[] args) {
		Process DCP = null;
		BufferedReader errbr = null;
		String line = null;
		try {
			ProcessBuilder pb = new ProcessBuilder(DCPCommand);
			DCP = pb.start(); // 指定したコマンドを実行する
			// pb.redirectErrorStream(true);
			// pb.redirectInput();
			//

			//
			errbr = new BufferedReader(new InputStreamReader(DCP.getErrorStream()));
			BufferedReader inbr = new BufferedReader(new InputStreamReader(DCP.getInputStream()));
			while (true) {
				Thread.sleep(200);
				// System.out.println("wait.");

				line = errbr.readLine();
				if (line != null)
					System.out.println(line);
				
				line = inbr.readLine();
				if (line != null)
					System.out.println(line);

				if (line != null && line.contains("DCP finished")) {
					errbr.close();
					return;
				}
				//
				// DCP.waitFor();
				//

			}
		} catch (IOException e) {
			e.printStackTrace();
		}
		// } catch (InterruptedException r) {
		// r.printStackTrace();
		// }
		catch (InterruptedException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}

	}
}