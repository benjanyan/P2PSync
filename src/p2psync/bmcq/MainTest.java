package p2psync.bmcq;

import java.io.File;

public class MainTest extends Thread{

	public static void main(String[] args) {
		long startTime = System.currentTimeMillis();
		File testFolder = new File("/home/yuki_n/demo");
		FileInfo fileInfo = new FileInfo(testFolder, null);
		fileInfo.setFlags();
		fileInfo.refreshHashMap();
		fileInfo.printContents();
		fileInfo.export();
		System.out.println("Finished in " + (System.currentTimeMillis() - startTime) + "ms");
		
		
		
	}

}
