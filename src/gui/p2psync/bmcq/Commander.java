package gui.p2psync.bmcq;

import java.io.File;
import java.io.IOException;
import java.nio.file.Path;
import java.nio.file.Paths;

import p2psync.bmcq.ControlClient;
import p2psync.bmcq.ControlServer;
import p2psync.bmcq.FileInfo;

public class Commander {
	ControlServer controlServer;
	ControlClient controlClient;
	FileInfo localFileInfo;
	Path localSyncPath;
	
	Commander() {
		
	}
	
	public void setLocalSyncPath(String localSyncPath) throws IOException, NullPointerException {
		this.localSyncPath = Paths.get(localSyncPath);
		File test = new File(this.localSyncPath.toString());
		if (!test.exists()) {
			throw new IOException();
		}
	}
	
	public void scanLocalSyncDirectory() {
		
	}
	
	public void startControlServer() throws NullPointerException {
		
	}
	
}
