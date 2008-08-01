package nl.progaia.esbxref.artifact;

import com.sonicsw.deploy.IArtifact;
import com.sonicsw.deploy.artifact.AbstractArtifact;

public abstract class MQArtifact extends AbstractArtifact {
	public static final IArtifact QUEUE = new QueueArtifact("") {
		@Override
		public boolean isDirectory() {
			return true;
		}
	};
	
	public static final IArtifact TOPIC = new TopicArtifact("") {
		@Override
		public boolean isDirectory() {
			return true;
		}
	};
	
	protected final String name;
	
	public MQArtifact(String name) {
		this.name = name;
	}	
	
	@Override
	public String getArchiveParentPath() {
		return "/" + getDisplayType() + getParentPath();
	}

	@Override
	public String getArchivePath() {
		return getArchiveParentPath() + getName();
	}

	@Override
	public String getDisplayType() {
		return "MQ";
	}
	
	@Override
	public String getName() {
		return name;
	}

	@Override
	public String getExtension() {
		return null;
	}

	@Override
	public String getParentPath() {
		return "/" + getRootDirectory() + "/";
	}

	@Override
	public String getPath() {
		return getParentPath() + getName();
	}

	@Override
	public boolean isDirectory() {
		return false;
	}

	@Override
	public boolean isHidden() {
		return false;
	}

	@Override
	public boolean isRootPath() {
		return false;
	}

	@Override
	public boolean isValidArtifact() {
		return true;
	}

	@Override
	public String toString() {
		return getPath();
	}	
}
