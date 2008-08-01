package nl.progaia.esbxref;

import java.io.File;

import com.sonicsw.deploy.IArtifact;
import com.sonicsw.deploy.IArtifactStorage;
import com.sonicsw.deploy.IArtifactTraverser;
import com.sonicsw.deploy.action.SearchAction;
import com.sonicsw.deploy.artifact.ESBArtifact;
import com.sonicsw.deploy.storage.ZipArtifactStorage;
import com.sonicsw.deploy.traversal.TraverserFactory;
import com.sonicsw.pso.util.MFUtils;
import com.sonicsw.xqimpl.config.XQProcessConfig;
import com.sonicsw.xqimpl.config.XQServiceConfig;

public class Main {

	public static void main(String[] args) throws Exception {
		DependencyGraph graph;
		File graphFile = new File("dependencies.dat");
		
		if(!graphFile.exists()) {
			System.setProperty("com.sonicsw.xq.home", "D:/Sonic75/ESB7.5");
			
			// Initialize the storage in some way (DS, Xar, File, etc)
			MFUtils domain = new MFUtils("Domain1", "localhost:2506", "Administrator", "Administrator");
//			MFUtils domain = new MFUtils("dmInformaTI", "detbusdm01.emea.corplan.net:6000", "Administrator", "*UHBnji9");
			IArtifactStorage storage = 
				domain.getDSArtifactStorage();
//				new ZipArtifactStorage();
//			((ZipArtifactStorage)storage).openArchive("dmInformaTI.xar", false);

			
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
	
	//		traverseArtifacts(storage);
			
			// List all artifacts in ESB feed them into the graph
			graph = new DependencyGraph();
			graph.analyse(storage, ESBArtifact.ROOT);
//			graph.analyse(storage, new ESBArtifact("/Processes/process.person"));
			graph.save(graphFile);
			System.out.println("Saved to file: " + graphFile.getAbsolutePath());
			
//			((ZipArtifactStorage)storage).closeArchive();
//			domain.cleanup();
		} else {
			System.out.println("Loading from file: " + graphFile.getAbsolutePath());
			graph = DependencyGraph.load(graphFile);
		}		
		
		graph.dumpToHTMLFile(new File("deps.html"));
		System.out.println("Dumped to html");
		graph.dumpToCSVFile(new File("deps.csv"));
		System.out.println("Dumped to csv");
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
