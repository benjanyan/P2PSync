package p2psync.bmcq;

import java.io.File;
import java.io.Serializable;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.ArrayList;
import java.util.Iterator;

public class RelativePath implements Serializable {
	private ArrayList<String> path = new ArrayList<String>();
	private int memberCount;
	
	RelativePath(String... members) {
		memberCount = members.length;
		
		for(String member : members) {
			path.add(member);
		}
		
		Utils.logD("Created RelativePath from Array: " + toString());
	}
	
	RelativePath(Path path) {
		this.path = new ArrayList<String>();
		memberCount = path.getNameCount();
		Iterator<Path> pathIterator = path.iterator();
		Path member = null;
		
		while (pathIterator.hasNext()) {
			member = pathIterator.next();
			this.path.add(member.toString());
		}
		
		Utils.logD("Created RelativePath from Path: " + toString() + " (memberCount: " + memberCount + ")");
	}
	
	RelativePath() {
		memberCount = 0;
		path = new ArrayList<String>();
	}
	
	public int getNameCount() {
		return memberCount;
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
	
	public Path toPath() {
		return Paths.get(toString());
	}	
}
