package p2psync.bmcq;

import java.io.IOException;
import java.io.InputStream;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;
import java.io.OutputStream;
import java.net.InetAddress;
import java.net.ServerSocket;
import java.net.Socket;
import java.net.UnknownHostException;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

public class Server {
	private ServerSocket serverSocket;
	protected Socket socket;
	protected int port;
	protected InputStream input;
	protected OutputStream output;
	protected String description;
	protected String key;
	private InetAddress clientAddress;
	protected Path rootDirectory;
	public ServerIP serverIp;
	
	Server(int port, Path rootDirectory) {
		this.port = port;
		this.rootDirectory = rootDirectory;
		serverIp = null;
	}
	
	public void listen() {
		if (isConnected()) {
			close();
		}
		
		try {
			serverSocket = new ServerSocket(port);
			Utils.logD(description + ": attempting to listen on port " + port);
			socket = serverSocket.accept();
			input = socket.getInputStream();
			output = socket.getOutputStream();
			
			clientAddress = socket.getInetAddress();
			
		} catch (IOException exception) {
			System.err.print(description + " failed to create socket, listen and accept connection: " + exception.getMessage());
		} finally {
			Utils.logD(description + ": Client " + socket.getRemoteSocketAddress() + " is connected!");
		}
	}
	
	public void close() {
		try {
			if (isConnected()) {
				input.close();
				output.close();
				socket.close();
				serverSocket.close();
				Utils.logD(description + " closed on port " + port);
			}
		} catch (IOException ioe) {
			System.err.print(description + " failed to close: " + ioe.getMessage());
		}
	}
	
	public void informMasterServer() {
		ServerDiscover serverDiscover = new ServerDiscover();
		
		try {
			serverIp = new ServerIP(InetAddress.getLocalHost());
		} catch (UnknownHostException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
		serverDiscover.setServerIP(serverIp);
	}
	
	
	protected void setKey(String key) {
		this.key = key;
		Utils.logD(description + " key set to: " + key);
	}
	
	protected String readLine() {
		int characterBuffer = 512;
		int[] lineArray = new int[characterBuffer];
		int i = 0;
		int j = 0;
		int data;
		
		Utils.logD("Server: Awaiting client...");
		
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
					Utils.logD("Client->Me: " + line);
					return line;
				} else {
					lineArray[i] = data;
					++i;
				}
			}
		} catch (IOException ioe) {
			System.err.print("Server: Failed to read line from input buffer: " + ioe.getMessage());
			System.exit(1);
		}
		Utils.logE("Server: readLine() reached end of buffer before a new line character");
		System.exit(1);		//Reached end of buffer without new line! Bail out!
		return "";
	}
	
	protected boolean isConnected() {
		return socket != null;
	}
	
	public InetAddress getClientAddress() {
		return clientAddress;
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
	
	public FileInfo getRemoteFileInfo() {
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
	
	protected void command_request(String thing) {
		command_echo("request:" + thing);
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
