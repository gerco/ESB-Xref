package nl.progaia.esbxref;

import java.io.File;
import java.util.ArrayList;
import java.util.List;

import nl.progaia.esbxref.artifact.QueueArtifact;
import nl.progaia.esbxref.artifact.TopicArtifact;
import nl.progaia.esbxref.ui.MainFrame;

import org.w3c.dom.Document;
import org.w3c.dom.Node;
import org.w3c.dom.NodeList;

import com.sonicsw.deploy.IArtifact;
import com.sonicsw.deploy.IArtifactNotificationEvent;
import com.sonicsw.deploy.IArtifactNotificationListener;
import com.sonicsw.deploy.IArtifactStorage;
import com.sonicsw.deploy.artifact.ESBArtifact;
import com.sonicsw.deploy.tools.common.ExportPropertiesArtifact;
import com.sonicsw.deploy.traversal.TraverserFactory;
import com.sonicsw.pso.util.MFUtils;

public class Main implements Runnable, IArtifactNotificationListener {

	private transient List<IArtifact> analyzed;
	File graphFile = new File("dependencies.dat");
	
	public static void main(String[] args) throws Exception {
		new Main().run();
	}
	
	public void run() {
		try {
			DependencyGraph graph;
			
			if(!graphFile.exists()) {
				System.setProperty("com.sonicsw.xq.home", "D:/Sonic75/ESB7.5");
				
				// Initialize the storage in some way (DS, Xar, File, etc)
				MFUtils domain = new MFUtils("Domain1", "localhost:2506", "Administrator", "Administrator");
				
				IArtifactStorage storage = 
					domain.getDSArtifactStorage();
//					new ZipArtifactStorage();
//				((ZipArtifactStorage)storage).openArchive("dmInformaTI.xar", false);
				
				graph = analyzeStorage(storage);
				domain.cleanup();
				
				graph.save(graphFile);
//				System.out.println("Saved to file: " + graphFile.getAbsolutePath());
			} else {
				System.out.println("Loading from file: " + graphFile.getAbsolutePath());
				graph = DependencyGraph.load(graphFile);
			}		
			
			graph.dumpToHTMLFile(new File("deps.html"));
			System.out.println("Dumped to html");
			graph.dumpToCSVFile(new File("deps.csv"));
			System.out.println("Dumped to csv");
			
			MainFrame frame = new MainFrame(graph);
			frame.pack();
			frame.setVisible(true);
		} catch (Exception e) {
			e.printStackTrace();
		}
	}
	
	public DependencyGraph analyzeStorage(IArtifactStorage storage) throws Exception {
			// List all artifacts in storage feed them into the graph
			DependencyGraph graph = new DependencyGraph();
			analyze(storage, ESBArtifact.ROOT, graph);
			graph.compressLinks();
			graph.printStats();
			return graph;			
	}
	
	/**
	 * Analyse the storage for dependencies, starting at root.
	 * 
	 * @param storage
	 * @param root
	 * @param graph
	 * @throws Exception
	 */
	public void analyze(IArtifactStorage storage, IArtifact root, DependencyGraph graph) throws Exception {
		storage.addNotificationListener(this);
		
		analyzed = new ArrayList<IArtifact>();
		analyzed.add(root);
		graph.addArtifact(root);
		
		IArtifact[] as = traverseArtifacts(storage, root);
		System.out.println(as.length + " artifacts to analyze");
		for(IArtifact a: as) {
			System.out.println("Analyzing " + a.getArchivePath());
			analyzeInternal(storage, a, graph);
		}
	}

	private void analyzeInternal(IArtifactStorage storage, IArtifact root, DependencyGraph graph) throws Exception {
		if(analyzed.contains(root))
			return;
				
		analyzed.add(root);
		
		// Get dependent artifacts from the traversers and deep analysis
		IArtifact[] travDeps = traverseArtifacts(storage, root);
		IArtifact[] deepDeps = analyzeDeep(storage, root, graph);

		// Combine the dependecies from the two sources
		IArtifact[] dependencies = new IArtifact[travDeps.length + deepDeps.length];
		System.arraycopy(travDeps, 0, dependencies, 0, travDeps.length);
		System.arraycopy(deepDeps, 0, dependencies, travDeps.length, deepDeps.length);
			
		// Add the artifact to the graph and link the dependencies accordingly
		graph.addArtifact(root, dependencies);
		
		// Analyze the dependencies themselves
		for(IArtifact a: dependencies) {
			analyzeInternal(storage, a, graph);
		}
	}	
	
