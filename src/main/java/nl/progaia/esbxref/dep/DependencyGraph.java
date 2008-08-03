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
import java.util.List;
import java.util.TreeSet;


import com.sonicsw.deploy.IArtifact;
import com.sonicsw.deploy.artifact.ESBArtifact;

public class DependencyGraph implements Serializable {
	public static final long serialVersionUID = 1;
	
	private final HashMap<String, Node> nodes = new HashMap<String, Node>();
	
	/**
	 * Add an artifact to the graph without dependencies
	 * 
	 * @param artifact
	 */
	public void addArtifact(IArtifact artifact) {
		addArtifactInternal(artifact);
	}
	
	private Node addArtifactInternal(IArtifact artifact) {
		if(artifact.isDirectory())
			return null;
		
		if(nodes.containsKey(artifact.getArchivePath()))
			return nodes.get(artifact.getArchivePath());
		
		// If the artifact is a service that belongs with a process, merge the two
		if(ESBArtifact.SERVICE.getArchivePath().equals(artifact.getArchiveParentPath())) {
			String processName = ESBArtifact.PROCESS.getArchivePath() + artifact.getName();
			if(nodes.containsKey(processName)) {
//				System.out.println("Merged " + artifact.getPath() + " with " + processName);
				return nodes.get(processName);
			}
		}
				
//		System.out.println("Adding new artifact: " + artifact.getPath());
		Node newNode = new Node(artifact);
		nodes.put(artifact.getArchivePath(), newNode);
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

		// Don't store directories, they screw up the "unused artifact analysis"
		if(artifact.isDirectory())
			return;
		
		Node node = addArtifactInternal(artifact);
		
		for(IArtifact dep: dependencies) {
			// Find the node if it already exists, create otherwise
			Node depNode = nodes.get(dep.getArchivePath());
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
	 * Get a Node from the graph
	 */
	public Node getNode(String path) {
		return nodes.get(path);
	}	
	
	/**
	 * Get all nodes from the graph
	 */
	public Collection<Node> getAllNodes() {
		return Collections.unmodifiableCollection(nodes.values());
	}	
	
	/**
	 * Remove all direct links between nodes that are also indirectly linked.
	 */
	public void compressLinks() {
		for(Node n: nodes.values()) {
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

			Node n = nodes.get(path);
			
			w.println("<h3>Uses</h3>");
			for(Node dep: sortList(n.getIUse())) {
				w.println("<a href=\"#" + dep.getPath().replace('/', '_') + "\">" + dep.getPath() + "</a><br>");
			}
			
			w.println("<h3>Used by</h3>");
			for(Node dep: sortList(n.getUsedBy())) {
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
	
	@SuppressWarnings("unchecked")
	private static <T extends Comparable> List<T> sortList(List<T> list) {
		List<T> newList = new ArrayList<T>(list);
		Collections.sort(newList);
		return newList;
	}

	public void printStats() {
		int numLinks = 0;
		int numOrphans = 0;
		
		for(Node n: nodes.values()) {
			numLinks += n.getIUse().size() + n.getUsedBy().size();
			if(n.getUsedBy().size() == 0)
				numOrphans++;
		}
		
		System.out.println(nodes.size() + " nodes with " + numLinks + " links. " + numOrphans + " nodes unused.");
	}	
}
