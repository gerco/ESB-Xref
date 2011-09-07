package nl.progaia.esbxref.ui.infopanels;

import java.awt.BorderLayout;
import java.io.StringReader;
import java.util.List;

import javax.swing.JPanel;
import javax.swing.JScrollPane;
import javax.swing.JTable;
import javax.swing.table.DefaultTableModel;
import javax.xml.bind.JAXBContext;
import javax.xml.bind.JAXBElement;
import javax.xml.bind.JAXBException;
import javax.xml.bind.Unmarshaller;

import nl.progaia.esb.Connection;
import nl.progaia.esb.EndpointSchemaType;
import nl.progaia.esb.ParamsType.StringParam;
import nl.progaia.esb.ParamsType.XmlParam;
import nl.progaia.esbxref.dep.ArtifactNode;

public class ConnectionInfoPanel extends JPanel {

	private JAXBContext jaxb;
	private Unmarshaller unmarshaller;
	private DefaultTableModel tableModel = new DefaultTableModel();
	
	public ConnectionInfoPanel() {
		tableModel.setColumnCount(2);
		tableModel.setColumnIdentifiers(new Object[] {"Name", "Value"});
		JTable table = new JTable();
		table.setModel(tableModel);
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
	
	public void setConnection(ArtifactNode node) {
		tableModel.setRowCount(0);
		String xml = node.getArtifactXml();
		try {
			Connection endpoint = (Connection) unmarshaller.unmarshal(new StringReader(xml));
			List<Object> params = endpoint.getParams().getStringParamOrXmlParam();
			for(Object param: params) {
				if(param instanceof StringParam) {
					StringParam p = (StringParam)param;
					tableModel.addRow(new Object[] {p.getName(), p.getValue()});
				}
				else if(param instanceof XmlParam) {
					XmlParam p = (XmlParam)param;
					tableModel.addRow(new Object[] {p.getName(), "..."});
				}
			}
		} catch (JAXBException e) {
			throw new RuntimeException(e);
		}
	}
	
}
