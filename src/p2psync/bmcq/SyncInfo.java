package p2psync.bmcq;

import java.io.Serializable;

public class SyncInfo implements Serializable {
	/**
	 * Contains our flags. Only 3 are really used as of today; modified, deleted and analysed.
	 */
	private static final long serialVersionUID = 4130923471999661699L;
	private boolean modified;
	private boolean deleted;
	private boolean conflicted;
	private boolean ignored;
	private boolean analysed;
	
	SyncInfo() {
		modified = false;
		deleted = false;
		conflicted = false;
		ignored = false;
		analysed = false;
	}
	
	public void setModified(boolean modified) {
		this.modified = modified;
	}
	
	public void setDeleted(boolean deleted) {
		this.deleted = deleted;
	}
	
	public void setConflicted(boolean conflicted) {
		this.conflicted = conflicted;
	}
	
	public void setIgnored(boolean ignored) {
		this.ignored = ignored;
	}
	
	public void setAnalysed(boolean analysed) {
		this.analysed = analysed;
	}
	
	public boolean isModified() {
		return modified;
	}
	
	public boolean isDeleted() {
		return deleted;
	}
	
	public boolean isConflicted() {
		return conflicted;
	}
	
	public boolean isIgnored() {
		return ignored;
	}
	
	public boolean isAnalysed() {
		return analysed;
	}
}
