package nl.progaia.esbxref.ui;

import java.awt.BorderLayout;
import java.awt.Component;
import java.util.TreeSet;

import javax.swing.ImageIcon;
import javax.swing.JPanel;
import javax.swing.JScrollPane;
import javax.swing.JTree;
import javax.swing.event.TreeSelectionEvent;
import javax.swing.event.TreeSelectionListener;
import javax.swing.tree.DefaultMutableTreeNode;
import javax.swing.tree.DefaultTreeCellRenderer;
import javax.swing.tree.DefaultTreeModel;
import javax.swing.tree.TreeNode;
import javax.swing.tree.TreePath;
import javax.swing.tree.TreeSelectionModel;

import nl.progaia.esbxref.dep.ArtifactNode;
import nl.progaia.esbxref.dep.DependencyGraph;
import nl.progaia.esbxref.dep.INode;

import com.sonicsw.deploy.artifact.ESBArtifact;

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
		artifactTree.setExpandsSelectedPaths(true);
		artifactTree.getSelectionModel().setSelectionMode(TreeSelectionModel.SINGLE_TREE_SELECTION);
		artifactTree.setCellRenderer(new ArtifactTreeCellRenderer());
		
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
	
	public void selectNode(INode node) {
		TreePath treePath = new TreePath(rootNode);
		String[] nodePath = node.getPath().split("/");
		
		for(String nodePathComponent: nodePath) {
			TreeNode n = (TreeNode)treePath.getLastPathComponent();
			
			if(!n.isLeaf()) {
				walkChildren:
				for(int i=0; i < n.getChildCount(); i++) {
					if(nodePathComponent.equals(n.getChildAt(i).toString())) {
						treePath = treePath.pathByAddingChild(n.getChildAt(i));
						break walkChildren;
					}
				}
			}
		}
		
		artifactTree.setSelectionPath(treePath);
		artifactTree.scrollPathToVisible(treePath);
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
	
	private class ArtifactTreeCellRenderer extends DefaultTreeCellRenderer {
		
		// ESB Icons
		private ImageIcon ICON_CONTAINER = 
			new ImageIcon(getClass().getResource("icons/container.gif"));
		private ImageIcon ICON_ESBP = 
			new ImageIcon(getClass().getResource("icons/esbp.gif"));
		private ImageIcon ICON_ESBSTYP = 
			new ImageIcon(getClass().getResource("icons/service_type.gif"));
		private ImageIcon ICON_SERVICE = 
			new ImageIcon(getClass().getResource("icons/service.gif"));
		private ImageIcon ICON_ENDPOINT = 
			new ImageIcon(getClass().getResource("icons/endpoint.gif"));
		
		// SonicFS icons
		private ImageIcon ICON_CBR = 
			new ImageIcon(getClass().getResource("icons/cbr.gif"));
		private ImageIcon ICON_XCBR = 
			new ImageIcon(getClass().getResource("icons/xcbr.gif"));
		private ImageIcon ICON_XML = 
			new ImageIcon(getClass().getResource("icons/xml.gif"));
		private ImageIcon ICON_XSD = 
			new ImageIcon(getClass().getResource("icons/xsd.gif"));
		private ImageIcon ICON_XSLT = 
			new ImageIcon(getClass().getResource("icons/xslt.gif"));
		private ImageIcon ICON_WSDL = 
			new ImageIcon(getClass().getResource("icons/wsdl.gif"));
		private ImageIcon ICON_JS = 
			new ImageIcon(getClass().getResource("icons/js.gif"));
		
		private ImageIcon ICON_FILE = 
			new ImageIcon(getClass().getResource("icons/file.gif"));
		
        public Component getTreeCellRendererComponent(
        		JTree tree, Object value, boolean sel, boolean expanded,
                boolean leaf, int row, boolean hasFocus) {

				super.getTreeCellRendererComponent(
						tree, value, sel, expanded, leaf, row, hasFocus);
			if (leaf) {
				Object userObject = ((DefaultMutableTreeNode)value).getUserObject();
				if(userObject instanceof INode) {
					String path = ((INode)userObject).getPath();
					if(path.startsWith(ESBArtifact.PROCESS.getArchivePath()))
						setIcon(ICON_ESBP);
					else if(path.startsWith(ESBArtifact.CONTAINER.getArchivePath()))
						setIcon(ICON_CONTAINER);
					else if(path.startsWith(ESBArtifact.SERVICE_TYPE.getArchivePath()))
						setIcon(ICON_ESBSTYP);
					else if(path.startsWith(ESBArtifact.SERVICE.getArchivePath()))
						setIcon(ICON_SERVICE);
					else if(path.startsWith(ESBArtifact.ENDPOINT.getArchivePath()))
						setIcon(ICON_ENDPOINT);
					else if(path.toLowerCase().endsWith(".xml"))
						setIcon(ICON_XML);
					else if(path.toLowerCase().endsWith(".xsd"))
						setIcon(ICON_XSD);
					else if(path.toLowerCase().endsWith(".cbr"))
						setIcon(ICON_CBR);
					else if(path.toLowerCase().endsWith(".xcbr"))
						setIcon(ICON_XCBR);
					else if(path.toLowerCase().endsWith(".xslt") || path.endsWith(".xsl"))
						setIcon(ICON_XSLT);
					else if(path.toLowerCase().endsWith(".wsdl"))
						setIcon(ICON_WSDL);
					else if(path.toLowerCase().endsWith(".js"))
						setIcon(ICON_JS);
					else
						setIcon(ICON_FILE);
				}				
			} else {
			    setToolTipText(null); //no tool tip
			}
			
			return this;
		}
	}
}
