package p2psync.bmcq;

import java.io.File;

/*
 * Handles the protocol for the server side. One of the main parts of the software.
 */

public class ControlServer extends Server {
	private FileServer fileServer;
	private FileInfo rootFileInfo;
	
	ControlServer(int port, Path rootDirectory) {
		super(port, rootDirectory);
		this.description = "Control Server";
		fileServer = new FileServer(port + 1, this, rootDirectory);
	}
	
		//Our entry point, it kicks everything off calls control() to handle the communication with the client
	public void run() {
		listen();						//We can't progress from this point until the client has connected.
		this.rootFileInfo = new FileInfo(new File(rootDirectory.toString()),null);		//Our directory to sync is traversed and changed to object form. It has no parent so we pass "null".
		rootFileInfo.setFlags();		//Set the modified or deleted flags based on the previous run if it exists.
		rootFileInfo.refreshHashMap();	//HashMap for looking up files quickly. Should be called everytime the rootFileInfo changes.
		control();
	}
	
	public void restart() {				//When the process is complete, we reset and wait for connections again via the run() method.
		//this.rootFileInfo = new FileInfo(new File(rootDirectory.toString()),null); Not sure why I have this again. It's rebuilt again in run() anyway.
		fileServer = new FileServer(port + 1, this, rootDirectory);
		try {
			Thread.sleep(2000);
		} catch (InterruptedException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
		run();
	}
	
	public void exportFileInfo() {		//Write our FileInfo objects for comparison the next time we run rootFileInfo.setFlags()
		rootFileInfo = new FileInfo(new File(rootDirectory.toString()),null);
		rootFileInfo.export();
	}
	
	private void control() {
		int state = 1;
		String command = null;
		String param;
		String key;
		
			//Key request
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
			//Main commands
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
