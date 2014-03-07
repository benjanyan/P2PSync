package p2psync.bmcq;

import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.net.Inet4Address;
import java.net.InetAddress;
import java.net.ServerSocket;
import java.net.Socket;

public class Server {
	private ServerSocket serverSocket;
	protected Socket socket;
	protected int port;
	protected InputStream input;
	protected OutputStream output;
	protected String description;
	protected String key;
	private InetAddress clientAddress;
	
	Server(int port) {
		this.port = port;
		try {
			serverSocket = new ServerSocket(port);
		} catch (IOException ioe) {
			System.err.print(description + ": failed to create ServerSocket: " + ioe.getMessage());
		}
	}
	
	public void listen() {
		Utils.logD(description + ": listening on port " + port);
		try {
			socket = serverSocket.accept();
			input = socket.getInputStream();
			output = socket.getOutputStream();
			
			clientAddress = socket.getInetAddress();
			
		} catch (IOException exception) {
			System.err.print(description + " failed to listen and accept connection: " + exception.getMessage());
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
}
