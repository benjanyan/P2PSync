package p2psync.bmcq;

import java.net.InetAddress;

public class MainServer {

	public static void main(String[] args)  {
			Path localSyncDirectory = new Path("G:/test");	//Without trailing slash
			ControlServer server = new ControlServer(5555, localSyncDirectory);
			server.setKey("jd874jks893ka");
			server.informMasterServer();
			server.run();
			
			while (true) {
				
				ControlClient controlClient = new ControlClient(server.serverIp, 5555, localSyncDirectory);
				controlClient.setKey("jd874jks893ka");
				controlClient.run();
				controlClient.exportFileInfo();	
				controlClient = null;
				
				server.restart();
			}
			
	}

}
