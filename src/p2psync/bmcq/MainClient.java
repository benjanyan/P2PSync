package p2psync.bmcq;

import java.io.File;
import java.net.InetAddress;
import java.net.UnknownHostException;

public class MainClient {

	/**
	 * @param args
	 */
	public static void main(String[] args) {
		RelativePath localSyncDirectory = new RelativePath("G:", "test");	//Without trailing slash
		String hostName = "kyouko.portsmouth";
		InetAddress host = null;
		FileInfo rootFileInfo = new FileInfo(new File(localSyncDirectory.toString()),null);
		
		rootFileInfo.setFlags();
		rootFileInfo.refreshHashMap();
				
		try {
			host = InetAddress.getByName(hostName);
		} catch (UnknownHostException e) {
			Utils.logE("Unable to resolve hostname");
			e.printStackTrace();
		}
		
		ControlClient controlClient = new ControlClient(host, 5555, localSyncDirectory, rootFileInfo);
		controlClient.setKey("jd874jks893ka");
		controlClient.connect();
		controlClient.control();

		
		ControlServer server = new ControlServer(5555, localSyncDirectory, rootFileInfo);
		server.setKey("jd874jks893ka");
		server.run();
		server = null;
		
		rootFileInfo = new FileInfo(new File(localSyncDirectory.toString()),null);
		rootFileInfo.export();
	}
}