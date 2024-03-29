package p2psync.bmcq;

import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;

public class FileServer extends Server {
	
	private FileOutputStream fileOutput;
	private ControlServer controlServer;
	

	FileServer(int port, ControlServer controlServer, Path rootDirectory) {
		super(port, rootDirectory);
		this.controlServer = controlServer;
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
		
		if (!isConnected()) {		//The socket may already be connected from a previous transfer
			listen();
		}
				
		try {
			localFile = new File(rootDirectory + File.separator + fileInfo.getPath());
			fileOutput = new FileOutputStream(localFile);
			
			while (received < fileInfo.getLength()) {
				read = input.read(data);
				fileOutput.write(data,0,read);
				received += read;
			}
			fileOutput.close();
			localFile.setLastModified(fileInfo.getModifiedDate());		//preserve the original's modified date! Otherwise things get confusing and it'll be flagged as new/modified on the next run.
						
			if (received != fileInfo.getLength()) {					//A bit of debugging code.
				Utils.logE("Received file size differs for " + fileInfo.getName() + " [" + received + "/" + fileInfo.getLength() + "]");
			}
			
			Utils.logD("FileServer: Wrote " + fileInfo.getPath());		
		} catch (IOException ioe) {
			Utils.logE("FileServer: Failed to retrieve file: " + ioe.getMessage());
			System.exit(1);
		}
	}

}
