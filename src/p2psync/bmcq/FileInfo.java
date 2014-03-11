package p2psync.bmcq;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;
import java.io.Serializable;
import java.util.ArrayList;
import java.util.HashMap;

public class FileInfo extends SyncInfo implements Serializable {
	/**
	 * 
	 */
	private static final long serialVersionUID = -8865725152813983428L;
	private String name;		//File's name
	private Path path;		//The path relative to the root directory
	private Long modifiedDate;
	private Long length;		//Size in bytes
	private String id;
	
	private File file;
	private FileInfo parent;	//(null for root)
	private FileInfo[] children;
	private HashMap<String, FileInfo> completeHashMap;
	private boolean directory;	//True if directory rather than a file
	
	private String localRootPath;
	
	FileInfo(File file, FileInfo parent) {
		super();
		this.file = file;
		this.parent = parent;
		completeHashMap = new HashMap<String,FileInfo>();
		constructFromFile();
	}
	
	FileInfo(String name, Path path, Long modifiedDate, Long length, FileInfo parent) {	//Constructor for File
		super();
		this.name = name;
		this.path = path;
		this.modifiedDate = modifiedDate;
		this.length = length;
		this.parent = parent;
		this.id = modifiedDate.toString() + name.length() + path.getNameCount() + length.toString();
		children = null;
		directory = false;
		completeHashMap = null;
	}
	
	FileInfo(String name, Path path, Long modifiedDate, FileInfo parent, int childrenLength) {	//Constructor for Directory
		super();
		this.name = name;
		this.path = path;
		this.modifiedDate = modifiedDate;
		this.length = (long) 0;	//NA
		this.id = modifiedDate.toString() + name.length() + path.getNameCount() + length.toString();
		children = new FileInfo[childrenLength];
		directory = true;
	}
	
	public void constructFromFile() {
		name = file.getName();
		if (parent == null) {
			localRootPath = file.getPath();
		} else {
			localRootPath = parent.getLocalRootPath();
		}
		modifiedDate = file.lastModified();
		path = getPathRelativeToRoot();
		this.length = (long) 0;
		
		if (file.isDirectory()) {
			File[] childFiles = file.listFiles();
			directory = true;
			children = new FileInfo[childFiles.length];
			for (int i = 0; i < childFiles.length; ++i) {
				children[i] = new FileInfo(childFiles[i],this);
			}
		} else {
			this.length = file.length();
		}
		this.id = modifiedDate.toString() + name.length() + path.getNameCount() + length.toString();
	}
	
	
	public void addChild(FileInfo child) {								//Add a child FileInfo to the end of the children array
		FileInfo[] newChildren = new FileInfo[children.length + 1];		//New array of existing size + 1
		for (int i = 0; i < children.length; ++i) {						//Copy current children to new array
			newChildren[i] = children[i];
		}
		newChildren[children.length] = child;							//Add new child onto the end of new array
		children = newChildren;											//Replace old array
	}
	
	public void addChildren(FileInfo[] children) {										//Add an array of multiple children FileInfo to the end of the children array
		if (!directory) {
			FileInfo[] newChildren = new FileInfo[this.children.length + children.length];	//New array of existing size + additional children size
			for (int i = 0; i < this.children.length; ++i) {								//Copy current children to new array
				newChildren[i] = this.children[i];
			}
			for (int i = 0; i < children.length; ++i) {										//Copy additional children to new array
				newChildren[this.children.length + i] = children[i];
			}
			children = newChildren;															//Replace old array
		} else {
			Utils.logE("Attempt made to add children to a FileInfo object that isn't a directory. Ignored.");
		}
	}
	
	public boolean isNewerOrEqual(FileInfo otherFile) {
		return modifiedDate >= otherFile.modifiedDate;
	}
	
	public void associatedFile(File file) {
		this.file = file;
	}
	
	public void deassociatedFile(){
		this.file = null;
	}
	
	public File getAssociatedFile(String localRootPath) {
		if (file == null) {
			return new File(localRootPath + File.separator + getPath());
		} else {
			return file;
		}
	}
	
	public void export() {																			//Export serialized object for next sync
		String path = ".lastRun.bmh";
		try {
			File file = new File(localRootPath + File.separator + path);
			ObjectOutputStream fileOutput = new ObjectOutputStream(new FileOutputStream(file));
			forgetDeleted();
			resetFlags();
			fileOutput.writeObject(this);
			fileOutput.close();
			Utils.logD("Exported FileInfo to " + localRootPath);
		} catch (IOException exception) {
			Utils.logE("Failed to export DirectoryInfo");
			exception.printStackTrace();
		}
	}
	
