package nl.progaia.esbxref.tasks;

import com.sonicsw.deploy.storage.DSArtifactStorage;
import com.sonicsw.deploy.tools.gui.common.DomainConnectionModel;

public class AnalyzeDomainTask extends AnalyzeArtifactStoreTask {
	
	private final String domain;
	private final String url;
	private final String username;
	private final String password;
	
	public AnalyzeDomainTask(DomainConnectionModel model) {
		this.domain = model.getDomain();
		this.url = model.getURL();
		this.username = model.getUsername();
		this.password = model.getPassword();
	}
	
	public AnalyzeDomainTask(String domain, String url, String username, String password) {
		this.domain = domain;
		this.url = url;
		this.username = username;
		this.password = password;
	}
	
	@Override
	public void execute() throws Exception {
        DSArtifactStorage dsStore = new DSArtifactStorage();
        dsStore.connect(domain, url, username, password);

		try {
			setStorage(dsStore);
			super.execute();
		} finally {
			dsStore.disconnect();
		}
	}

}
