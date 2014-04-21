package p2psync.bmcq;

import java.io.IOException;
import java.io.InputStream;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;
import java.io.OutputStream;
import java.net.InetAddress;
import java.net.Socket;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

public class Client {
	protected InetAddress host;
	protected int port;
	protected Socket socket;
	protected InputStream input;
	protected OutputStream output;
	protected String key;
	private boolean isConnected;

	
	Client(InetAddress host, int port) {
		this.host = host;
		this.port = port;
		this.isConnected = false;
	}
	

	public void connect() {
		Utils.logD("Client: Connecting to " + host + ":" + port + "...");
		try {
			socket = new Socket(host,port);
			input = socket.getInputStream();
			output = socket.getOutputStream();
			isConnected = true;
		} catch (IOException exception) {
			System.err.print("Client: Failed to connect to server: " + exception.getMessage());
			System.exit(1);
		}
	}
	
	protected void close() {
		try {
			input.close();
			output.close();
			socket.close();
			Utils.logD("Client: Closed connection to " + host + ":" + port);
		} catch (IOException ioe) {
			System.err.print("Client: Failed to close connection: " + ioe.getMessage());
		}
	}
	
	protected String readLine() {
		int characterBuffer = 512;
		int[] lineArray = new int[characterBuffer];
		int i = 0;
		int j = 0;
		int data;
		
		Utils.logD("Client: Awaiting server...");
		
		String line = "";
		
			//Read until new line character (10)
		try {
			while(i < lineArray.length) {
				data = input.read();
				if (data == 10) {
					while (j < i) {
						line += (char)lineArray[j];
						++j;
					}
					Utils.logD("Server->Me: " + line);
					return line;
				} else {
					lineArray[i] = data;
					++i;
				}
			}
		} catch (IOException ioe) {
			System.err.print("Client: Failed to read line from input buffer: " + ioe.getMessage());
			System.exit(1);
		}
		Utils.logE("Client: readLine() reached end of buffer before a new line character");
		System.exit(1);
		return "";		//Reached end of buffer without new line!
	}
	
	protected void setKey(String key) {
		this.key = key;
	}
	
	protected boolean isConnected() {
		return isConnected;
	}
	
	public void sendDirectoryInfo(FileInfo fileInfo) {
		OutputStream directoryInfoOutput = null;
		String command = readLine();
		directoryInfoOutput = output;
		
		if (command.equals("request:FileInfo") & fileInfo != null) {
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
		} else {
			Utils.logE("Couldn't send directoryInfo. Remote host is not ready or the directoryInfo is null");
		}
	}
	
	protected FileInfo getRemoteFileInfo() {
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
				if (fileInfo != null) {
					return fileInfo;
				} else {
					Utils.logE("Remote fileinfo is null!");
					System.exit(1);
				}
			} else {
				Utils.logE("Server: Unexpected response from client: " + command);
			}
		}
		return null;
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
	
	protected void command_key() {
		command_echo("key:" + key);
	}
	
	protected String getParam(String command, String regEx, int paramNo) {
		Pattern pattern = Pattern.compile(regEx);
		Matcher matcher = pattern.matcher(command);
		
		if (matcher.matches()) {
			return matcher.group(paramNo);
		} else {
			return "";
		}
	}
}