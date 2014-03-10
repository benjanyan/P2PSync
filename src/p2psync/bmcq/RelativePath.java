package p2psync.bmcq;

import java.io.File;
import java.io.Serializable;
import java.util.ArrayList;
import java.util.Iterator;

public class RelativePath implements Serializable {
	/**
	 * 
	 */
	private static final long serialVersionUID = -3968651610332770221L;
	private ArrayList<String> path = new ArrayList<String>();
	
	RelativePath(String pathAsString) {
		this.path = new ArrayList<String>();
		String separator;
		
		if (File.separator.equals("\\")) {
			separator = "\\\\";
		} else {
			separator = File.separator;
		}
		
		String[] newPath = pathAsString.split(separator);
		
		for (String member : newPath) {
			this.path.add(member);
		}
	}
	
	RelativePath(String... members) {
		for(String member : members) {
			path.add(member);
		}
		
		//Utils.logD("Created RelativePath from Array: " + toString());
	}
	
	RelativePath() {
		path = new ArrayList<String>();
	}
	
	RelativePath(ArrayList<String> newPath) {
		path = newPath;
	}
	
	public String getMember(int index) {
		return path.get(index);
	}
	
	public int getNameCount() {
		return this.path.size();
	}
	
	public String toString() {
		String pathString = "";
		Iterator<String> pathIterator = path.iterator();
		String member = null;
		while(pathIterator.hasNext()) {
			member = pathIterator.next();
			pathString = pathString + member;
			if (pathIterator.hasNext()) {
				pathString = pathString + File.separator;
			}
		}
		return pathString;
	}
	
	public RelativePath getPathRelativeTo(RelativePath otherPath) {
		ArrayList<String> newPath = new ArrayList<String>();
		int i = 0;
		
		while (i < this.getNameCount() && i < otherPath.getNameCount()) {
			if (!this.getMember(i).equals(otherPath.getMember(i))) {
				newPath.add(otherPath.getMember(i));
			}
			++i;
		}
		while (i < this.getNameCount()) {
			newPath.add(this.getMember(i));
			++i;
		}		
		return new RelativePath(newPath);		
	}
}
