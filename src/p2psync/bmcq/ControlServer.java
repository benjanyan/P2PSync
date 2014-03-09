package p2psync.bmcq;

import java.io.File;
import java.io.IOException;
import java.io.InputStream;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;
import java.io.OutputStream;
import java.nio.file.Path;
import java.util.regex.Matcher;
import java.util.regex.Pattern;


public class ControlServer extends Server {
	private FileServer fileServer;
	private Path rootDirectory;
	private FileInfo rootFileInfo;
	
	ControlServer(int port, Path rootDirectory, FileInfo rootFileInfo) {
		super(port);
		this.description = "Control Server";
		this.rootDirectory = rootDirectory;
		this.rootFileInfo = rootFileInfo;
		fileServer = new FileServer(port + 1, this, rootDirectory);
	}
	
	public void run() {
		listen();
		control();
	}
	
	private String getParam(String command, String regEx, int paramNo) {
		Pattern pattern = Pattern.compile(regEx);
		Matcher matcher = pattern.matcher(command);
		
		if (matcher.matches()) {
			return matcher.group(paramNo);
		} else {
			return "";
		}
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
		FileInfo fileInfo = null;
		//Directory directory = new Directory(rootDirectory, false, rootDirectory);
		
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
				
				//fileInfo = new FileInfo(new File(rootDirectory),null);
				//fileInfo.export();

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
		close();		
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
