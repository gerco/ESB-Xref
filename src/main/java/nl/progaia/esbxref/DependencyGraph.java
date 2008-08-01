package nl.progaia.esbxref;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.FileWriter;
import java.io.IOException;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;
import java.io.PrintWriter;
import java.io.Serializable;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.TreeSet;

import com.sonicsw.deploy.IArtifact;
import com.sonicsw.deploy.IArtifactNotificationEvent;
import com.sonicsw.deploy.IArtifactNotificationListener;
import com.sonicsw.deploy.IArtifactStorage;
import com.sonicsw.deploy.artifact.ESBArtifact;
import com.sonicsw.deploy.tools.common.ExportPropertiesArtifact;
import com.sonicsw.deploy.traversal.TraverserFactory;

public class DependencyGraph implements IArtifactNotificationListener, Serializable {
	public static final long serialVersionUID = 1;
	
	private final HashMap<String, Node> nodes = new HashMap<String, Node>();
	private transient List<IArtifact> analyzed;
	
	/**
	 * Add an artifact to the graph without dependencies
	 * 
	 * @param artifact
	 */
	public void addArtifact(IArtifact artifact) {
		addArtifactInternal(artifact);
	}
	
	private Node addArtifactInternal(IArtifact artifact) {
		if(nodes.containsKey(artifact.getPath()))
			return nodes.get(artifact.getPath());
		
		// If the artifact is a service that belongs with a process, merge the two
		if(ESBArtifact.SERVICE.getRootDirectory().equals(artifact.getParentPath())) {
			String processName = ESBArtifact.PROCESS.getRootDirectory() + artifact.getName();
			if(nodes.containsKey(processName)) {
				System.out.println("Merged " + artifact.getPath() + " with " + processName);
				return nodes.get(processName);
			}
		}
		
		// If the artifact is a process and the service for that process was already 
		// created, return that node.
		if(ESBArtifact.PROCESS.getRootDirectory().equals(artifact.getParentPath())) {
			String serviceName = ESBArtifact.SERVICE.getRootDirectory() + artifact.getName();
			if(nodes.containsKey(serviceName)) {
				System.out.println("Merged " + artifact.getPath() + " with " + serviceName);
				return nodes.get(serviceName);
			}
		}
		
//		System.out.println("Adding new artifact: " + artifact.getPath());
		Node newNode = new Node(artifact);
		nodes.put(artifact.getPath(), newNode);
		return newNode;
	}
	
	/**
	 * Add an artifact to the graph with dependencies
	 * 
	 * @param artifact
	 * @param dependencies
	 */
	public void addArtifact(IArtifact artifact, IArtifact[] dependencies) {
		if(artifact == null)
			throw new IllegalArgumentException("Artifact must not be null");
		
		Node node = addArtifactInternal(artifact);
		if(node == null)
			throw new RuntimeException("Node == null after adding to HashMap??");
		
		for(IArtifact dep: dependencies) {
			// Find the node if it already exists, create otherwise
			Node depNode = nodes.get(dep.getPath());
			if(depNode == null) {
				depNode = addArtifactInternal(dep);
			}
			
			// Add the links to the parent and dependent node
//			System.out.println("Linking " + node + " and " + depNode);
			node.addIUse(depNode);
			depNode.addUsedBy(node);
		}
	}
	
	/**
	 * Get an artifact from the graph
	 */
	public Node getArtifact(String path) {
		return nodes.get(path);
	}
	
	/**
	 * Analyse the storage for dependencies, starting at root.
	 * 
	 * @param storage
	 * @param root
	 * @param graph
	 * @throws Exception
	 */
	public void analyse(IArtifactStorage storage, IArtifact root) throws Exception {
		storage.addNotificationListener(this);
		
		analyzed = new ArrayList<IArtifact>();
		analyzed.add(root);
		addArtifact(root);
		
		IArtifact[] as = traverseArtifacts(storage, root);
		System.out.println(as.length + " artifacts to analyze");
		for(IArtifact a: as) {
			System.out.println("Analyzing " + a.getPath());
			analyseInternal(storage, a);
		}
		printStats();
		compressLinks();
		printStats();
	}

	private void analyseInternal(IArtifactStorage storage, IArtifact root) throws Exception {
		if(analyzed.contains(root))
			return;
		
		analyzed.add(root);
		
		IArtifact[] dependencies = traverseArtifacts(storage, root);
		addArtifact(root, dependencies);
		
		for(IArtifact a: dependencies) {
			// If the artifact not a directory, the traverser will already have analyzed
			// it's dependencies and they are already included here. Do not traverse that
			// artifact again.
			//
			// On the other hand... we do want the direct dependencies of every artifact,
			// not just those of the top level artifacts, or else there wouldn't be much
			// to compress for compressLinks()
//			if(!a.isDirectory())
//				analyzed.add(a);
			
			analyseInternal(storage, a);
		}
	}	
	
