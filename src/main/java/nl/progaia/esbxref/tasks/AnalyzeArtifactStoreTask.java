package nl.progaia.esbxref.tasks;

import java.util.ArrayList;

import nl.progaia.esbxref.artifact.QueueArtifact;
import nl.progaia.esbxref.artifact.TopicArtifact;
import nl.progaia.esbxref.dep.DependencyGraph;
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
		IArtifact[] as = traverseArtifacts(getStorage(), ESBArtifact.ROOT);
		artifactsToAnalyze = as.length;
		int currentArtifact = 0;
		
		// Report the task as started, we know the number of items to analyze now
		super.dispatchTaskStarted();

		// Store all artifacts in the graph first
		for(IArtifact a: as) {
			graph.addArtifact(a);
		}
		
		// Perform the analysis
		for(IArtifact a: as) {
			currentArtifactName = a.getName();
			reportProgress(currentArtifact++);
	
			// Skip .svn files in SonicFS
			if("SonicFS".equals(a.getDisplayType()) && a.getName().contains("/.svn/"))
				continue;

			// Ignore .sid files from Sonic Workbench
			if("SonicFS".equals(a.getDisplayType()) && a.getName().endsWith(".sid"))
				continue;
			
			analyzeInternal(getStorage(), a, graph);
			if(canceled) break;
		}

		// Compress the links in the graph to the minimum number to represent the
		// information completely
		graph.compressLinks();
		
		reportProgress(getProgressMaximum());
	}
	
	@Override
	protected void dispatchTaskStarted() {
		// Do nothing
	}
	
	@Override
	protected Object getInfo() {
		return graph;
	}
	
	@Override
	public int getProgressMaximum() {
		return artifactsToAnalyze;
	}
	
	public void cancel() {
		canceled = true;
	}	
	
	@Override
	public String getStatus() {
		return "Analyzing " + currentArtifactName;
	}
	
	@Override
	public String toString() {
		return "Analyzing dependencies";
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
			
		// If the artifact is a service that belongs with a process, merge the two by
		// replacing 'root' with the process artifact but specifying the dependencies
		// of the original service artifact.
		if(ESBArtifact.SERVICE.getArchivePath().equals(root.getArchiveParentPath())) {
			String processPath = ESBArtifact.PROCESS.getArchivePath() + root.getName();
			if(graph.getNode(processPath) != null) {
				root = new ESBArtifact(ESBArtifact.PROCESS, root.getName());
			}
		}
		
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
					ArrayList<IArtifact> topicArtifacts = new ArrayList<IArtifact>();
					for(String name: topicNames) {
						topicArtifacts.add(new TopicArtifact(name));
					}
					return topicArtifacts.toArray(new IArtifact[0]);
				} else {
					return new IArtifact[] {new TopicArtifact(destination)};
				}
			}
			
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
//	        TraversalContext context = new TraversalContext(storage, ExportPropertiesArtifact.DEFAULT_IGNORE);
	        TraverserContext context = new TraverserContext(storage, ExportPropertiesArtifact.DEFAULT_IGNORE);
	        context.setTraverseCompressed(false);
	        
	        TraverserFactory.createTraverser(root).traverse(context);
	
	        return context.completeTraversal();
		} catch (UnsupportedOperationException e) {
			return new IArtifact[0];
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
