package nl.progaia.esbxref.tasks;

import java.util.ArrayList;
import java.util.List;

import nl.progaia.esbxref.artifact.QueueArtifact;
import nl.progaia.esbxref.artifact.TopicArtifact;
import nl.progaia.esbxref.dep.ArtifactNode;
import nl.progaia.esbxref.dep.Dependency;
import nl.progaia.esbxref.dep.DependencyGraph;
import nl.progaia.esbxref.dep.INode;
import nl.progaia.esbxref.task.CancelableTask;
import nl.progaia.esbxref.task.Task;

import org.w3c.dom.Document;
import org.w3c.dom.Node;
import org.w3c.dom.NodeList;

import com.sonicsw.deploy.IArtifact;
import com.sonicsw.deploy.IArtifactNotificationEvent;
import com.sonicsw.deploy.IArtifactNotificationListener;
import com.sonicsw.deploy.IArtifactStorage;
import com.sonicsw.deploy.artifact.ESBArtifact;
import com.sonicsw.deploy.artifact.RootArtifact;
import com.sonicsw.deploy.tools.common.ExportPropertiesArtifact;
import com.sonicsw.deploy.traversal.TraverserContext;
import com.sonicsw.deploy.traversal.TraverserFactory;

public class AnalyzeArtifactStoreTask extends Task implements CancelableTask {
	
	private static final String XQ_NAMESPACE = "http://www.sonicsw.com/sonicxq";
	
	private volatile boolean canceled = false;
	private IArtifactStorage storage;
	private ArrayList<IArtifact> analyzed;
	private int artifactsToAnalyze = 0;
	private String currentArtifactName;
	private final DependencyGraph graph = new DependencyGraph();
	
	@Override
	public void execute() throws Exception {
		// Ignore all messages from the store, they clutter up the console
		getStorage().addNotificationListener(new IgnoreAllMessagesListener());
		
		// List all artifacts in storage feed them into the graph

		// Start by adding the root element to the analyzed list. This prevents infinite loops
		analyzed = new ArrayList<IArtifact>();
		analyzed.add(RootArtifact.ROOT);
		graph.addArtifact(RootArtifact.ROOT);

		// How many artifacts do we need to analyze?
		List<Dependency> ds = traverseArtifacts(getStorage(), RootArtifact.ROOT);
		artifactsToAnalyze = ds.size();
		int currentArtifact = 0;
		
		// Report the number of items to analyze now
		currentArtifactName = artifactsToAnalyze + " artifacts";
		dispatchProgressInfo(artifactsToAnalyze);

		// Store all artifacts in the graph first
		for(Dependency d: ds) {
			graph.addArtifact(d.artifact);
		}
		
		// Perform the analysis
		for(Dependency d: ds) {
			currentArtifactName = d.artifact.getName();
			reportProgress(currentArtifact++);
	
			// Skip .svn files in SonicFS
			if("SonicFS".equals(d.artifact.getDisplayType()) && d.artifact.getName().contains("/.svn/"))
				continue;

			// Ignore .sid files from Sonic Workbench
			if("SonicFS".equals(d.artifact.getDisplayType()) && d.artifact.getName().endsWith(".sid"))
				continue;
			
			analyzeInternal(getStorage(), d.artifact, graph);
			if(canceled) break;
		}
		
		// Now merge /ESB/Service/processName with /ESB/Process/processName
		for(INode n: graph.getAllNodes()) {
			if(n.getPath().startsWith(ESBArtifact.PROCESS.getArchivePath())) {
				INode serviceNode = 
					graph.getNode(ESBArtifact.SERVICE.getArchivePath() + n.getName());
				if(serviceNode != null) {
					//System.out.println("Merging " + n + " and " + serviceNode);
					graph.mergeNodes(n, serviceNode);
				}
			}
		}		

		// Compress the links in the graph to the minimum number to represent the
		// information completely
		graph.compressLinks();
		
		reportProgress(getProgressMaximum());
	}
	
	@Override
	protected Object getInfo() {
		return graph;
	}
	
	@Override
	public int getProgressMaximum() {
		return artifactsToAnalyze == 0 ? 1 : artifactsToAnalyze;
	}
	
	public void cancel() {
		canceled = true;
	}	
	
	@Override
	public String getStatus() {
		if(artifactsToAnalyze == 0)
			return "Finding artifacts to analyze";
		
		return "Analyzing " + currentArtifactName;
	}
	
	@Override
	public String toString() {
		return getStatus();
	}
	
	private void analyzeInternal(IArtifactStorage storage, IArtifact root, DependencyGraph graph) throws Exception {
		if(analyzed.contains(root))
			return;
		
		if(root.getName().equals("ba.sip.esb.car.FlightCargoLoadV01")) {
			System.out.println();
		}
		
		analyzed.add(root);
		
		// Get dependent artifacts from the traversers and deep analysis
		List<Dependency> travDeps = traverseArtifacts(storage, root);
		List<Dependency> deepDeps = analyzeDeep(storage, root, graph);

		// Combine the dependecies from the two sources
		List<Dependency> dependencies = new ArrayList<Dependency>();
		dependencies.addAll(travDeps);
		dependencies.addAll(deepDeps);
					
		// Add the artifact to the graph and link the dependencies accordingly
		ArtifactNode n = (ArtifactNode)graph.addArtifact(root, dependencies);
		if(n != null && n.getPath().startsWith(ESBArtifact.ROOT.getArchivePath())) {
			n.setArtifactXml(storage.getContentsAsString(root));
		}
		
		// Analyze the dependencies themselves
		for(Dependency d: dependencies) {
			analyzeInternal(storage, d.artifact, graph);
		}		
	}	
	
