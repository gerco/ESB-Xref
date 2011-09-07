package nl.progaia.esbxref.dep;

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
import java.util.Collection;
import java.util.Collections;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.TreeSet;

import com.sonicsw.deploy.IArtifact;

public class DependencyGraph implements Serializable {
	public static final long serialVersionUID = 1;
	
	/**
	 * Holds the graph itself, key=path, value=INode
	 */
	private final HashMap<String, INode> nodes;
	
	/**
	 * The root node of the graph. It is not included in the graph itself because
	 * that would screw up the findUnused() analysis.
	 */
	private final INode rootNode;
	
	public DependencyGraph() {
		nodes = new HashMap<String, INode>();
		rootNode = new RootNode();
	}
	
	/**
	 * Add an artifact to the graph without dependencies
	 * 
	 * @param artifact
	 */
	public void addArtifact(IArtifact artifact) {
		addArtifactInternal(artifact);
	}
	
	private INode addArtifactInternal(IArtifact artifact) {
		if(artifact.isDirectory())
			return null;
		
		if(nodes.containsKey(artifact.getArchivePath()))
			return nodes.get(artifact.getArchivePath());
						
//		System.out.println("Adding new artifact: " + artifact.getPath());
		INode newNode = new ArtifactNode(artifact);
		nodes.put(artifact.getArchivePath(), newNode);
		return newNode;
	}
	
	/**
	 * Add an artifact to the graph with dependencies
	 * 
	 * @param artifact
	 * @param dependencies
	 */
	public INode addArtifact(IArtifact artifact, List<Dependency> dependencies) {
		if(artifact == null)
			throw new IllegalArgumentException("Artifact must not be null");

		// Don't store directories, they screw up the "unused artifact analysis"
		if(artifact.isDirectory())
			return null;
		
		INode node = addArtifactInternal(artifact);
		
		for(Dependency dep: dependencies) {
			// Find the node if it already exists, create otherwise
			INode depNode = nodes.get(dep.artifact.getArchivePath());
			if(depNode == null) {
				depNode = addArtifactInternal(dep.artifact);
			}
			
			// Add the links to the parent and dependent node
			// The traversal API will return the node itself as a depencency, do not link the node
			// to itself to prevent cycles in the graph.
			if(node != depNode) {
				Link link = new Link(node, depNode, dep.hard);
				node.addOutgoing(link);
				depNode.addIncoming(link);
			}
		}
		
		return node;
	}

	public boolean containsPath(String path) {
		return nodes.containsKey(path);
	}
	
	/**
	 * Merge the two nodes into one, keeping the primary node.
	 * 
	 * @param primary
	 * @param secondary
	 */
	public void mergeNodes(INode primary, INode secondary) {
		// Create new links between all nodes the secondary has outgoing links to and the primary
		for(Link link: secondary.getOutgoing()) {
			INode targetNode = link.getTarget();
			
			Link newLink = new Link(primary, targetNode, link.isHard());
			
			// Link the node to the primary node
			primary.addOutgoing(newLink);
			targetNode.addIncoming(newLink);
			
			// Unlink the node from the secondary node
			targetNode.removeIncoming(link);
		}
		
		// Create new links between all nodes that have links to the secondary to the primary
		for(Link link: secondary.getIncoming()) {
			INode sourceNode = link.getSource();
			Link newLink = new Link(sourceNode, primary, link.isHard());
			
			// Link the primary node to the node
			primary.addIncoming(newLink);
			sourceNode.addOutgoing(newLink);
			
			// Unlink the secondary node from the node
			sourceNode.removeOutgoing(link);
		}
		
		// Break any links between the primary and secondary node
		unlinkNodes(primary, secondary);
		
		// Remove the secondary node from the graph
		nodes.remove(secondary.getPath());
	}

	private void unlinkNodes(INode primary, INode secondary) {
		for(Iterator<Link> it = primary.getOutgoing().iterator(); it.hasNext();) {
			Link link = it.next();
			if(link.getTarget() == secondary)
				it.remove();
		}
		for(Iterator<Link> it = primary.getIncoming().iterator(); it.hasNext();) {
			Link link = it.next();
			if(link.getSource() == secondary)
				it.remove();
		}
	}
	
