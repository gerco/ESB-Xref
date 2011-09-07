package nl.progaia.esbxref.dep;

import java.io.Serializable;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Iterator;
import java.util.List;

public abstract class AbstractNode implements INode, Serializable {
	public static final long serialVersionUID = 1;
	
	private final String name;
	private final String path;
	
	private final List<Link> outgoingLinks = new ArrayList<Link>();
	private final List<Link> incomingLinks = new ArrayList<Link>();

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
	public void addOutgoing(Link link) {
		assert link.getTarget() != this;
		assert link.getSource() == this;
			
		if(!outgoingLinks.contains(link)) {
			outgoingLinks.add(link);
		}
	}
	
	public boolean removeOutgoing(Link link) {
		return outgoingLinks.remove(link);
	}
	
	/* (non-Javadoc)
	 * @see nl.progaia.esbxref.dep.INode#getIUse()
	 */
	public List<Link> getOutgoing() {
		return Collections.unmodifiableList(outgoingLinks);
	}
	
	public List<INode> getOutgoingTargets() {
		List<INode> result = new ArrayList<INode>();
		for(Link l: outgoingLinks)
			result.add(l.getTarget());
		return Collections.unmodifiableList(result);
	}
	
	/* (non-Javadoc)
	 * @see nl.progaia.esbxref.dep.INode#addUsedBy(nl.progaia.esbxref.dep.INode)
	 */
	public void addIncoming(Link link) {
		assert link.getTarget() == this;
		assert link.getSource() != this;
		
		if(!incomingLinks.contains(link)) {
			incomingLinks.add(link);
		}
	}
	
	/* (non-Javadoc)
	 * @see nl.progaia.esbxref.dep.INode#getUsedBy()
	 */
	public List<Link> getIncoming() {
		return Collections.unmodifiableList(incomingLinks);
	}
	
	public List<INode> getIncomingSources() {
		List<INode> result = new ArrayList<INode>();
		for(Link l: incomingLinks)
			result.add(l.getSource());
		return Collections.unmodifiableList(result);
	}
	
	public boolean removeIncoming(Link link) {
		return incomingLinks.remove(link);
	}
	
	/* (non-Javadoc)
	 * @see nl.progaia.esbxref.dep.INode#uses(nl.progaia.esbxref.dep.INode)
	 */
	public boolean uses(INode other) {
//		System.out.println(this.getPath() + " uses(" + other.getPath() + ")");
		for(Link l: outgoingLinks) {
			if(l.getTarget() == other)
				return true;
		}
		
		return usesIndirect(other);
	}
	
	/* (non-Javadoc)
	 * @see nl.progaia.esbxref.dep.INode#usesIndirect(nl.progaia.esbxref.dep.INode)
	 */
	public boolean usesIndirect(INode other) {
//		System.out.println(this.getPath() + " usesIndirect(" + other.getPath() + ")");
		for(Link l: outgoingLinks)
			if(l.getTarget().uses(other))
				return true;
		
		return false;
	}
	
	/* (non-Javadoc)
	 * @see nl.progaia.esbxref.dep.INode#usedBy(nl.progaia.esbxref.dep.INode)
	 */
	public boolean usedBy(INode other) {
		for(Link l: incomingLinks)
			if(l.getSource() == other)
				return true;
		
		return usedByIndirect(other);
	}
	
	/* (non-Javadoc)
	 * @see nl.progaia.esbxref.dep.INode#usedByIndirect(nl.progaia.esbxref.dep.INode)
	 */
	public boolean usedByIndirect(INode other) {
		for(Link l: incomingLinks) {
			if(l.getSource().usedBy(other))
				return true;
		}
		
		return false;
	}
	
	public boolean unusedByAll(List<INode> nodes) {
		for(Link l: incomingLinks)
			if(!nodes.contains(l.getSource()))
				return false;
		return true;
	}
	
	/* (non-Javadoc)
	 * @see nl.progaia.esbxref.dep.INode#compressLinks()
	 */
	public void compressLinks() {
		// Compress outgoing links
		for(Iterator<Link> it = outgoingLinks.iterator(); it.hasNext();) {
			Link directLink = it.next();
			if(directLink.isHard())
				continue;
			
			if(usesIndirect(directLink.getTarget()))
				it.remove();
		}
		
		// Compress incoming links
		for(Iterator<Link> it = incomingLinks.iterator(); it.hasNext();) {
			Link directLink = it.next();
			if(directLink.isHard())
				continue;
			
			if(usedByIndirect(directLink.getSource()))
				it.remove();
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