	/**
	 * Perform extra analysis on some artifacts the Traversal API doesn't handle.
	 * 
	 * @param storage
	 * @param root
	 * @param graph
	 */
	private List<Dependency> analyzeDeep(IArtifactStorage storage, IArtifact root, DependencyGraph graph) {
		if(ESBArtifact.ENDPOINT.getArchivePath().equals(root.getArchiveParentPath()))
			return analyzeEndpoint(storage, root, graph);
		
		if(ESBArtifact.CONTAINER.getArchivePath().equals(root.getArchiveParentPath()))
			return analyzeContainer(storage, root, graph);
		
		return new ArrayList<Dependency>();
	}

	/**
	 * Add the referenced Topic or Queue to the dependency graph
	 * 
	 * @param storage
	 * @param root
	 * @param graph
	 */
	private List<Dependency> analyzeEndpoint(IArtifactStorage storage, IArtifact root, DependencyGraph graph) {
		try {
			Document doc = storage.getContentsAsDom(root);
			
			String endpointType = null;
			String destination = null;
			
			NodeList params = doc.getElementsByTagNameNS(XQ_NAMESPACE, "stringParam");
			for(int i=0; i<params.getLength(); i++) {
				Node param = params.item(i);
				
				if("type".equals(getAttributeValue(param, "name"))) {
					endpointType = param.getTextContent();
				}
				
				if("destination".equals(getAttributeValue(param, "name"))) {
					destination = param.getTextContent();
				}
			}
			
			if("TOPIC".equals(endpointType)) {
				if(destination.startsWith("MULTITOPIC:")) {
					String[] topicNames = destination.substring(11).split("\\|\\|");
					List<Dependency> topicDependencies = new ArrayList<Dependency>();
					for(String name: topicNames) {
						topicDependencies.add(new Dependency(new TopicArtifact(name), false));
					}
					return topicDependencies;
				} else {
					List<Dependency> result = new ArrayList<Dependency>();
					result.add(new Dependency(new TopicArtifact(destination), false));
					return result;
				}
			}
			
			if("QUEUE".equals(endpointType)) {
				List<Dependency> result = new ArrayList<Dependency>();
				if(destination.contains("::")) {
					result.add(new Dependency(new QueueArtifact(destination.substring(destination.indexOf("::") + 2)), false));
				} else {
					result.add(new Dependency(new QueueArtifact(destination), false));
				}
			}
		} catch (Exception e) {
			e.printStackTrace();
		}
		
		return new ArrayList<Dependency>();
	}
	
	/**
	 * The traverser will not see the Process dependencies because they do not exist in
	 * a xar file (/Services/name.xml). Add the missing dependency nodes to the Process
	 * artifacts.
	 * 
	 * @param storage
	 * @param root
	 * @param graph
	 * @return
	 */
	private List<Dependency> analyzeContainer(IArtifactStorage storage, IArtifact root, DependencyGraph graph) {
		try {
			List<Dependency> containerDeps = new ArrayList<Dependency>();
			Document container = storage.getContentsAsDom(root);
			NodeList services = container.getElementsByTagNameNS(XQ_NAMESPACE, "serviceApplication");
			for(int i=0; i<services.getLength(); i++) {
				Node service = services.item(i);
				String serviceRef = getAttributeValue(service, "service_ref");
				
				IArtifact processArtifact = new ESBArtifact(ESBArtifact.PROCESS, serviceRef);
				if(graph.containsPath(processArtifact.getArchivePath()))
					containerDeps.add(new Dependency(processArtifact, true));
				
				IArtifact serviceArtifact = new ESBArtifact(ESBArtifact.SERVICE, serviceRef);
				if(graph.containsPath(serviceArtifact.getArchivePath()))
					containerDeps.add(new Dependency(serviceArtifact, true));
			}
			return containerDeps;
		} catch (Exception e) {
			e.printStackTrace();
		}
		
		return new ArrayList<Dependency>();
	}
	
	private String getAttributeValue(Node n, String attribute) {
		return n.getAttributes().getNamedItem(attribute).getNodeValue();
	}

	private static List<Dependency> traverseArtifacts(IArtifactStorage storage, IArtifact root) throws Exception {
		try {
//	        TraversalContext context = new TraversalContext(storage, ExportPropertiesArtifact.DEFAULT_IGNORE);
	        TraverserContext context = new TraverserContext(storage, ExportPropertiesArtifact.DEFAULT_IGNORE);
	        context.setTraverseCompressed(false);
	        
	        TraverserFactory.createTraverser(root).traverse(context);
	
	        IArtifact[] artifacts = context.completeTraversal();
	        List<Dependency> deps = new ArrayList<Dependency>(artifacts.length);
	        for(IArtifact a: artifacts) {
	        	deps.add(new Dependency(a, false));
	        }
	        return deps;
		} catch (UnsupportedOperationException e) {
			return new ArrayList<Dependency>();
		}
	}

	public void setStorage(IArtifactStorage storage) {
		this.storage = storage;
	}

	private IArtifactStorage getStorage() {
		return storage;
	}

	private static class IgnoreAllMessagesListener implements IArtifactNotificationListener {
		public void notifyMessage(IArtifactNotificationEvent arg0) {
			// TODO Auto-generated method stub
		}	
	}
		
}
