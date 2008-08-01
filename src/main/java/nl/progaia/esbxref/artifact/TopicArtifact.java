package nl.progaia.esbxref.artifact;

public class TopicArtifact extends MQArtifact {
	
	public TopicArtifact(String name) {
		super(name);
	}
	
	@Override
	public String getRootDirectory() {
		return "Topics";
	}
	
}
