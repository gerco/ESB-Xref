package nl.progaia.esbxref.dep;

import java.io.Serializable;

public class Link implements Comparable<Link>, Serializable {

	private final INode source;
	private final INode target;
	private final boolean hard;
	
	public Link(INode source, INode target, boolean hard) {
		if(source == target)
			throw new IllegalArgumentException("Cannot link a node to itself!");
		
		this.source = source;
		this.target = target;
		this.hard = hard;
	}
	
	public INode getSource() {
		return source;
	}
	
	public INode getTarget() {
		return target;
	}
	
	public boolean isHard() {
		return hard;
	}

	@Override
	public int hashCode() {
		final int prime = 31;
		int result = 1;
		result = prime * result + (hard ? 1231 : 1237);
		result = prime * result + ((source == null) ? 0 : source.hashCode());
		result = prime * result + ((target == null) ? 0 : target.hashCode());
		return result;
	}

	@Override
	public boolean equals(Object obj) {
		if (this == obj)
			return true;
		if (obj == null)
			return false;
		if (getClass() != obj.getClass())
			return false;
		Link other = (Link) obj;
		if (hard != other.hard)
			return false;
		if (source == null) {
			if (other.source != null)
				return false;
		} else if (!source.equals(other.source))
			return false;
		if (target == null) {
			if (other.target != null)
				return false;
		} else if (!target.equals(other.target))
			return false;
		return true;
	}

	public int compareTo(Link o) {
		// TODO Auto-generated method stub
		return 0;
	}
}
