package nl.progaia.esbxref.dep;

import java.util.List;

public interface INode extends Comparable<INode>{

	public abstract String getName();

	public abstract String getPath();

	public abstract void addOutgoing(Link link);
	
	public abstract boolean removeOutgoing(Link link);

	public abstract List<Link> getOutgoing();
	
	public abstract List<INode> getOutgoingTargets();

	public abstract void addIncoming(Link link);

	public abstract boolean removeIncoming(Link link);
	
	public abstract List<Link> getIncoming();
	
	public abstract List<INode> getIncomingSources();

	public abstract boolean uses(INode other);

	public abstract boolean usesIndirect(INode other);

	public abstract boolean usedBy(INode other);

	public abstract boolean usedByIndirect(INode other);

	public abstract boolean unusedByAll(List<INode> result);
	
	public abstract void compressLinks();

	public abstract boolean equals(Object other);

}