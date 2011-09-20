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

import nl.progaia.esb.EndpointRefType;
import nl.progaia.esb.EndpointSchemaType;
import nl.progaia.esb.Service;
import nl.progaia.esb.ServiceType;
import nl.progaia.esb.ParamsType.StringParam;
import nl.progaia.esb.ParamsType.XmlParam;
import nl.progaia.esb.Service.ExitEndpointList;
import nl.progaia.esbxref.dep.ArtifactNode;

public class ServiceInfoPanel extends JPanel {
	private JAXBContext jaxb;
	private Unmarshaller unmarshaller;
	private DefaultTableModel tableModel = new DefaultTableModel();
	
	public ServiceInfoPanel() {
		tableModel.setColumnCount(2);
		tableModel.setColumnIdentifiers(new Object[] {"Name", "Value"});
		JTable table = new JTable();
		table.setModel(tableModel);
		setLayout(new BorderLayout());
		add(new JScrollPane(table, JScrollPane.VERTICAL_SCROLLBAR_AS_NEEDED, JScrollPane.HORIZONTAL_SCROLLBAR_NEVER),
			BorderLayout.CENTER);
		
		try {
			jaxb = JAXBContext.newInstance(Service.class);
			unmarshaller = jaxb.createUnmarshaller();
		} catch (JAXBException e) {
			throw new RuntimeException(e);
		}
	}
	
	public void setService(ArtifactNode node) {
		tableModel.setRowCount(0);
		String xml = node.getArtifactXml();
		try {
			Service service = (Service) unmarshaller.unmarshal(new StringReader(xml));
			tableModel.addRow(new Object[] {"Service name", service.getName()});
			tableModel.addRow(new Object[] {"Service type", service.getTypeRef()});
			tableModel.addRow(new Object[] {"Entry endpoint", service.getEntryRef()});
			tableModel.addRow(new Object[] {"Exit endpoints", getExitEndpoints(service)});
			tableModel.addRow(new Object[] {"Fault endpoint", getEndpointRef(service.getFaultEndpoint())});
			tableModel.addRow(new Object[] {"RME endpoint", getEndpointRef(service.getRejectEndpoint())});
			List<Object> params = service.getParams().getStringParamOrXmlParam();
			for(Object param: params) {
				if(param instanceof StringParam) {
					StringParam p = (StringParam)param;
					tableModel.addRow(new Object[] {p.getName(), p.getValue()});
				}
				else if(param instanceof XmlParam) {
					XmlParam p = (XmlParam)param;
					tableModel.addRow(new Object[] {p.getName(), "--- Xml params not yet supported ---"});
				}
			}
		} catch (JAXBException e) {
			throw new RuntimeException(e);
		}
	}

	private String getEndpointRef(EndpointRefType endpoint) {
		if(endpoint != null)
			return endpoint.getEndpointRef();
		return "";
	}

	private String getExitEndpoints(Service service) {
		ExitEndpointList list = service.getExitEndpointList();
		if(list != null) {
			List<EndpointRefType> endpoints = list.getExitEndpoint();
			StringBuilder result = new StringBuilder();
			for(EndpointRefType ref: endpoints) {
				result.append(", ");
				result.append(ref.getEndpointRef());
			}
			return result.substring(2);
		}
		
		return "";
	}
		
}
