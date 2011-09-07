package nl.progaia.esbxref.dep;

import com.sonicsw.deploy.IArtifact;

public class Dependency {
	public final IArtifact artifact;
	public final boolean hard;
	
	public Dependency(IArtifact artifact, boolean hard) {
		this.artifact = artifact;
		this.hard = hard;
	}
}
