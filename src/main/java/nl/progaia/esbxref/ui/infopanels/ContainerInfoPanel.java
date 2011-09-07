package nl.progaia.esbxref.ui.infopanels;

import java.awt.BorderLayout;
import java.awt.Component;
import java.io.StringReader;
import java.util.List;

import javax.swing.Icon;
import javax.swing.ImageIcon;
import javax.swing.JLabel;
import javax.swing.JPanel;
import javax.swing.JScrollPane;
import javax.swing.JTable;
import javax.swing.table.DefaultTableCellRenderer;
import javax.swing.table.DefaultTableModel;
import javax.xml.bind.JAXBContext;
import javax.xml.bind.JAXBException;
import javax.xml.bind.Unmarshaller;

import com.sonicsw.deploy.artifact.ESBArtifact;

import nl.progaia.esb.EndpointSchemaType;
import nl.progaia.esb.XQCONTAINER;
import nl.progaia.esb.XQCONTAINER.Services.ServiceApplication;
import nl.progaia.esbxref.dep.ArtifactNode;
import nl.progaia.esbxref.dep.INode;
import nl.progaia.esbxref.dep.Link;

public class ContainerInfoPanel extends JPanel {

	private JAXBContext jaxb;
	private Unmarshaller unmarshaller;
	private DefaultTableModel tableModel = new DefaultTableModel();
	
	public ContainerInfoPanel() {
		tableModel.setColumnCount(2);
		tableModel.setColumnIdentifiers(new Object[] {"Name", "Instances"});
		
		JTable table = new JTable();
		table.setModel(tableModel);
		table.getColumnModel().getColumn(0).setCellRenderer(new RowTypeCellRenderer());
		setLayout(new BorderLayout());
		add(new JScrollPane(table, JScrollPane.VERTICAL_SCROLLBAR_AS_NEEDED, JScrollPane.HORIZONTAL_SCROLLBAR_NEVER),
			BorderLayout.CENTER);
		
		try {
			jaxb = JAXBContext.newInstance(EndpointSchemaType.class);
			unmarshaller = jaxb.createUnmarshaller();
		} catch (JAXBException e) {
			throw new RuntimeException(e);
		}
	}
	
	public void setContainer(ArtifactNode node) {
		tableModel.setRowCount(0);
		String xml = node.getArtifactXml();
		try {
			XQCONTAINER container = (XQCONTAINER) unmarshaller.unmarshal(new StringReader(xml));
			List<ServiceApplication> services = container.getServices().getServiceApplication();
			for(ServiceApplication service: services) {
				tableModel.addRow(new Object[] {
					findNodeWithName(node.getOutgoingTargets(), service.getName()),
					service.getInstances()});
			}
		} catch (JAXBException e) {
			throw new RuntimeException(e);
		}
	}
	
	private INode findNodeWithName(List<INode> targetNodes, String name) {
		for(INode targetNode: targetNodes) {
			if(targetNode.getName().equals(name))
				return targetNode;
		}
		
		System.out.println("Can't find " + name);
		
		return null;
	}
	
	private static class RowTypeCellRenderer extends DefaultTableCellRenderer {

		private final Icon SERVICE_ICON = new ImageIcon(getClass().getResource("../icons/service.gif"));
		private final Icon PROCESS_ICON = new ImageIcon(getClass().getResource("../icons/esbp.gif"));
		private final Icon UNKNOWN_ICON = new ImageIcon(getClass().getResource("../icons/xslt.gif"));
		
		@Override
		public Component getTableCellRendererComponent(JTable table, Object value, boolean isSelected, boolean hasFocus, int row, int column) {
			Component c = super.getTableCellRendererComponent(table, value, isSelected, hasFocus, row, column);
			
			if(c instanceof JLabel && value instanceof INode) {
				JLabel label = (JLabel)c;
				label.setIcon(getIconFor((INode)value));
			}
			
			return c;
		}

		private Icon getIconFor(INode value) {
			String path = value.getPath();
			if(path.startsWith(ESBArtifact.SERVICE.getArchivePath()))
				return SERVICE_ICON;
			else if(path.startsWith(ESBArtifact.PROCESS.getArchivePath()))
				return PROCESS_ICON;
			else
				return UNKNOWN_ICON;
		}
		
	}
}
