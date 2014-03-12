package p2psync.bmcq;

import java.io.File;
import java.net.InetAddress;

public class ControlClient extends Client {
	
	protected FileClient fileClient;
	private FileInfo rootFileInfo;
	private Path rootDirectory;
	
	ControlClient(InetAddress host, int port, Path rootDirectory) {
		super(host,port);
		fileClient = new FileClient(host,port + 1,this);
		this.rootDirectory = rootDirectory;
	}
	
	public void run() {
		rootFileInfo = new FileInfo(new File(rootDirectory.toString()), null);
		rootFileInfo.setFlags();
		rootFileInfo.refreshHashMap();
		connect();
		control();
	}
	
	public void exportFileInfo() {
		rootFileInfo = new FileInfo(new File(rootDirectory.toString()),null);
		rootFileInfo.export();
	}
	
	protected void control() {
		int state = 2;
		String command;
		
		while (state == 1) {
			command = readLine();
			Utils.logD("Incoming Command: " + command);
		}
			
		
		//Initial key request and file sync request
		while (state == 2) {
			command = readLine();
			if (command.equals("request:key")) {
				command_key();
			} else if (command.equals("request:command")) {
				command_echo("command:sync");
				
				sendDirectoryInfo(rootFileInfo);
				fileClient.control(rootFileInfo);
				command_echo("command:nothing");
				
				state = 4;
			} else {
				command_echo("Wut?");
			}
		}
	

		
		while (state == 4) {
			Utils.logD("Client: Starting shutdown... (awaiting server request for command)");
			command = readLine();
			
			if (command.equals("request:command")) {
				command_echo("command:quit");
				state = 0;
				command = readLine();	//Wait for "sayonara" from server...
			}
		}
		
		close();

	}
}
