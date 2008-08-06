package nl.progaia.esbxref.ui;

import java.awt.BorderLayout;

import javax.swing.JPanel;
import javax.swing.JScrollPane;
import javax.swing.JTabbedPane;
import javax.swing.JTable;

import nl.progaia.esbxref.dep.INode;

public class XRefResultsPanel extends JPanel {

	private JTable usesTable;
	private JTable whereUsedTable;
	
	private NodeListTableModel usesTableModel;
	private NodeListTableModel whereUsedTableModel;
	
	public XRefResultsPanel() {
		setLayout(new BorderLayout());
		
		// Create tablemodels
		usesTableModel = new NodeListTableModel();
		whereUsedTableModel = new NodeListTableModel();
		
		// Create tables
		usesTable = new JTable(usesTableModel);
		whereUsedTable = new JTable(whereUsedTableModel);
		
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
		
		add(tabbedPane, BorderLayout.CENTER);
	}
	
	public void setDisplayedNode(INode displayedNode) {
		if(displayedNode != null) {
			usesTableModel.setData(displayedNode.getIUse());
			whereUsedTableModel.setData(displayedNode.getUsedBy());
		} else {
			usesTableModel.setData(null);
			whereUsedTableModel.setData(null);
		}
	}
	
	private class NodeListTableModel extends ListTableModel<INode> {

		public NodeListTableModel() {
			columnNames = new String[] {"Name"};
			columnTypes = new Class[] {String.class};
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
