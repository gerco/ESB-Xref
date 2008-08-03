package nl.progaia.esbxref.tasks;

import com.sonicsw.pso.util.MFUtils;

public class AnalyzeDomainTask extends AnalyzeArtifactStoreTask {
	
	private final String domain;
	private final String url;
	private final String username;
	private final String password;
	
	public AnalyzeDomainTask(String domain, String url, String username, String password) {
		this.domain = domain;
		this.url = url;
		this.username = username;
		this.password = password;
	}
	
	@Override
	public void execute() throws Exception {
		MFUtils utils = new MFUtils(domain, url, username, password);
		
		setStorage(utils.getDSArtifactStorage());
		
		super.execute();
		
		utils.cleanup();
	}

}
