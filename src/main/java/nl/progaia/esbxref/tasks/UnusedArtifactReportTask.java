package nl.progaia.esbxref.tasks;

import java.io.File;
import java.io.FileWriter;
import java.io.PrintWriter;
import java.util.Collection;
import java.util.List;

import com.sonicsw.deploy.artifact.ESBArtifact;

import nl.progaia.esbxref.dep.DependencyGraph;
import nl.progaia.esbxref.dep.INode;
import nl.progaia.esbxref.task.Task;

public class UnusedArtifactReportTask extends Task {

	private final DependencyGraph graph;
	private final File file;
	
	public UnusedArtifactReportTask(DependencyGraph graph, File file) {
		this.graph = graph;
		this.file = file;
	}
	
	@Override
	public void execute() throws Exception {
		// Set all ESB containers as top level artifacts
		Collection<INode> allNodes = graph.getAllNodes();
		for(INode n: allNodes) {
			if(n.getPath().startsWith(ESBArtifact.CONTAINER.getArchivePath())) {
				graph.setTopLevel(n, true);
			}
		}
		
		List<INode> unusedNodes = graph.findAllUnused();
		
		FileWriter fw = new FileWriter(file);
		PrintWriter pw = new PrintWriter(fw);
		
		for(INode n: allNodes) {
			pw.print(unusedNodes.contains(n)? " ;" : "x;");
			pw.println(n.getPath());
		}
		
		pw.close();
		fw.close();
	}

	@Override
	public String toString(){ 
		return "Finding unused artifacts";
	}
}
