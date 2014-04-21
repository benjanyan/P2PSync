package p2psync.bmcq;

import java.io.FileInputStream;
import java.io.IOException;
import java.net.InetAddress;
import java.util.regex.Matcher;
import java.util.regex.Pattern;


public class FileClient extends Client {

	private ControlClient control;

	FileClient(InetAddress host, int port, ControlClient control) {
		super(host,port);
		this.control = control;
	}
	
	public void control(FileInfo fileInfo) {
		String command = control.readLine();
		String fileInfoId;
		
		if (command.matches("request:file:.*")) {
			do {
				fileInfoId = getParam(command,"request:file:(.*)",1);
				sendFile(fileInfo.getChildById(fileInfoId));
				
				command = control.readLine();
			} while (command.matches("request:file:.*"));
		}
	}
	
	public void sendFile(FileInfo fileInfo) {
		FileInputStream fileInput;
		int bufferSize = 4096;
		byte[] buffer = new byte[bufferSize];
		int read = 0;
		
		try {
			fileInput = new FileInputStream(fileInfo.getFile());
			if (!isConnected()) {
				connect();
			}
			
			while ((read = fileInput.read(buffer)) != -1) {
				output.write(buffer, 0, read);
				//sent += read;
			}
			
			output.flush();
			control.command_echo("request:done");
			fileInput.close();
		} catch (IOException ioe) {
			Utils.logE("FileClient->sendFile(): " + ioe.getMessage());
			System.exit(1);
		}
	}
	
}
