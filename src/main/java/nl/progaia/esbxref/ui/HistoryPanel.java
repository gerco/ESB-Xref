package nl.progaia.esbxref.ui;

import java.util.LinkedList;

import javax.swing.Box;
import javax.swing.BoxLayout;
import javax.swing.JButton;
import javax.swing.JPanel;

import nl.progaia.esbxref.dep.INode;

public class HistoryPanel extends JPanel {

	private final LinkedList<INode> history;
	private final JButton backButton;
	private final JButton forwardButton;
//	private final JLabel crumbs;
	
	public HistoryPanel() {
		history = new LinkedList<INode>();
		
		setLayout(new BoxLayout(this, BoxLayout.X_AXIS));
		add(backButton = new JButton("Back"));
		add(Box.createHorizontalStrut(5));
		add(forwardButton = new JButton("Forward"));
//		add(crumbs = new JLabel());
	}
	
	public void addHistoryNode(INode node) {
		if(node == null)
			return;
		
		if(!history.contains(node)) {
			history.add(node);
		}
	}

}