	/**
	 * Perform extra analysis on some artifacts the Traversal API doesn't handle.
	 * 
	 * @param storage
	 * @param root
	 * @param graph
	 */
	private IArtifact[] analyzeDeep(IArtifactStorage storage, IArtifact root, DependencyGraph graph) {
		if(ESBArtifact.ENDPOINT.getArchivePath().equals(root.getArchiveParentPath()))
			return analyzeEndpoint(storage, root, graph);
		
		return new IArtifact[0];
	}

	/**
	 * Add the referenced Topic or Queue to the dependency graph
	 * 
	 * @param storage
	 * @param root
	 * @param graph
	 */
	private IArtifact[] analyzeEndpoint(IArtifactStorage storage, IArtifact root, DependencyGraph graph) {
		try {
			Document doc = storage.getContentsAsDom(root);
			
			String endpointType = null;
			String destination = null;
			
			NodeList params = doc.getElementsByTagNameNS(XQNamespaceContext.NAMESPACE, "stringParam");
			for(int i=0; i<params.getLength(); i++) {
				Node param = params.item(i);
				
				if("type".equals(getAttributeValue(param, "name"))) {
					endpointType = param.getTextContent();
				}
				
				if("destination".equals(getAttributeValue(param, "name"))) {
					destination = param.getTextContent();
				}
			}
			
			if("TOPIC".equals(endpointType))
				return new IArtifact[] {new TopicArtifact(destination)};
			
			if("QUEUE".equals(endpointType))
				return new IArtifact[] {new QueueArtifact(destination)};			
		} catch (Exception e) {
			e.printStackTrace();
		}
		
		return new IArtifact[0];
	}
	
	private String getAttributeValue(Node n, String attribute) {
		return n.getAttributes().getNamedItem(attribute).getNodeValue();
	}

	private static IArtifact[] traverseArtifacts(IArtifactStorage storage, IArtifact root) throws Exception {
		try {
	        TraversalContext context = new TraversalContext(storage, ExportPropertiesArtifact.DEFAULT_IGNORE);
	        context.setTraverseCompressed(false);
	        
	        TraverserFactory.createTraverser(root).traverse(context);
	
	        return context.completeTraversal();
		} catch (UnsupportedOperationException e) {
			return new IArtifact[0];
		}
	}

	public void notifyMessage(IArtifactNotificationEvent arg0) {
		// TODO Auto-generated method stub
	}	
	
		
//	public static void listArtifacts(IArtifactStorage storage, IArtifact type) throws Exception {
//		// Find every artifact in the storage
//		IArtifactTraverser trav = TraverserFactory.createESBTraverser(type);
//		IArtifact[] artifacts = SearchAction.search(storage, null, trav);
//		
//		for(IArtifact artifact: artifacts) {
//			System.out.println("------");
//			System.out.println(artifact.getName());
////			System.out.println(artifact.getDisplayType());
//			System.out.println(artifact.getArchivePath());
//			if("ESB".equals(artifact.getDisplayType())) {
//				if(ESBArtifact.SERVICE == type) {
//					XQServiceConfig sc = new XQServiceConfig();
//					sc.initFromXMLString(
//							artifact.getName(),
//							storage.getContentsAsString(artifact),
//							false /* validating? */);
//					System.out.println(sc.getEntryEndpoint());
//				}
//				
//				if(ESBArtifact.PROCESS == type) {
//					XQProcessConfig pc = new XQProcessConfig();
//					pc.initFromXMLString(
//							artifact.getName(), 
//							storage.getContentsAsString(artifact),
//							false);
//					System.out.println(pc.getEntryEndpoint());
//				}
//			}
//		}
//	}
}
