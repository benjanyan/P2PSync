package p2psync.bmcq;

import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;

public class FileServer extends Server {
	
	private FileOutputStream fileOutput;
	protected InputStream fileInput;
	private ControlServer controlServer;
	protected RelativePath rootDirectory;
	

	FileServer(int port, ControlServer controlServer, RelativePath rootDirectory) {
		super(port);
		this.controlServer = controlServer;
		this.rootDirectory = rootDirectory;
		this.description = "File server";
	}
	
	protected void getFile(FileInfo fileInfo) {
		int bufferSize = 4096;
		byte[] data = new byte[bufferSize];
		int read = 0;
		int received = 0;
		File localFile;
		
		controlServer.command_request("file:" + fileInfo.getId());
		Utils.logD("FileServer: Awaiting file...");
		
		if (!isConnected()) {
			listen();
			try {
				fileInput = socket.getInputStream();
			} catch (IOException ioe) {
				Utils.logE("FileServer: Failed to create file input stream: " + ioe.getMessage());
				System.exit(1);
			}
		}
				
		try {
			localFile = new File(rootDirectory + File.separator + fileInfo.getPath());
			fileOutput = new FileOutputStream(localFile);
			
			while (received < fileInfo.getLength()) {
				read = fileInput.read(data);
				fileOutput.write(data,0,read);
				received += read;
			}
			fileOutput.close();
			localFile.setLastModified(fileInfo.getModifiedDate());
						
			if (received != fileInfo.getLength()) {
				Utils.logE("Received file size differs for " + fileInfo.getName() + " [" + received + "/" + fileInfo.getLength() + "]");
			}
			
			Utils.logD("FileServer: Wrote " + fileInfo.getPath());		
		} catch (IOException ioe) {
			Utils.logE("FileServer: Failed to retrieve file: " + ioe.getMessage());
			System.exit(1);
		}
	}

}