	/**
	 * Get a Node from the graph
	 */
	public INode getNode(String path) {
		return nodes.get(path);
	}	
	
	/**
	 * Get all nodes from the graph
	 */
	public Collection<INode> getAllNodes() {
		return Collections.unmodifiableCollection(
				new ArrayList<INode>(nodes.values()));
	}	

	public void setTopLevel(String path, boolean topLevel) {
		INode node = getNode(path);
		if(node == null)
			throw new IllegalArgumentException("Path " + path + " does not exist in the graph");
		setTopLevel(node, topLevel);
	}
	
	public void setTopLevel(INode node, boolean topLevel) {
		if(node == null)
			throw new IllegalArgumentException("Node must not be null");
		
		if(topLevel) {
			Link link = new Link(rootNode, node, false);
			rootNode.addOutgoing(link);
			node.addIncoming(link);
		} else {
			unlinkNodes(rootNode, node);
		}
	}
	
	/**
	 * Remove all direct links between nodes that are also indirectly linked.
	 */
	public void compressLinks() {
		for(INode n: nodes.values()) {
			n.compressLinks();
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

			INode n = nodes.get(path);
			
			w.println("<h3>Uses</h3>");
			for(Link link: sortList(n.getOutgoing())) {
				w.println("<a href=\"#" + link.getTarget().getPath().replace('/', '_') + "\">" + link.getTarget().getPath() + "</a><br>");
			}
			
			w.println("<h3>Used by</h3>");
			for(Link link: sortList(n.getIncoming())) {
				w.println("<a href=\"#" + link.getSource().getPath().replace('/', '_') + "\">" + link.getSource().getPath() + "</a><br>");
			}
		}
				
		w.println("</body></html>");
		
		w.close();
	}
	
	public void dumpToCSVFile(File file) throws IOException {
		PrintWriter w = new PrintWriter(new FileWriter(file));

		for(String path: nodes.keySet()) {
			for(Link l: nodes.get(path).getOutgoing()) {
				INode n = l.getTarget();
				w.print(path);
				w.print(";");
				w.print(n.getPath());
				w.println();
			}
		}
		
		w.close();
	}
	
	@SuppressWarnings("unchecked")
	private static <T extends Comparable> List<T> sortList(List<T> list) {
		List<T> newList = new ArrayList<T>(list);
		Collections.sort(newList);
		return newList;
	}

	public void printStats() {
		int numLinks = 0;
		int numOrphans = 0;
		
		for(INode n: nodes.values()) {
			numLinks += n.getOutgoing().size() + n.getIncoming().size();
			if(n.getIncoming().size() == 0)
				numOrphans++;
		}
		
		System.out.println(nodes.size() + " nodes with " + numLinks + " links. " + numOrphans + " nodes unused.");
	}	
	
	/**
	 * Finds all nodes that aren't being used by any other node. Except for top level
	 * nodes.
	 * 
	 * @return
	 */
	public List<INode> findUnused() {
		List<INode> result = new ArrayList<INode>();
		
		for(INode n: getAllNodes()) {
			if(n.getIncoming().size()==0)
				result.add(n);
		}
		
		return result;
	}

	/**
	 * Find all nodes that aren't being used by any other node and also the
	 * dependent nodes that aren't being used by any node that is being used.
	 * 
	 * @return
	 */
	public List<INode> findAllUnused() {
		List<INode> result = findUnused();
				
		return findAllUnused(result);
	}
		
	/**
	 * Find all nodes that are only used by other nodes in the unused nodes list.
	 * 
	 * @return A List<Node> with all unused nodes and their exclusive dependencies.
	 */
	public List<INode> findAllUnused(List<INode> unusedNodes) {
		List<INode> result = new ArrayList<INode>(unusedNodes);

		for(INode n: nodes.values()) {
			if(!result.contains(n) && n.unusedByAll(result))
				result.add(n);
		}

		// If we didn't add any more nodes in this pass, we're done. Otherwise, do another pass.
		if(result.size() == unusedNodes.size()) {
			return result;
		} else {
			return findAllUnused(result);
		}
	}
	
}
