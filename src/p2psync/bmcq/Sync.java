package p2psync.bmcq;

import java.io.File;
import java.io.IOException;

public class Sync {
	private FileInfo syncInfo;
	private String rootDirectory;
	private ControlServer control;
	
	Sync(FileInfo syncInfo, String rootDirectory, ControlServer control) {
		this.syncInfo = syncInfo;
		this.rootDirectory = rootDirectory;
		this.control = control;
	}
	
	public void executeSync() {
		executeSync(syncInfo);
	}
	
	private void executeSync(FileInfo currentDir) {
		for (FileInfo item : currentDir.getChildren()) {
			if (item.isDeleted()) {
				item.deassociatedFile();
				item.getAssociatedFile(rootDirectory).delete();
				Utils.logD("Deleted " + rootDirectory + item.getPath());
			} else if (item.isIgnored()) {
				//Nothing
			} else if (item.isModified()) {
				if (!item.getName().matches(".*.bmh")) {
					control.receiveFile(item);
				}
			} else if(item.isDirectory()) {
				(new File(rootDirectory + item.getPath() + item.getName())).mkdir();
				executeSync(item);
			}
		}
	}
}
