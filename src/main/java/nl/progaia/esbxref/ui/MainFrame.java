package nl.progaia.esbxref.ui;

import java.util.TreeSet;

import javax.swing.JFrame;
import javax.swing.JScrollPane;
import javax.swing.JTree;
import javax.swing.tree.DefaultMutableTreeNode;
import javax.swing.tree.DefaultTreeModel;

import nl.progaia.esbxref.DependencyGraph;
import nl.progaia.esbxref.Node;

public class MainFrame extends JFrame {
	
	private static final long serialVersionUID = 1L;
	
	private final DependencyGraph graph;
	private DefaultMutableTreeNode rootNode;
	private DefaultTreeModel treeModel;
	private JTree artifactTree;
	
	public MainFrame(DependencyGraph graph) {
		super("Sonic ESB Cross Reference Tool");
		this.graph = graph;
		initComponents();
		fillTree();
	}

	private void initComponents() {
		rootNode = new DefaultMutableTreeNode("Configured objects");
		treeModel = new DefaultTreeModel(rootNode);
		artifactTree = new JTree(treeModel);
		artifactTree.setRootVisible(true);
		
		JScrollPane scrollPane = new JScrollPane(artifactTree,
				JScrollPane.VERTICAL_SCROLLBAR_ALWAYS,
				JScrollPane.HORIZONTAL_SCROLLBAR_ALWAYS);
		
		getContentPane().add(scrollPane);
	}
	
	private void fillTree() {
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
				
				System.out.println("C " + component);
				
				
				for(int i=0; i<parent.getChildCount(); i++) {
					System.out.println("N " + parent.getChildAt(i).toString());
					if(parent.getChildAt(i).toString().equals(component)) {
						parent = (DefaultMutableTreeNode) parent.getChildAt(i);
						continue processPathComponents;
					}					
				}
				
				// Not found, create
				System.out.println("Creating new node " + component);
				DefaultMutableTreeNode newParent = new DefaultMutableTreeNode(
						component.equals(pathComponents[pathComponents.length-1]) ? n : component);
				parent.add(newParent);
				parent = newParent;
			}
		}
	}
}
