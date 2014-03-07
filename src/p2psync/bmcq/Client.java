package p2psync.bmcq;

import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.net.InetAddress;
import java.net.Socket;

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

}