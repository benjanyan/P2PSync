package p2psync.bmcq;

public class Utils {
	
	static boolean isAndroid() {
		return System.getProperty("java.vm.name").equalsIgnoreCase("dalvik");
	}
	
	static void logD(String message) {
		if (isAndroid()) {
			//Log.d("P2PSync", message);
		} else {
			System.out.println(message);
		}
	}
	
	static void logE(String message) {
		if (isAndroid()) {
			//Log.e("P2PSync", message);
		} else {
			System.err.println(message);
		}
	}

}
