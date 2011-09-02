package nl.progaia.esbxref.ui.infopanels;

import javax.swing.DefaultListModel;

import nl.progaia.esb.EndpointRefType;
import nl.progaia.esb.Process;
import nl.progaia.esbprocessdraw.draw.esb.ESBProcess;

public class ProcessInfoPanel extends ProcessInfoPanelUI {

	public void setProcess(ESBProcess p) {
		Process process = p.getProcess();
		getProcessNameField().setText(process.getName());
		getEntryEndpointField().setText(process.getEntryRef());
		getFaultEndpointField().setText(process.getFaultEndpoint().getEndpointRef());
		getRmeEndpointField().setText(process.getRejectEndpoint().getEndpointRef());
		
		DefaultListModel model = new DefaultListModel();
		for(EndpointRefType endpoint: process.getExitEndpointList().getExitEndpoint()) {
			model.addElement(endpoint.getEndpointRef());
		}
		getExitEndpointList().setModel(model);
		
		getProcessViewPanel().setProcess(p);
	}

}
