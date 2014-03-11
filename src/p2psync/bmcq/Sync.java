package p2psync.bmcq;

import java.io.File;

public class Sync {
	private FileInfo syncInfo;
	private Path rootDirectory;
	private ControlServer control;
	
	Sync(FileInfo syncInfo, Path rootDirectory, ControlServer control) {
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
				delete(item);
			} else if (item.isIgnored()) {
				//Nothing
			} else if(item.isDirectory() && item.isModified()) {
				if ((new File(rootDirectory.toString() + File.separator + item.getPathAsString())).mkdir()) {
					executeSync(item);
					Utils.logD("Created folder..");
				} else {
					Utils.logE("Failed to create folder: " + rootDirectory.toString() + File.separator+ item.getPathAsString());
				}
			} else if (item.isModified()) {
				if (!item.getName().matches(".*.bmh")) {
					control.receiveFile(item);
				}
			}
		}
	}
	
	private void delete(FileInfo item) {
		File file = new File(rootDirectory.toString() + File.separator + item.getPathAsString());
		if (item.isDirectory() && file.listFiles() != null) {
			recursiveDelete(file.listFiles());
		}
		if (file.delete()) {
			Utils.logD("Deleted " + (rootDirectory.toString() + File.separator + item.getPathAsString()));
		} else {
			Utils.logE("Failed to delete " + (rootDirectory.toString() + File.separator + item.getPathAsString()));
		}
	}
	
	private void recursiveDelete(File[] files) {
		for (File file : files) {
			if (file.isDirectory()) {
				recursiveDelete(file.listFiles());
			} else {
				if (file.delete()) {
					Utils.logE("Failed to delete " + file.getPath());
				} else {
					Utils.logD("Deleted: " + file.getPath());
				}
				
			}
		}
	}
}
