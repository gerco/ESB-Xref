package nl.progaia.esbprocessdraw.store;

import nl.progaia.esbprocessdraw.schema.Process;

public interface ArtifactStore {
	public Process getProcess(String name);
}
