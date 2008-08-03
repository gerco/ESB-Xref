package nl.progaia.esbxref.ui;

import java.awt.BorderLayout;
import java.util.TreeSet;

import javax.swing.JPanel;
import javax.swing.JScrollPane;
import javax.swing.JTree;
import javax.swing.tree.DefaultMutableTreeNode;
import javax.swing.tree.DefaultTreeModel;

import nl.progaia.esbxref.dep.DependencyGraph;
import nl.progaia.esbxref.dep.Node;

public class DepGraphPanel extends JPanel {
	private static final long serialVersionUID = 1L;
	
	private DependencyGraph graph;
	private DefaultMutableTreeNode rootNode;
	private DefaultTreeModel treeModel;
	private JTree artifactTree;
	
	public DepGraphPanel() {
		setLayout(new BorderLayout());
		initComponents();
	}

	private void initComponents() {
		rootNode = new DefaultMutableTreeNode("Configured objects");
		treeModel = new DefaultTreeModel(rootNode);
		artifactTree = new JTree(treeModel);
		artifactTree.setRootVisible(true);
		
		JScrollPane scrollPane = new JScrollPane(artifactTree,
				JScrollPane.VERTICAL_SCROLLBAR_ALWAYS,
				JScrollPane.HORIZONTAL_SCROLLBAR_ALWAYS);
		
		add(scrollPane);
	}
	
	public void setDependencyGraph(DependencyGraph graph) {
		this.graph = graph;
		
		rootNode.removeAllChildren();
		TreeSet<Node> sortedSet = new TreeSet<Node>();
		sortedSet.addAll(graph.getAllNodes());

		for(Node n: sortedSet) {
//			DefaultMutableTreeNode node = new DefaultMutableTreeNode(n);
			
			DefaultMutableTreeNode parent = rootNode;
			// Find the parent path, creating nodes as required
			String[] pathComponents = n.getPath().split("/");
			
			processPathComponents:
			for(String component: pathComponents) {
				if(component.length() == 0)
					continue;
								
				for(int i=0; i<parent.getChildCount(); i++) {
					if(parent.getChildAt(i).toString().equals(component)) {
						parent = (DefaultMutableTreeNode) parent.getChildAt(i);
						continue processPathComponents;
					}					
				}
				
				// Not found, create
				DefaultMutableTreeNode newParent = new DefaultMutableTreeNode(
						component.equals(pathComponents[pathComponents.length-1]) ? n : component);
				parent.add(newParent);
				parent = newParent;
			}
		}
		
		treeModel.reload();
	}
	
	public DependencyGraph getDependencyGraph() {
		return graph;
	}
}
