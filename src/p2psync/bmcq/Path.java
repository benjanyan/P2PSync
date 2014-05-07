package p2psync.bmcq;

import java.io.File;
import java.io.Serializable;
import java.util.ArrayList;
import java.util.Iterator;

public class Path implements Serializable {
	/**
	 * An attempt to replicate some of Java 7's useful Path class.
	 * As we may be dealing with different OSs and sending this over sockets, representation of paths needs to remain consistent.
	 */
	private static final long serialVersionUID = -3968651610332770221L;
	private ArrayList<String> path = new ArrayList<String>();
	
	Path(String pathAsString) {
		this.path = new ArrayList<String>();
		String separator;
		
		if (File.separator.equals("\\")) {	//It is an escape character.
			separator = "\\\\";				//And it needs lots of escaping.
		} else {
			separator = File.separator;
		}
		
		String[] newPath = pathAsString.split(separator);
		
		for (String member : newPath) {
			this.path.add(member);
		}
		
		//Utils.logD("Created new Path object: " + toString());
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
	
	public String toString() {			//Return our path as a string in the local OSs format
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
	
	public Path getPathRelativeTo(Path otherPath) {				//Our sync paths on the two devices are probably in different places so we need to know stuff relative to that rather than the full path.
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
