package nl.progaia.esbxref.dep;

import java.util.List;

public interface INode extends Comparable<INode>{

	public abstract String getName();

	public abstract String getPath();

	public abstract void addIUse(INode other);
	
	public abstract boolean removeIUse(INode node);

	public abstract List<INode> getIUse();

	public abstract void addUsedBy(INode node);

	public abstract boolean removeUsedBy(INode node);
	
	public abstract List<INode> getUsedBy();

	public abstract boolean uses(INode other);

	public abstract boolean usesIndirect(INode other);

	public abstract boolean usedBy(INode other);

	public abstract boolean usedByIndirect(INode other);

	public abstract void compressLinks();

	public abstract boolean equals(Object other);

}