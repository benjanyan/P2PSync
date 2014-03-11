package p2psync.bmcq;

import java.io.File;
import java.net.InetAddress;

public class MainServer {

	public static void main(String[] args)  {
		
		
			Path localSyncDirectory = new Path("G:/test");	//Without trailing slash
			
			
			FileInfo rootFileInfo = new FileInfo(new File(localSyncDirectory.toString()),null);
			rootFileInfo.setFlags();
			rootFileInfo.refreshHashMap();
			
			ControlServer server = new ControlServer(5555, localSyncDirectory, rootFileInfo);
			server.setKey("jd874jks893ka");
			server.run();
			
			//rootFileInfo = null;
			InetAddress host = server.getClientAddress();
			
			ControlClient controlClient = new ControlClient(host, 5555, localSyncDirectory, rootFileInfo);
			controlClient.setKey("jd874jks893ka");
			controlClient.connect();
			controlClient.control();
			controlClient = null;
			
			
				//Resync lastRun files
			rootFileInfo = new FileInfo(new File(localSyncDirectory.toString()),null);
			rootFileInfo.export();
		
	}

}