	public FileInfo getPreviousFileInfo() {												//Load serialized object from last sync
		FileInfo importedFileInfo;
		try {
			File file = new File(localRootPath + File.separator + ".lastRun.bmh");
			if (file.exists()) {
				ObjectInputStream fileInput = new ObjectInputStream(new FileInputStream(file));
				importedFileInfo = (FileInfo) fileInput.readObject();
				fileInput.close();
				return importedFileInfo;
			} else {
				return null;
			}
		} catch (IOException exception) {
			Utils.logE("Failed to import DirectoryInfo - IO error");
			exception.printStackTrace();
			System.exit(1);
		} catch (ClassNotFoundException exception) {
			Utils.logE("Failed to import DirectoryInfo - Object input stream error");
			exception.printStackTrace();
			System.exit(1);
		}
		return null;
	}
	
	public void setFlags() {
		FileInfo previous = getPreviousFileInfo();
		setFlags(previous);
	}
	
	public void setFlags(FileInfo previous) {
		if (previous != null) {
			HashMap<String, FileInfo> current = getChildrenAsHashMap();
			FileInfo matchedFileInfo;
			ArrayList<FileInfo> deletedFileInfo = new ArrayList<FileInfo>();
			
			for (FileInfo pfi : previous.getChildren()) {								//Compare all previous children to now
				matchedFileInfo = current.get(pfi.getPathAsString());
				if (!pfi.isDirectory()) {
					if (matchedFileInfo != null) {
						matchedFileInfo.setAnalysed(true);
						if (matchedFileInfo.isNewer(pfi)) {
							matchedFileInfo.setModifiedAndParents(true);
						} else {
							matchedFileInfo.setModified(false);
						}
					} else {
						pfi.setDeleted(true);											//Didn't find the previous file, must've been deleted
						deletedFileInfo.add(pfi);
					}
				} else {
					if (matchedFileInfo != null) {
						matchedFileInfo.setAnalysed(true);
						matchedFileInfo.setFlags(pfi);
					} else {
						pfi.setDeleted(true);											//Didn't find the current file, must've been deleted
						deletedFileInfo.add(pfi);
					}
				}
				
				
			}
			
			for (FileInfo dfi : deletedFileInfo) {							//Quite intensive?
				addChild(dfi);
			}
			
			for (FileInfo cfi : getChildren()) {										//If they weren't analysed by the loop above, they're new files.
				if (!cfi.isAnalysed() && !cfi.isDeleted()) {
					cfi.setModified(true);
					cfi.setModifiedAndParents(true);
					if (cfi.isDirectory()) {
						cfi.setFlags(null);
					}
				}
			}
		} else {																//No previous data, we'll assume it's all new.
			for (FileInfo cfi : getChildren()) {
				cfi.setModifiedAndParents(true);
				if (cfi.isDirectory()) {
					cfi.setFlags(null);
				}
			}
		}
	}
	

	
		//INTERNAL
	protected FileInfo getRoot() {
		if (getParent() != null) {
			return getParent().getRoot();
		} else {
			return this;
		}
	}
	
	private Path getPathRelativeToRoot() {
		if (parent != null && parent.getParent() != null) {
			Path fullPath = new Path(file.getPath());
			Path rootPath = new Path(getRoot().getLocalRootPath());
			return fullPath.getPathRelativeTo(rootPath);
		} else {
			if (parent == null) {
				return new Path();
			} else {
				return new Path(name);
			}
		}
	}
	
	private boolean isNewer(FileInfo otherFileInfo) {
		return otherFileInfo.getModifiedDate() < modifiedDate;
	}
	
	public void refreshHashMap() {
		if (parent == null) {
			//Utils.logD("Rebuilding HashMap...");
			completeHashMap = new HashMap<String,FileInfo>();
			refreshHashMap(completeHashMap);
		} else {
			Utils.logE("Public call on refreshHashMap() for a non-root FileInfo!");
		}
	}
	
	private void refreshHashMap(HashMap<String,FileInfo> rootHashMap) {
		for (FileInfo child : getChildren()) {
			if (child != null) {
				rootHashMap.put(child.getId(), child);
				if (child.isDirectory()) {
					child.refreshHashMap(rootHashMap);
				}
			}
		}
	}
	
	
		//HELPER METHODS
	public void printContents(String indent) {
		if (this.isDirectory()) {
			for(FileInfo child : children) {
				if (child.isDirectory()) {
					System.out.print(indent + "+" + child.getName());
				} else {
					System.out.print(indent + child.getPathAsString());
				}
				if (child.isModified()) {
					System.out.print(" (Modified/New) ");
				}
				if (child.isDeleted()) {
					System.out.print(" (Deleted) ");
				}
				if (child.isIgnored()) {
					System.out.print(" (Ignored) ");
				}
				if (child.isConflicted()) {
					System.out.print(" (Conflicted)");
				}
				System.out.print("\n");
				if (child.isDirectory()) {
					child.printContents(indent + "\t");
				}
			}
		}
	}

	
	public void printContents() {
		printContents("");
	}
	