	private static IArtifact[] traverseArtifacts(IArtifactStorage storage, IArtifact root) throws Exception {
        TraversalContext context = new TraversalContext(storage, ExportPropertiesArtifact.DEFAULT_IGNORE);
        context.setTraverseCompressed(false);
        
        TraverserFactory.createTraverser(root).traverse(context);

        return context.completeTraversal();
//        m_errors = context.getErrored();
        
//		return SearchAction.search(
//				storage, 
//				ExportPropertiesArtifact.DEFAULT_IGNORE, 
//				TraverserFactory.createTraverser(root));
	}	
	
	/**
	 * Remove all direct links between nodes that are also indirectly linked.
	 */
	public void compressLinks() {
		for(Node n: nodes.values()) {
//			System.out.println("Scanning " + n.getPath() + " for indirect links");
			
			// Compress uses links
			for(Iterator<Node> it = n.getIUse().iterator(); it.hasNext();) {
				Node directlyLinkedNode = it.next();
				
				if(n.usesIndirect(directlyLinkedNode)) {
//					System.out.println("Removing link between " + n + " and " + directlyLinkedNode);
					it.remove();
				}
			}
			
			// Compress usedby links
			for(Iterator<Node> it = n.getUsedBy().iterator(); it.hasNext();) {
				Node directlyLinkedNode = it.next();
				
				if(n.usedByIndirect(directlyLinkedNode)) {
//					System.out.println("Removing link between " + n + " and " + directlyLinkedNode);
					it.remove();
				}
			}
		}
	}

	public void save(File file) throws FileNotFoundException, IOException {
		ObjectOutputStream oos = new ObjectOutputStream(new FileOutputStream(file));
		oos.writeObject(this);
		oos.flush();
		oos.close();
	}
	
	public static DependencyGraph load(File file) throws FileNotFoundException, IOException, ClassNotFoundException {
		ObjectInputStream ois = new ObjectInputStream(new FileInputStream(file));
		
		return (DependencyGraph)ois.readObject();
	}
	
	/**
	 * Write the dependency graph to file in html
	 * 
	 * @param file
	 * @throws IOException 
	 */
	public void dumpToHTMLFile(File file) throws IOException {
		PrintWriter w = new PrintWriter(new FileWriter(file));
		
		w.println("<html><body>");
		
		// List all Nodes by name
		w.println("<h1>List of all artifacts</h1>");
		TreeSet<String> sortedSet = new TreeSet<String>();
		sortedSet.addAll(nodes.keySet());
		for(String path: sortedSet) {
			w.println("<a href=\"#" + path.replace('/', '_') + "\">" + path + "</a><br>");
		}
		
		// Section with dependencies per Node
		w.println("<h1>Dependencies per artifact</h1>");
		for(String path: nodes.keySet()) {
			w.println("<h2><a name=\"" + path.replace('/', '_') + "\">" + path + "</a></h2>");

			Node n = nodes.get(path);
			
			w.println("<h3>Uses</h3>");
			for(Node dep: n.getIUse()) {
				w.println("<a href=\"#" + dep.getPath().replace('/', '_') + "\">" + dep.getPath() + "</a><br>");
			}
			
			w.println("<h3>Used by</h3>");
			for(Node dep: n.getUsedBy()) {
				w.println("<a href=\"#" + dep.getPath().replace('/', '_') + "\">" + dep.getPath() + "</a><br>");
			}
		}
				
		w.println("</body></html>");
		
		w.close();
	}
	
	public void dumpToCSVFile(File file) throws IOException {
		PrintWriter w = new PrintWriter(new FileWriter(file));

		for(String path: nodes.keySet()) {
			for(Node n: nodes.get(path).getIUse()) {
				w.print(path);
				w.print(";");
				w.print(n.getPath());
				w.println();
			}
		}
		
		w.close();
	}

	public void notifyMessage(IArtifactNotificationEvent event) {
		// Ignore
	}
	
	public void printStats() {
		int numLinks = 0;
		
		for(Node n: nodes.values()) {
			numLinks += n.getIUse().size() + n.getUsedBy().size();
		}
		
		System.out.println(nodes.size() + " nodes with " + numLinks + " links");
	}	
}
