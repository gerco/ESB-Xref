package nl.progaia.esbxref.artifact;

public class QueueArtifact extends MQArtifact {
	
	public QueueArtifact(String name) {
		super(name);
	}
	
	@Override
	public String getRootDirectory() {
		return "Queues";
	}
	
}