	public void detectConflicts(FileInfo otherFileInfos) {
		FileInfo matchedFileInfo = null;
		HashMap<String,FileInfo> children = getChildrenAsHashMap();
		for (FileInfo ofi : otherFileInfos.getChildren()) {
			if (!ofi.isModified()) {
				matchedFileInfo = children.get(ofi.getPathAsString());
				if (matchedFileInfo != null && !matchedFileInfo.isDirectory() && matchedFileInfo.detectConflict(ofi)) {
					matchedFileInfo.setConflicted(true);
					Utils.logD(matchedFileInfo.getName() + " and " + ofi.getName() + " have both changed since the last sync!!!!");
				}
			}
		}
	}
	
	private boolean detectConflict(FileInfo otherFileInfo) {
		if (isModified() && otherFileInfo.isModified()) {
			return true;
		}
		if (isModified() && otherFileInfo.isDeleted()) {
			return true;
		}
		
		return false;
	}
	
	
	public HashMap<String, FileInfo> getChildrenAsHashMap() {
		HashMap<String, FileInfo> childrenHM = new HashMap<String, FileInfo>();
		
		for (FileInfo child : children) {
			childrenHM.put(child.getPathAsString(), child);
		}
		
		return childrenHM;
	}
	
	public void forgetDeleted() {
		forgetDeleted(this);
	}
	
	public void forgetDeleted(FileInfo fileInfo) {
		ArrayList<FileInfo> newChildren = new ArrayList<FileInfo>();
		
		for (FileInfo child : fileInfo.getChildren()) {
			if (!child.isDeleted()) {
				newChildren.add(child);
			}
			if (!child.isDeleted() && child.isDirectory()) {
				forgetDeleted(child);
			}
		}
		
		FileInfo[] newChildrenArray = newChildren.toArray(new FileInfo[newChildren.size()]);
		fileInfo.setChildren(newChildrenArray);
	}
	
	public void resetFlags() {
		resetFlags(this);
	}
	
	public void resetFlags(FileInfo fileInfo) {
		for (FileInfo child : fileInfo.getChildren()) {
			child.setAnalysed(false);
			child.setConflicted(false);
			child.setDeleted(false);
			child.setIgnored(false);
			child.setModified(false);
			if (child.isDirectory()) {
				resetFlags(child);
			}
		}
	}
	
	


	
		//ACCESSOR METHODS
	public String getName() {
		return name;
	}
	
	public Path getPath() {
		return path;
	}
	
	public String getPathAsString() {
		return path.toString();
	}
	
	public long getModifiedDate() {
		return modifiedDate;
	}
	
	public long getLength() {
		return length;
	}
	
	public FileInfo getParent() {
		return parent;
	}
	
	public File getFile() {
		return file;
	}
	
	public String getLocalRootPath() {
		return localRootPath;
	}
	
	public boolean isDirectory() {
		return directory;
	}
	
	public FileInfo[] getChildren() {
		return children;
	}
	
	public String getId() {
		return id;
	}
	
	public FileInfo getChildById(String id) {
		FileInfo child = completeHashMap.get(id);
		if (child != null) {
			return child;
		} else {
			Utils.logE("Unablen to find child FileInfo by if of \"" + id + "\"");
			System.exit(1);
			return null;
		}
	}
	
	public void setChildren(FileInfo[] newChildren) {
		children = newChildren;
	}
	
	public void setLength(long length) {
		this.length = (Long) length;
	}
	
	public void setId(String id) {
		this.id = id;
	}
	
	public void setModifiedDate(Long modified) {
		modifiedDate = modified;
	}
	
	public FileInfo[] getTransferFiles() {
		ArrayList<FileInfo> transferFiles = getTransferFiles(new ArrayList<FileInfo>());
		return transferFiles.toArray(new FileInfo[transferFiles.size()]);
	}
	
	public ArrayList<FileInfo> getTransferFiles(ArrayList<FileInfo> transferFiles) {
		for (FileInfo child : getChildren()) {
			if (!child.isDirectory()) {
				if (child.isModified()) {
					transferFiles.add(child);
				}
			} else {
				getTransferFiles(transferFiles);
			}
		}
		
		return transferFiles;
	}
	
	public void setLocalRootPath(String localRootPath) {
		this.localRootPath = localRootPath;
	}
	
	private void setModifiedAndParents(boolean modified) {
		setModified(modified);
		if (getParent() != null) {
			setModifiedAndParents(modified);
		}
	}
}