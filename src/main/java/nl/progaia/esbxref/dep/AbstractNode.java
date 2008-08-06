package nl.progaia.esbxref.dep;

import java.io.Serializable;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Iterator;
import java.util.List;

public class AbstractNode implements INode, Serializable {
	public static final long serialVersionUID = 1;
	
	private final String name;
	private final String path;
	
	private final List<INode> iUse = new ArrayList<INode>();
	private final List<INode> usedBy = new ArrayList<INode>();

	public AbstractNode(final String name, final String path) {
		this.name = name;
		this.path = path;
	}
	
	/* (non-Javadoc)
	 * @see nl.progaia.esbxref.dep.INode#getName()
	 */
	public String getName() {
		return name;
	}
	
	/* (non-Javadoc)
	 * @see nl.progaia.esbxref.dep.INode#getPath()
	 */
	public String getPath() {
		return path;
	}
	
	/* (non-Javadoc)
	 * @see nl.progaia.esbxref.dep.INode#addIUse(nl.progaia.esbxref.dep.INode)
	 */
	public void addIUse(INode other) {
		if(other.equals(this))
			return;
			
		if(!iUse.contains(other)) {
			iUse.add(other);
//			System.out.println(getPath() + " uses " + other.getPath());
		}
	}
	
	public boolean removeIUse(INode other) {
		return iUse.remove(other);
	}
	
	/* (non-Javadoc)
	 * @see nl.progaia.esbxref.dep.INode#getIUse()
	 */
	public List<INode> getIUse() {
		return Collections.unmodifiableList(iUse);
	}

	/* (non-Javadoc)
	 * @see nl.progaia.esbxref.dep.INode#addUsedBy(nl.progaia.esbxref.dep.INode)
	 */
	public void addUsedBy(INode other) {
		if(other.equals(this))
			return;
		
		if(!usedBy.contains(other)) {
			usedBy.add(other);
//			System.out.println(getPath() + " is used by " + other.getPath());
		}
	}
	
	/* (non-Javadoc)
	 * @see nl.progaia.esbxref.dep.INode#getUsedBy()
	 */
	public List<INode> getUsedBy() {
		return Collections.unmodifiableList(usedBy);
	}
	
	public boolean removeUsedBy(INode other) {
		return usedBy.remove(other);
	}
	
	/* (non-Javadoc)
	 * @see nl.progaia.esbxref.dep.INode#uses(nl.progaia.esbxref.dep.INode)
	 */
	public boolean uses(INode other) {
		return iUse.contains(other) || usesIndirect(other);
	}
	
	/* (non-Javadoc)
	 * @see nl.progaia.esbxref.dep.INode#usesIndirect(nl.progaia.esbxref.dep.INode)
	 */
	public boolean usesIndirect(INode other) {
		for(INode n: iUse) {
			if(n.uses(other))
				return true;
		}
		
		return false;
	}
	
	/* (non-Javadoc)
	 * @see nl.progaia.esbxref.dep.INode#usedBy(nl.progaia.esbxref.dep.INode)
	 */
	public boolean usedBy(INode other) {
		return usedBy.contains(other) || usedByIndirect(other);
	}
	
	/* (non-Javadoc)
	 * @see nl.progaia.esbxref.dep.INode#usedByIndirect(nl.progaia.esbxref.dep.INode)
	 */
	public boolean usedByIndirect(INode other) {
		for(INode n: usedBy) {
			if(n.usedBy(other))
				return true;
		}
		
		return false;
	}
	
	/* (non-Javadoc)
	 * @see nl.progaia.esbxref.dep.INode#compressLinks()
	 */
	public void compressLinks() {
		// Compress uses links
		for(Iterator<INode> it = iUse.iterator(); it.hasNext();) {
			INode directlyLinkedNode = it.next();
			
			if(usesIndirect(directlyLinkedNode)) {
				it.remove();
			}
		}
		
		// Compress usedby links
		for(Iterator<INode> it = usedBy.iterator(); it.hasNext();) {
			INode directlyLinkedNode = it.next();
			
			if(usedByIndirect(directlyLinkedNode)) {
				it.remove();
			}
		}
	}
	
	/* (non-Javadoc)
	 * @see nl.progaia.esbxref.dep.INode#equals(java.lang.Object)
	 */
	@Override
	public boolean equals(Object other) {
		return other instanceof INode
		    && ((INode)other).getPath().equals(getPath());
	}
	
	@Override
	public int hashCode() {
		return getPath().hashCode();
	}
	
	@Override
	public String toString() {
		return getName();
	}

	/* (non-Javadoc)
	 * @see nl.progaia.esbxref.dep.INode#compareTo(nl.progaia.esbxref.dep.INode)
	 */
	public int compareTo(INode other) {
		return getPath().compareTo(other.getPath());
	}
}
