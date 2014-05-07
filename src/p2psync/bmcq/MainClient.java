package p2psync.bmcq;

import java.net.InetAddress;
import java.net.UnknownHostException;

public class MainClient {

	/**
	 * @param args
	 */
	public static void main(String[] args) {
		Path localSyncDirectory = new Path("/home/yuki_n/test");	//The directory we want to sync! without the trailing slash
		String hostName = "kyouko.portsmouth";						//The machine to sync with. Can be an IP or a name we can resolve. Replaced with ServerDiscover().
		
		ServerDiscover serverDiscover = new ServerDiscover();	
		ServerIP host = serverDiscover.getServerIP();				//Find out the machine that we need to connect to from the external server.
		
		ControlClient controlClient = new ControlClient(host, 5555, localSyncDirectory);
		controlClient.setKey("jd874jks893ka");
		ControlServer server = new ControlServer(5555, localSyncDirectory);
		server.setKey("jd874jks893ka");
		controlClient.run();

		controlClient = null;
		server.run();
		server.exportFileInfo();
	}
}