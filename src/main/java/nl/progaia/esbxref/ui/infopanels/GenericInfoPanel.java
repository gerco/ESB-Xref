package nl.progaia.esbxref.ui.infopanels;

import java.awt.BorderLayout;

import javax.swing.JPanel;
import javax.swing.JScrollPane;
import javax.swing.JTextArea;

import nl.progaia.esbxref.dep.ArtifactNode;

public class GenericInfoPanel extends JPanel {

	private JTextArea textArea;
	
	public GenericInfoPanel() {
		setLayout(new BorderLayout());
		textArea = new JTextArea();
		add(new JScrollPane(textArea, JScrollPane.VERTICAL_SCROLLBAR_AS_NEEDED, JScrollPane.HORIZONTAL_SCROLLBAR_AS_NEEDED), 
			BorderLayout.CENTER);
	}
	
	public void setNode(ArtifactNode node) {
		String xml = node.getArtifactXml();
		if(xml != null)
			textArea.setText(xml);
		else
			textArea.setText("");
	}
	
}
