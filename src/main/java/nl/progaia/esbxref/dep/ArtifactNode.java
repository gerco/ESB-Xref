package nl.progaia.esbxref.dep;

import com.sonicsw.deploy.IArtifact;

public class ArtifactNode extends AbstractNode {
	public static final long serialVersionUID = 1;
	
	public ArtifactNode(IArtifact artifact) {
		super(artifact.getName(), artifact.getArchivePath());
	}
}
