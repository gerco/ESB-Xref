package nl.progaia.esbxref;

import com.sonicsw.deploy.IArtifact;
import com.sonicsw.deploy.IArtifactStorage;
import com.sonicsw.deploy.IArtifactTraverser;
import com.sonicsw.deploy.action.SearchAction;
import com.sonicsw.deploy.artifact.ESBArtifact;
import com.sonicsw.deploy.artifact.SonicFSArtifact;
import com.sonicsw.deploy.tools.common.ExportPropertiesArtifact;
import com.sonicsw.deploy.traversal.TraverserFactory;
import com.sonicsw.pso.util.MFUtils;
import com.sonicsw.xqimpl.config.XQProcessConfig;
import com.sonicsw.xqimpl.config.XQServiceConfig;

public class Main {

	public static void main(String[] args) throws Exception {
		System.setProperty("com.sonicsw.xq.home", "D:/Sonic75/ESB7.5");
		// Initialize the storage in some way (DS, Xar, File, etc)
		MFUtils domain = new MFUtils("Domain1", "localhost:2506", "Administrator", "Administrator");
		IArtifactStorage storage = domain.getDSArtifactStorage();

		// For all valid ESB types
//		for(IArtifact type: ESBArtifact.getValidTypes()) {
//			listArtifacts(storage, type);
//		}
		
//		listArtifacts(storage, ESBArtifact.PROCESS);
		
//		DependencyAction da = new DependencyAction();
//		Hashtable artifacts = new Hashtable();
//		artifacts.put("sap60.book.book_so.application.onramp", new Object());
//		da.setArtifactList(artifacts);
//		da.run(storage);
//		da.generateReport(new File(da.getDefaultOutputFilename()));
		
//		DependencyAction.createDependencyReport(storage);

		traverseArtifacts(storage);
		
		domain.cleanup();
	}
	
	public static void traverseArtifacts(IArtifactStorage storage) throws Exception {
		IArtifact[] result = SearchAction.search(
				storage, 
				ExportPropertiesArtifact.DEFAULT_IGNORE, 
				TraverserFactory.createTraverser(
//						new ESBArtifact("/Processes/sap60.pi.vendor.application.offramp")
//						ESBArtifact.PROCESS
//						RootArtifact.ROOT
						ESBArtifact.ROOT
//						SonicFSArtifact.ROOT
				));
		for(IArtifact a: result) {
			System.out.println(a.getDisplayType() + ":" + a.getPath());
		}
	}
	
	public static void listArtifacts(IArtifactStorage storage, IArtifact type) throws Exception {
		// Find every artifact in the storage
		IArtifactTraverser trav = TraverserFactory.createESBTraverser(type);
		IArtifact[] artifacts = SearchAction.search(storage, null, trav);
		
		for(IArtifact artifact: artifacts) {
			System.out.println("------");
			System.out.println(artifact.getName());
//			System.out.println(artifact.getDisplayType());
			System.out.println(artifact.getArchivePath());
			if("ESB".equals(artifact.getDisplayType())) {
				if(ESBArtifact.SERVICE == type) {
					XQServiceConfig sc = new XQServiceConfig();
					sc.initFromXMLString(
							artifact.getName(),
							storage.getContentsAsString(artifact),
							false /* validating? */);
					System.out.println(sc.getEntryEndpoint());
				}
				
				if(ESBArtifact.PROCESS == type) {
					XQProcessConfig pc = new XQProcessConfig();
					pc.initFromXMLString(
							artifact.getName(), 
							storage.getContentsAsString(artifact),
							false);
					System.out.println(pc.getEntryEndpoint());
				}
			}
		}
	}
}
