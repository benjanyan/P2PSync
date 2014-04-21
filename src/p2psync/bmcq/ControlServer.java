package p2psync.bmcq;

import java.io.File;


public class ControlServer extends Server {
	private FileServer fileServer;
	private FileInfo rootFileInfo;
	
	ControlServer(int port, Path rootDirectory) {
		super(port, rootDirectory);
		this.description = "Control Server";
		fileServer = new FileServer(port + 1, this, rootDirectory);
	}
	
	public void run() {
		listen();
		this.rootFileInfo = new FileInfo(new File(rootDirectory.toString()),null);
		rootFileInfo.setFlags();
		rootFileInfo.refreshHashMap();
		control();
	}
	
	public void restart() {
		this.rootFileInfo = new FileInfo(new File(rootDirectory.toString()),null);
		fileServer = new FileServer(port + 1, this, rootDirectory);
		run();
	}
	
	public void exportFileInfo() {
		rootFileInfo = new FileInfo(new File(rootDirectory.toString()),null);
		rootFileInfo.export();
	}
	
	private void control() {
		int state = 1;
		String command = null;
		String param;
		String key;
		
		while (state == 1) {
			command_echo("request:key");
			command = readLine();
			if (command.matches("key:(.*)")) {
				key = getParam(command,"key:(.*)",1);
				if (key.matches(this.key)) {
					state = 2;
				}
			}
		}
		
		FileInfo remoteFileInfo = null;
		
		while (state == 2) {
			command_request("command");
			command = readLine();
			
			if (command.matches("echo:(.*)")) {
				param = getParam(command,"echo:(.*)",1);
				command_echo(param);
			} else if (command.equals("command:quit")) {
				command_echo("sayonara");
				state = 0;
			} else if (command.equals("command:nothing")) {
				//Nothing, ha!
			} else if (command.equals("request:done")) {
				
			} else if (command.equals("command:sync")) {
				remoteFileInfo = getRemoteFileInfo();
				Sync sync = new Sync(remoteFileInfo, rootDirectory, this);
				
				remoteFileInfo.setLocalRootPath(rootDirectory.toString());				
				rootFileInfo.detectConflicts(remoteFileInfo);
				
				remoteFileInfo.printContents();
				sync.executeSync();
			} else {
				command_echo("Wut?");
			}
		}
		
		try {
			Thread.sleep(1000);
		} catch (InterruptedException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
		
		fileServer.close();	
	}
	
	public void receiveFile(FileInfo fileInfo) {
		fileServer.getFile(fileInfo);
		if (readLine().matches("request:done")) {
			Utils.logD("Client and server ready for next file..");
		} else {
			Utils.logD("Client replied with something weird...");
		}
	}
}
