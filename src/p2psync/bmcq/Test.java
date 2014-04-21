package p2psync.bmcq;

import java.io.File;

public class Test {

	public static void main(String[] args)  {
		test1();
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
