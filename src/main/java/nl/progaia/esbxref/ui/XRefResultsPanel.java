package nl.progaia.esbxref.ui;

import java.awt.BorderLayout;
import java.awt.event.MouseAdapter;
import java.awt.event.MouseEvent;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

import javax.swing.JComponent;
import javax.swing.JPanel;
import javax.swing.JScrollPane;
import javax.swing.JTabbedPane;
import javax.swing.JTable;
import javax.swing.SwingUtilities;

import com.sonicsw.deploy.artifact.ESBArtifact;

import nl.progaia.esbprocessdraw.ProcessRenderer;
import nl.progaia.esbprocessdraw.draw.esb.ESBProcess;
import nl.progaia.esbxref.dep.ArtifactNode;
import nl.progaia.esbxref.dep.INode;
import nl.progaia.esbxref.task.Task;
import nl.progaia.esbxref.task.TaskExecutor;
import nl.progaia.esbxref.ui.infopanels.EndpointInfoPanel;
import nl.progaia.esbxref.ui.infopanels.GenericInfoPanel;
import nl.progaia.esbxref.ui.infopanels.ProcessInfoPanel;
import nl.progaia.esbxref.ui.infopanels.ServiceInfoPanel;

public class XRefResultsPanel extends JPanel {
	private JTable usesTable;
	private JTable whereUsedTable;
	
	private JPanel infoPanel;
	private GenericInfoPanel genericInfoPanel = new GenericInfoPanel();
	private ProcessInfoPanel processInfoPanel = new ProcessInfoPanel();
	private EndpointInfoPanel endpointInfoPanel = new EndpointInfoPanel();
	private ServiceInfoPanel serviceInfoPanel = new ServiceInfoPanel();
	
	private NodeListTableModel usesTableModel;
	private NodeListTableModel whereUsedTableModel;
	
	private NodeSelectionListener selectionListener;
	private JTabbedPane tabbedPane;
	
	private TaskExecutor worker;
	
	public XRefResultsPanel(TaskExecutor worker) {
		this.worker = worker;
		setLayout(new BorderLayout());

		infoPanel = new JPanel();
		infoPanel.setLayout(new BorderLayout());
		
		// Create tablemodels
		usesTableModel = new NodeListTableModel();
		whereUsedTableModel = new NodeListTableModel();
		
		// Create tables
		usesTable = new JTable(usesTableModel);
		whereUsedTable = new JTable(whereUsedTableModel);

		// Set up listeners
		usesTable.addMouseListener(new DoubleClickListener(usesTable));
		whereUsedTable.addMouseListener(new DoubleClickListener(whereUsedTable));
		setSelectionListener(new NodeSelectionListener() {
			public void nodeSelected(INode node) {
				System.out.println("You double clicked " + node + " but I don't care");
			}
		});
		
		tabbedPane = new JTabbedPane();
		tabbedPane.addTab("Info", infoPanel);
		tabbedPane.addTab("Uses", 
				new JScrollPane(usesTable,
						JScrollPane.VERTICAL_SCROLLBAR_ALWAYS,
						JScrollPane.HORIZONTAL_SCROLLBAR_ALWAYS));
		tabbedPane.addTab("Where used", 
			new JScrollPane(whereUsedTable,
					JScrollPane.VERTICAL_SCROLLBAR_ALWAYS,
					JScrollPane.HORIZONTAL_SCROLLBAR_ALWAYS));
		
		add(tabbedPane, BorderLayout.CENTER);
		displayNullNode();
	}
	
	public void setDisplayedNode(INode displayedNode) {
		if(displayedNode == null) {
			displayNullNode();
			return;
		}
		
		tabbedPane.setEnabledAt(1, true);
		usesTableModel.setData(displayedNode.getIUse());
		tabbedPane.setEnabledAt(2, true);
		whereUsedTableModel.setData(displayedNode.getUsedBy());

		if(displayedNode instanceof ArtifactNode) {
			final ArtifactNode artifactNode = (ArtifactNode)displayedNode;
			tabbedPane.setEnabledAt(0, true);
			
			if(artifactNode.getPath().startsWith(ESBArtifact.PROCESS.getArchivePath())) {
				Task t = new Task() {
					@Override
					public void execute() throws Exception {
						final ESBProcess p = ProcessRenderer.unmarshalProcess(artifactNode.getArtifactXml());
						SwingUtilities.invokeLater(new Runnable() {
							public void run() {
								processInfoPanel.setProcess(p);
								setInfoComponent(processInfoPanel);
							}
						});
					}
					@Override
					public String toString() {
						return "Loading process";
					}
				};
				worker.execute(t);
			} else if(artifactNode.getPath().startsWith(ESBArtifact.ENDPOINT.getArchivePath())) {
				endpointInfoPanel.setEndpoint(artifactNode);
				setInfoComponent(endpointInfoPanel);
			} else if(artifactNode.getPath().startsWith(ESBArtifact.SERVICE.getArchivePath())) {
				serviceInfoPanel.setService(artifactNode);
				setInfoComponent(serviceInfoPanel);
			} else {
				genericInfoPanel.setNode(artifactNode);
				setInfoComponent(genericInfoPanel);
			}
		}
	}

	private void setInfoComponent(JComponent component) {
		infoPanel.removeAll();
		if(component != null)
			infoPanel.add(component, BorderLayout.CENTER);
		infoPanel.revalidate();
	}
	
	private void displayNullNode() {
		tabbedPane.setEnabledAt(0, false);
		tabbedPane.setEnabledAt(1, false);
		tabbedPane.setEnabledAt(2, false);
		setInfoComponent(null);
		usesTableModel.setData(null);
		whereUsedTableModel.setData(null);
	}
	
	public void setSelectionListener(NodeSelectionListener selectionListener) {
		this.selectionListener = selectionListener;
	}

	public NodeSelectionListener getSelectionListener() {
		return selectionListener;
	}
	
	private class DoubleClickListener extends MouseAdapter {

		private final JTable table;
		
		public DoubleClickListener(JTable table) {
			this.table = table;
		}
		
		@Override
		public void mouseClicked(MouseEvent e) {
			if(e.getClickCount() == 2) {
				if(table.getSelectedRow() != -1) {
					INode node = 
						((NodeListTableModel)table.getModel())
							.getRowItem(table.getSelectedRow());
					
					selectionListener.nodeSelected(node);
				}
			}
		}
		
	}
	
	private class NodeListTableModel extends ListTableModel<INode> {

		public NodeListTableModel() {
			columnNames = new String[] {"Name"};
			columnTypes = new Class[] {String.class};
		}
		
		@Override
		public void setData(List<INode> data) {
			if(data != null) {
				ArrayList<INode> copy = new ArrayList<INode>(data);
				Collections.sort(copy);
				super.setData(copy);
			} else {
				super.setData(null);
			}
		}
		
		@Override
		public Object getColumnValue(INode item, int columnIndex) {
			switch(columnIndex) {
			case 0:
				return item.getPath();
				
			default:
				return null;
			}
		}
		
	}
}
