package nl.progaia.esbxref;

import java.io.Serializable;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Iterator;
import java.util.List;

import com.sonicsw.deploy.IArtifact;

public class Node implements Comparable<Node>, Serializable {
	public static final long serialVersionUID = 1;
	
	private final String name;
	private final String path;
	
	private final List<Node> iUse = new ArrayList<Node>();
	private final List<Node> usedBy = new ArrayList<Node>();
	
	public Node(IArtifact artifact) {
		super();
		name = artifact.getName();
		path = artifact.getArchivePath();
	}

	public String getName() {
		return name;
	}
	
	public String getPath() {
		return path;
	}
	
	public void addIUse(Node other) {
		if(other.equals(this))
			return;
			
		if(!iUse.contains(other)) {
			iUse.add(other);
//			System.out.println(getPath() + " uses " + other.getPath());
		}
	}
	
	public List<Node> getIUse() {
		return Collections.unmodifiableList(iUse);
	}

	public void addUsedBy(Node other) {
		if(other.equals(this))
			return;
		
		if(!usedBy.contains(other)) {
			usedBy.add(other);
//			System.out.println(getPath() + " is used by " + other.getPath());
		}
	}
	
	public List<Node> getUsedBy() {
		return Collections.unmodifiableList(usedBy);
	}
	
	public boolean uses(Node other) {
		return iUse.contains(other) || usesIndirect(other);
	}
	
	public boolean usesIndirect(Node other) {
		for(Node n: iUse) {
			if(n.uses(other))
				return true;
		}
		
		return false;
	}
	
	public boolean usedBy(Node other) {
		return usedBy.contains(other) || usedByIndirect(other);
	}
	
	public boolean usedByIndirect(Node other) {
		for(Node n: usedBy) {
			if(n.usedBy(other))
				return true;
		}
		
		return false;
	}
	
	public void compressLinks() {
		// Compress uses links
		for(Iterator<Node> it = iUse.iterator(); it.hasNext();) {
			Node directlyLinkedNode = it.next();
			
			if(usesIndirect(directlyLinkedNode)) {
				it.remove();
			}
		}
		
		// Compress usedby links
		for(Iterator<Node> it = usedBy.iterator(); it.hasNext();) {
			Node directlyLinkedNode = it.next();
			
			if(usedByIndirect(directlyLinkedNode)) {
				it.remove();
			}
		}
	}
	
	@Override
	public boolean equals(Object other) {
		return other instanceof Node
		    && ((Node)other).getPath().equals(getPath());
	}
	
	@Override
	public int hashCode() {
		return getPath().hashCode();
	}
	
	@Override
	public String toString() {
		return getName();
	}

	public int compareTo(Node other) {
		return getPath().compareTo(other.getPath());
	}
}
