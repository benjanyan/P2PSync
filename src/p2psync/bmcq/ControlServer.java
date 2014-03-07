package p2psync.bmcq;

import java.io.File;
import java.io.IOException;
import java.io.InputStream;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;
import java.io.OutputStream;
import java.util.regex.Matcher;
import java.util.regex.Pattern;


public class ControlServer extends Server {
	private FileServer fileServer;
	private String rootDirectory;
	private FileInfo rootFileInfo;
	
	ControlServer(int port, String rootDirectory, FileInfo rootFileInfo) {
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
				
				remoteFileInfo.setLocalRootPath(rootDirectory);				
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
	
	protected void command_echo(String text) {
		Utils.logD("Me->Client: " + text);
		try {
			output.write((text + "\n").getBytes());
		} catch (IOException ioe) {
			Utils.logE("Server->echo: Failed to write text to output" + ioe.getMessage());
			System.exit(1);
		}
	}
	
	public void sendDirectoryInfo(FileInfo fileInfo) {
		OutputStream directoryInfoOutput = null;
		String command = readLine();
		directoryInfoOutput = output;
		
		if (command.equals("request:FileInfo")) {
			Utils.logD("Sending DirectoryInfo Object to client...");
			try {
				ObjectOutputStream objectOut = new ObjectOutputStream(directoryInfoOutput);
				objectOut.writeObject(fileInfo);
				objectOut.flush();
			} catch (IOException e) {
				Utils.logE("Failed to create ObjectOutput\n" + e.getMessage());
				e.printStackTrace();
			}
			command_echo("request:done");
		}
	}
	
	private FileInfo getRemoteFileInfo() {
		FileInfo fileInfo = null;
		InputStream fileInfoInput = null;
		
		Utils.logD("Client: Preparing to receive directory details back from the server...");
		
		try {
			command_echo("request:FileInfo");
			fileInfoInput = input;
			ObjectInputStream objectIn = new ObjectInputStream(fileInfoInput);
			fileInfo = (FileInfo) objectIn.readObject();
		} catch (IOException e) {
			Utils.logE("Failed to create ObjectInput\n" + e.getMessage());
			e.printStackTrace();
		} catch (ClassNotFoundException e) {
			Utils.logE("Failed to recieve ObjectInput\n" + e.getMessage());
			e.printStackTrace();
		} finally {
			String command = readLine();
			if (command.equals("request:done")) {
				return fileInfo;
			} else {
				Utils.logE("Server: Unexpected response from client: " + command);
			}
		}
		return null;
	}
	
	public void receiveFile(FileInfo fileInfo) {
		fileServer.getFile(fileInfo);
		if (readLine().matches("request:done")) {
			Utils.logD("Client and server ready for next file..");
		} else {
			Utils.logD("Client replied with something weird...");
		}
	}
	
	protected void command_request(String thing) {
		command_echo("request:" + thing);
	}
}
