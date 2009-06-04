package nl.progaia.esbxref.launcher;

import java.awt.Container;
import java.awt.Dimension;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.util.prefs.Preferences;

import javax.swing.Box;
import javax.swing.BoxLayout;
import javax.swing.JButton;
import javax.swing.JDialog;
import javax.swing.JFileChooser;
import javax.swing.JLabel;
import javax.swing.JPanel;
import javax.swing.JTextArea;
import javax.swing.JTextField;

public class SonicLocationsDialog extends JDialog {

	private static Preferences prefs = Preferences.userNodeForPackage(Launcher.class);
	
	private JTextField mqHomeField; 
	private JButton    mqHomeBrowseButton; 
	
	private JTextField xqHomeField; 
	private JButton    xqHomeBrowseButton; 
	
	private JButton    okButton;
	private JButton    cancelButton;	
	
	private JDialog dialog;
	private boolean userResponse;
	
	public SonicLocationsDialog() {
		dialog = this;
		
		initComponents();
		wireComponents();
		pack();
	}
	
	public boolean askForSonicLocations() {
		mqHomeField.setText(prefs.get(Launcher.PREF_MQ_HOME, ""));
		xqHomeField.setText(prefs.get(Launcher.PREF_XQ_HOME, ""));		
		
		setModal(true);
		setLocationRelativeTo(null);
		setVisible(true);
		
		return userResponse;
	}
	
	private void wireComponents() {
		okButton.addActionListener(new ActionListener() {
			public void actionPerformed(ActionEvent arg0) {
				userResponse = true;
				prefs.put(Launcher.PREF_MQ_HOME, mqHomeField.getText());
				prefs.put(Launcher.PREF_XQ_HOME, xqHomeField.getText());
				setVisible(false);
			}
		});
		cancelButton.addActionListener(new ActionListener() {
			public void actionPerformed(ActionEvent arg0) {
				userResponse = false;
				setVisible(false);
			}
		});
		mqHomeBrowseButton.addActionListener(new ActionListener() {
			public void actionPerformed(ActionEvent arg0) {
				JFileChooser chooser = new JFileChooser();
				chooser.setFileSelectionMode(JFileChooser.DIRECTORIES_ONLY);
				chooser.showDialog(dialog, "Select directory");
				
				if(chooser.getSelectedFile() != null)
					mqHomeField.setText(chooser.getSelectedFile().getAbsolutePath());
			}
		});		
		xqHomeBrowseButton.addActionListener(new ActionListener() {
			public void actionPerformed(ActionEvent arg0) {
				JFileChooser chooser = new JFileChooser();
				chooser.setFileSelectionMode(JFileChooser.DIRECTORIES_ONLY);
				chooser.showDialog(dialog, "Select directory");
				
				if(chooser.getSelectedFile() != null)
					xqHomeField.setText(chooser.getSelectedFile().getAbsolutePath());
			}
		});		
	}
	
	private void initComponents() {
		setLayout(new BoxLayout(getContentPane(), BoxLayout.Y_AXIS));
		
		setPreferredSize(new Dimension(500, 300));
		Container contentPane = getContentPane();
		
		JTextArea label;
		contentPane.add(label = new JTextArea());
		label.setText(
			"Please provide the locations of your Sonic MQ and Sonic ESB installation " +
			"directories in order to start the program. Only Sonic 7.5 and 7.6 are " +
			"supported!");
		label.setLineWrap(true);
		label.setWrapStyleWord(true);
		label.setOpaque(false);
		label.setFont(new JLabel().getFont());
		
		JPanel mqHomePanel = new JPanel();
		mqHomePanel.setLayout(new BoxLayout(mqHomePanel, BoxLayout.X_AXIS));
		mqHomePanel.add(new JLabel("Sonic MQ:"));
		mqHomePanel.add(Box.createHorizontalStrut(5));
		mqHomePanel.add(mqHomeField = new JTextField(15));
		mqHomeField.setMaximumSize(new Dimension(
			mqHomeField.getMaximumSize().width,
			mqHomeField.getPreferredSize().height));
		mqHomePanel.add(Box.createHorizontalStrut(5));
		mqHomePanel.add(mqHomeBrowseButton = new JButton("Browse..."));
		contentPane.add(mqHomePanel);
		
		contentPane.add(Box.createVerticalStrut(10));
		
		JPanel xqHomePanel = new JPanel();
		xqHomePanel.setLayout(new BoxLayout(xqHomePanel, BoxLayout.X_AXIS));
		xqHomePanel.add(new JLabel("Sonic ESB:"));
		xqHomePanel.add(Box.createHorizontalStrut(5));
		xqHomePanel.add(xqHomeField = new JTextField(15));
		xqHomeField.setMaximumSize(new Dimension(
			xqHomeField.getMaximumSize().width,
			xqHomeField.getPreferredSize().height));
		xqHomePanel.add(Box.createHorizontalStrut(5));
		xqHomePanel.add(xqHomeBrowseButton = new JButton("Browse..."));
		contentPane.add(xqHomePanel);
		
		contentPane.add(Box.createVerticalStrut(10));
		
		JPanel actionPanel = new JPanel();
		actionPanel.setLayout(new BoxLayout(actionPanel, BoxLayout.X_AXIS));
		actionPanel.add(Box.createHorizontalGlue());
		actionPanel.add(okButton = new JButton("OK"));
		actionPanel.add(Box.createHorizontalStrut(5));
		actionPanel.add(cancelButton = new JButton("Cancel"));
		contentPane.add(actionPanel);
	}
}
