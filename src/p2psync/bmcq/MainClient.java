package p2psync.bmcq;

import java.net.InetAddress;
import java.net.UnknownHostException;

public class MainClient {

	/**
	 * @param args
	 */
	public static void main(String[] args) {
		Path localSyncDirectory = new Path("/home/yuki_n/test");	//Without trailing slash
		String hostName = "kyouko.portsmouth";
		InetAddress host = null;
				
		try {
			host = InetAddress.getByName(hostName);
		} catch (UnknownHostException e) {
			Utils.logE("Unable to resolve hostname");
			e.printStackTrace();
		}
		
		ControlClient controlClient = new ControlClient(host, 5555, localSyncDirectory);
		controlClient.setKey("jd874jks893ka");
		ControlServer server = new ControlServer(5555, localSyncDirectory);
		server.setKey("jd874jks893ka");
		
		while (true) {

			controlClient.run();
			controlClient.exportFileInfo();	
			controlClient = null;
			server.restart();
		}
	}
}