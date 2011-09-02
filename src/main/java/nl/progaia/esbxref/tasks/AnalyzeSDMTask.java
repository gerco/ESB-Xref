package nl.progaia.esbxref.tasks;

import java.io.File;

import nl.progaia.esbxref.SDMArtifactStorage;

public class AnalyzeSDMTask extends AnalyzeArtifactStoreTask {
	
	private final File modelDir;
	
	public AnalyzeSDMTask(File modelDir) {
		this.modelDir = modelDir;
	}
	
	@Override
	public void execute() throws Exception {
		SDMArtifactStorage storage = new SDMArtifactStorage();
		storage.setModelDir(modelDir);
		
		setStorage(storage);
		
		try {
			super.execute();
		} catch (Exception e) {
			e.printStackTrace();
		}
	}

}
