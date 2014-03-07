package p2psync.bmcq;

import java.io.File;
import java.net.InetAddress;
import java.net.UnknownHostException;

public class MainServer {

	public static void main(String[] args)  {
		String localSyncDirectory = "G:/test";	//Without trailing slash
		
		
		FileInfo rootFileInfo = new FileInfo(new File(localSyncDirectory),null);
		rootFileInfo.setFlags();
		rootFileInfo.refreshHashMap();
		
		ControlServer server = new ControlServer(5555, localSyncDirectory, rootFileInfo);
		server.setKey("jd874jks893ka");
		server.run();
		
		rootFileInfo = null;
		InetAddress host = server.getClientAddress();
		server = null;
		
		ControlClient controlClient = new ControlClient(host, 5555, localSyncDirectory, rootFileInfo);
		controlClient.setKey("jd874jks893ka");
		controlClient.connect();
		controlClient.control();
		controlClient = null;
		
		
			//Resync lastRun files
		rootFileInfo = new FileInfo(new File(localSyncDirectory),null);
		rootFileInfo.export();		
	}

}
