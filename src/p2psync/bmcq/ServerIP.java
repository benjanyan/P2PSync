package p2psync.bmcq;

import java.net.InetAddress;

public class ServerIP {
	
	InetAddress localIp;
	InetAddress externalIp;
	
	ServerIP(InetAddress server) {
		this.localIp = server;
	}

}
