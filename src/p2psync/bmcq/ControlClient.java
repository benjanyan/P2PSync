package p2psync.bmcq;

import java.io.File;
import java.io.IOException;
import java.io.InputStream;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;
import java.io.OutputStream;
import java.net.InetAddress;

public class ControlClient extends Client {
	
	protected FileClient fileClient;
	private String rootDirectory;
	private FileInfo rootFileInfo;
	
	ControlClient(InetAddress host, int port, String rootDirectory, FileInfo rootFileInfo) {
		super(host,port);
		fileClient = new FileClient(host,port + 1,this);
		this.rootDirectory = rootDirectory;
		this.rootFileInfo = rootFileInfo;
	}
	
	protected void control() {
		int state = 2;
		String command;
		
		while (state == 1) {
			command = readLine();
			Utils.logD("Incoming Command: " + command);
		}
		
		FileInfo fileInfo = null;
				
		
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
	
	protected void command_echo(String text) {
		Utils.logD("Me->Server: " + text);
		try {
			output.write((text + "\n").getBytes());
		} catch (IOException ioe) {
			System.err.print("Server->echo: Failed to write text to output - " + ioe.getMessage());
			System.exit(1);
		}
	}
	
	private void command_key() {
		command_echo("key:" + key);
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
}
