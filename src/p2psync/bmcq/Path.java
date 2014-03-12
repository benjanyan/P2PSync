package p2psync.bmcq;

import java.io.File;
import java.io.Serializable;
import java.util.ArrayList;
import java.util.Iterator;

public class Path implements Serializable {
	/**
	 * 
	 */
	private static final long serialVersionUID = -3968651610332770221L;
	private ArrayList<String> path = new ArrayList<String>();
	
	Path(String pathAsString) {
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
		
		Utils.logD("Created new Path object: " + toString());
	}
	
	Path() {
		path = new ArrayList<String>();
	}
	
	Path(ArrayList<String> newPath) {
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
	
	public Path getPathRelativeTo(Path otherPath) {
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
		return new Path(newPath);		
	}
}
