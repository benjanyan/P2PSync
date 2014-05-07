package p2psync.bmcq;

import java.io.File;

//Mainly intended for testing random stuff.

public class MainTest extends Thread{

	public static void main(String[] args) {
		long startTime = System.currentTimeMillis();
		File testFolder = new File("G:/test");
		FileInfo fileInfo = new FileInfo(testFolder, null);
		fileInfo.setFlags();
		fileInfo.refreshHashMap();
		fileInfo.printContents();
		fileInfo.export();
		System.out.println("Finished in " + (System.currentTimeMillis() - startTime) + "ms");
		
		
		
	}

}
