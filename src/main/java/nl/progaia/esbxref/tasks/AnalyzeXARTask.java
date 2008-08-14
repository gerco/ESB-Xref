package nl.progaia.esbxref.tasks;

import java.io.File;

import com.sonicsw.deploy.storage.ZipArtifactStorage;

public class AnalyzeXARTask extends AnalyzeArtifactStoreTask {
	
	private final File file;
	
	public AnalyzeXARTask(File file) {
		this.file = file;
	}
	
	@Override
	public void execute() throws Exception {
		ZipArtifactStorage storage = new ZipArtifactStorage();
		storage.openArchive(file.getAbsolutePath(), false);
		
		setStorage(storage);
		
		super.execute();
	
		storage.closeArchive();
	}

}
