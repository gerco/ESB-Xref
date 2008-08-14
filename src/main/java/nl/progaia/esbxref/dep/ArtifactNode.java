package nl.progaia.esbxref.dep;

import com.sonicsw.deploy.IArtifact;
import com.sonicsw.deploy.IArtifactStorage;

public class ArtifactNode extends AbstractNode {
	public static final long serialVersionUID = 1;
	
	private String artifactXml;
	
	public ArtifactNode(IArtifact artifact) {
		super(artifact.getName(), artifact.getArchivePath());
	}
	
	public ArtifactNode(IArtifactStorage storage, IArtifact artifact) {
		this(artifact);
		artifactXml = retrieveArtifactXml(storage, artifact);
	}
	
	public String getArtifactXml() {
		return artifactXml;
	}

	public void setArtifactXml(String artifactXml) {
		this.artifactXml = artifactXml;
	}

	private String retrieveArtifactXml(IArtifactStorage storage, IArtifact artifact) {
		try {
			return storage.getContentsAsString(artifact);
		} catch (Exception e) {
			e.printStackTrace();
			return null;
		}
	}
}
