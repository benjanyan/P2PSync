package p2psync.bmcq;

import java.io.File;
import java.net.InetAddress;
import java.net.UnknownHostException;

public class Test {
	//Some of the code used for testing in the report.
	public static void main(String[] args)  {
		ServerDiscover serverDiscover = new ServerDiscover();
		ServerIP serverIp = null;
		try {
			serverIp = new ServerIP(InetAddress.getLocalHost());
		} catch (UnknownHostException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
		serverDiscover.setServerIP(serverIp);
		
		ServerIP test = serverDiscover.getServerIP();
		System.out.print(test.localIp + "" + test.externalIp);
	}
	
	static public void test1() {
		//Where cat.jpg resides
		File testDirectory = new File("G:/test");
		//The directory and a null parent (it's our root)
		FileInfo fileInfo = new FileInfo(testDirectory, null);	
		fileInfo.setFlags();
		fileInfo.refreshHashMap();
		fileInfo.printContents();
		//Export results for comparison for the next run
		fileInfo.export();
	}
	
}
