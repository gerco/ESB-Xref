package nl.progaia.esbxref.ui;

import java.awt.BorderLayout;
import java.util.TreeSet;

import javax.swing.JPanel;
import javax.swing.JScrollPane;
import javax.swing.JTree;
import javax.swing.event.TreeSelectionEvent;
import javax.swing.event.TreeSelectionListener;
import javax.swing.tree.DefaultMutableTreeNode;
import javax.swing.tree.DefaultTreeModel;
import javax.swing.tree.TreePath;
import javax.swing.tree.TreeSelectionModel;

import nl.progaia.esbxref.dep.DependencyGraph;
import nl.progaia.esbxref.dep.ArtifactNode;
import nl.progaia.esbxref.dep.INode;

public class DepGraphPanel extends JPanel {
	public interface DepGraphSelectionListener {
		public void nodeSelected(INode selectedNode);
	}

	private static final long serialVersionUID = 1L;
	
	private DependencyGraph graph;
	private DefaultMutableTreeNode rootNode;
	private DefaultTreeModel treeModel;
	private JTree artifactTree;
	
	private DepGraphSelectionListener selectionListener;
	
	public DepGraphPanel() {
		setLayout(new BorderLayout());
		
		// Set up a dummy selectionListener
		selectionListener = new DepGraphSelectionListener() {
			public void nodeSelected(INode selectedNode) {
				System.out.println("Node " + selectedNode + " selected, but no-one cared!");
			}
		};
		
		initComponents();
	}

	private void initComponents() {
		rootNode = new DefaultMutableTreeNode("Configured objects");
		treeModel = new DefaultTreeModel(rootNode);
		
		artifactTree = new JTree(treeModel);
		artifactTree.setRootVisible(true);
		artifactTree.getSelectionModel().setSelectionMode(TreeSelectionModel.SINGLE_TREE_SELECTION);
		
		
		artifactTree.getSelectionModel().addTreeSelectionListener(new TreeSelectionListener() {
			public void valueChanged(TreeSelectionEvent e) {
				TreePath selectedPath = e.getPath();
				if(selectedPath != null) {
					DefaultMutableTreeNode selectedNode = (DefaultMutableTreeNode)selectedPath.getLastPathComponent();
					Object userObject = selectedNode.getUserObject();
					
					if(userObject instanceof ArtifactNode) {
						selectionListener.nodeSelected((INode)userObject);
						return;
					}
				}
				
				selectionListener.nodeSelected(null);
			}
		});
		
		JScrollPane scrollPane = new JScrollPane(artifactTree,
				JScrollPane.VERTICAL_SCROLLBAR_ALWAYS,
				JScrollPane.HORIZONTAL_SCROLLBAR_ALWAYS);
		
		add(scrollPane);
	}
	
	public void setDependencyGraph(DependencyGraph graph) {
		this.graph = graph;
		
		rootNode.removeAllChildren();
		TreeSet<INode> sortedSet = new TreeSet<INode>();
		sortedSet.addAll(graph.getAllNodes());

		for(INode n: sortedSet) {
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

	public void setSelectionListener(DepGraphSelectionListener selectionListener) {
		this.selectionListener = selectionListener;
	}

	public DepGraphSelectionListener getSelectionListener() {
		return selectionListener;
	}
}
