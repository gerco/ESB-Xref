package nl.progaia.esbxref.ui.infopanels;

import java.awt.GridBagConstraints;
import java.awt.GridBagLayout;
import java.awt.Insets;

import javax.swing.JLabel;
import javax.swing.JList;
import javax.swing.JPanel;
import javax.swing.JTextField;
import javax.swing.ListModel;
import javax.swing.border.EmptyBorder;
import nl.progaia.esbxref.ui.ESBProcessPanel;
import javax.swing.JScrollPane;


public class ProcessInfoPanelUI extends JPanel {
	private JTextField processNameField;
	private JTextField entryEndpointField;
	private JList exitEndpointList;
	private JTextField faultEndpointField;
	private JTextField rmeEndpointField;
	private ESBProcessPanel processViewPanel;
	private JScrollPane scrollPane;

	public ProcessInfoPanelUI() {
		setBorder(new EmptyBorder(5, 5, 5, 5));
		GridBagLayout gridBagLayout = new GridBagLayout();
		gridBagLayout.columnWidths = new int[]{0, 0, 0};
		gridBagLayout.rowHeights = new int[]{0, 0, 0, 0, 0, 0, 0};
		gridBagLayout.columnWeights = new double[]{0.0, 1.0, Double.MIN_VALUE};
		gridBagLayout.rowWeights = new double[]{0.0, 0.0, 0.0, 0.0, 0.0, 1.0, Double.MIN_VALUE};
		setLayout(gridBagLayout);
		
		JLabel lblProcessName = new JLabel("Process name");
		GridBagConstraints gbc_lblProcessName = new GridBagConstraints();
		gbc_lblProcessName.insets = new Insets(0, 0, 5, 5);
		gbc_lblProcessName.anchor = GridBagConstraints.WEST;
		gbc_lblProcessName.gridx = 0;
		gbc_lblProcessName.gridy = 0;
		add(lblProcessName, gbc_lblProcessName);
		
		processNameField = new JTextField("");
		processNameField.setEditable(false);
		lblProcessName.setLabelFor(processNameField);
		GridBagConstraints gbc_processNameField = new GridBagConstraints();
		gbc_processNameField.fill = GridBagConstraints.HORIZONTAL;
		gbc_processNameField.insets = new Insets(0, 0, 5, 0);
		gbc_processNameField.gridx = 1;
		gbc_processNameField.gridy = 0;
		add(processNameField, gbc_processNameField);
		
		JLabel lblEntryEndpoint = new JLabel("Entry endpoint");
		GridBagConstraints gbc_lblEntryEndpoint = new GridBagConstraints();
		gbc_lblEntryEndpoint.anchor = GridBagConstraints.WEST;
		gbc_lblEntryEndpoint.insets = new Insets(0, 0, 5, 5);
		gbc_lblEntryEndpoint.gridx = 0;
		gbc_lblEntryEndpoint.gridy = 1;
		add(lblEntryEndpoint, gbc_lblEntryEndpoint);
		
		entryEndpointField = new JTextField("");
		entryEndpointField.setEditable(false);
		lblEntryEndpoint.setLabelFor(entryEndpointField);
		GridBagConstraints gbc_entryEndpointField = new GridBagConstraints();
		gbc_entryEndpointField.fill = GridBagConstraints.HORIZONTAL;
		gbc_entryEndpointField.insets = new Insets(0, 0, 5, 0);
		gbc_entryEndpointField.gridx = 1;
		gbc_entryEndpointField.gridy = 1;
		add(entryEndpointField, gbc_entryEndpointField);
		
		JLabel lblExitEndpoints = new JLabel("Exit endpoints");
		GridBagConstraints gbc_lblExitEndpoints = new GridBagConstraints();
		gbc_lblExitEndpoints.anchor = GridBagConstraints.WEST;
		gbc_lblExitEndpoints.insets = new Insets(0, 0, 5, 5);
		gbc_lblExitEndpoints.gridx = 0;
		gbc_lblExitEndpoints.gridy = 2;
		add(lblExitEndpoints, gbc_lblExitEndpoints);
		
		exitEndpointList = new JList();
		lblExitEndpoints.setLabelFor(exitEndpointList);
		GridBagConstraints gbc_exitEndpointList = new GridBagConstraints();
		gbc_exitEndpointList.fill = GridBagConstraints.BOTH;
		gbc_exitEndpointList.insets = new Insets(0, 0, 5, 0);
		gbc_exitEndpointList.gridx = 1;
		gbc_exitEndpointList.gridy = 2;
		add(exitEndpointList, gbc_exitEndpointList);
		
		JLabel lblFaultEndpoint = new JLabel("Fault endpoint");
		GridBagConstraints gbc_lblFaultEndpoint = new GridBagConstraints();
		gbc_lblFaultEndpoint.anchor = GridBagConstraints.WEST;
		gbc_lblFaultEndpoint.insets = new Insets(0, 0, 5, 5);
		gbc_lblFaultEndpoint.gridx = 0;
		gbc_lblFaultEndpoint.gridy = 3;
		add(lblFaultEndpoint, gbc_lblFaultEndpoint);
		
		faultEndpointField = new JTextField("");
		faultEndpointField.setEditable(false);
		lblFaultEndpoint.setLabelFor(faultEndpointField);
		GridBagConstraints gbc_faultEndpointField = new GridBagConstraints();
		gbc_faultEndpointField.fill = GridBagConstraints.HORIZONTAL;
		gbc_faultEndpointField.insets = new Insets(0, 0, 5, 0);
		gbc_faultEndpointField.gridx = 1;
		gbc_faultEndpointField.gridy = 3;
		add(faultEndpointField, gbc_faultEndpointField);
		
		JLabel lblRmeEndpoint = new JLabel("RME endpoint");
		GridBagConstraints gbc_lblRmeEndpoint = new GridBagConstraints();
		gbc_lblRmeEndpoint.anchor = GridBagConstraints.WEST;
		gbc_lblRmeEndpoint.insets = new Insets(0, 0, 5, 5);
		gbc_lblRmeEndpoint.gridx = 0;
		gbc_lblRmeEndpoint.gridy = 4;
		add(lblRmeEndpoint, gbc_lblRmeEndpoint);
		
		rmeEndpointField = new JTextField("");
		rmeEndpointField.setEditable(false);
		lblRmeEndpoint.setLabelFor(rmeEndpointField);
		GridBagConstraints gbc_rmeEndpointField = new GridBagConstraints();
		gbc_rmeEndpointField.insets = new Insets(0, 0, 5, 0);
		gbc_rmeEndpointField.fill = GridBagConstraints.HORIZONTAL;
		gbc_rmeEndpointField.gridx = 1;
		gbc_rmeEndpointField.gridy = 4;
		add(rmeEndpointField, gbc_rmeEndpointField);
		
		scrollPane = new JScrollPane();
		GridBagConstraints gbc_scrollPane = new GridBagConstraints();
		gbc_scrollPane.fill = GridBagConstraints.BOTH;
		gbc_scrollPane.gridwidth = 2;
		gbc_scrollPane.insets = new Insets(0, 0, 0, 5);
		gbc_scrollPane.gridx = 0;
		gbc_scrollPane.gridy = 5;
		add(scrollPane, gbc_scrollPane);
		
		processViewPanel = new ESBProcessPanel();
		scrollPane.setViewportView(processViewPanel);
		
	}
	
	protected JTextField getProcessNameField() {
		return processNameField;
	}
	protected JTextField getEntryEndpointField() {
		return entryEndpointField;
	}
	protected JList getExitEndpointList() {
		return exitEndpointList;
	}
	protected JTextField getFaultEndpointField() {
		return faultEndpointField;
	}
	protected JTextField getRmeEndpointField() {
		return rmeEndpointField;
	}
	protected ESBProcessPanel getProcessViewPanel() {
		return processViewPanel;
	}
}
