package nl.progaia.esbxref.ui;

import java.awt.BorderLayout;
import java.awt.event.MouseAdapter;
import java.awt.event.MouseEvent;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

import javax.swing.JPanel;
import javax.swing.JScrollPane;
import javax.swing.JTabbedPane;
import javax.swing.JTable;

import nl.progaia.esbprocessdraw.ProcessRenderer;
import nl.progaia.esbprocessdraw.draw.esb.ESBProcess;
import nl.progaia.esbxref.dep.ArtifactNode;
import nl.progaia.esbxref.dep.INode;

public class XRefResultsPanel extends JPanel {

	private JTable usesTable;
	private JTable whereUsedTable;
	private ESBProcessPanel viewArea;
	
	private NodeListTableModel usesTableModel;
	private NodeListTableModel whereUsedTableModel;
	
	private NodeSelectionListener selectionListener;
	
	public XRefResultsPanel() {
		setLayout(new BorderLayout());
		
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
		
		// Create the view area
		viewArea = new ESBProcessPanel();
		
		// Now add the tables to a tabbedpane
		JTabbedPane tabbedPane = new JTabbedPane();
		tabbedPane.addTab("Uses", 
				new JScrollPane(usesTable,
						JScrollPane.VERTICAL_SCROLLBAR_ALWAYS,
						JScrollPane.HORIZONTAL_SCROLLBAR_ALWAYS));
		tabbedPane.addTab("Where used", 
			new JScrollPane(whereUsedTable,
					JScrollPane.VERTICAL_SCROLLBAR_ALWAYS,
					JScrollPane.HORIZONTAL_SCROLLBAR_ALWAYS));
		tabbedPane.addTab("View",
			new JScrollPane(viewArea,
					JScrollPane.VERTICAL_SCROLLBAR_ALWAYS,
					JScrollPane.HORIZONTAL_SCROLLBAR_ALWAYS));
		
		add(tabbedPane, BorderLayout.CENTER);
	}
	
	public void setDisplayedNode(INode displayedNode) {
		if(displayedNode != null) {
			usesTableModel.setData(displayedNode.getIUse());
			whereUsedTableModel.setData(displayedNode.getUsedBy());
			
			if(displayedNode instanceof ArtifactNode) {
				ESBProcess p = ProcessRenderer.unmarshalProcess(
						((ArtifactNode)displayedNode).getArtifactXml());
				viewArea.setProcess(p);
			} else {
				viewArea.setProcess(null);
			}
		} else {
			usesTableModel.setData(null);
			whereUsedTableModel.setData(null);
			viewArea.setProcess(null);
		}
	}
	
	public void setSelectionListener(NodeSelectionListener selectionListener) {
		this.selectionListener = selectionListener;
	}

	public NodeSelectionListener getSelectionListener() {
		return selectionListener;
	}

	public interface NodeSelectionListener {
		public void nodeSelected(INode node);
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
