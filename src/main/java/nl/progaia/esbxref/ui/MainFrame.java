package nl.progaia.esbxref.ui;

import java.awt.BorderLayout;
import java.awt.Dimension;
import java.awt.Toolkit;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.event.WindowAdapter;
import java.awt.event.WindowEvent;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;
import java.util.prefs.Preferences;
import java.util.zip.GZIPInputStream;
import java.util.zip.GZIPOutputStream;

import javax.swing.JFileChooser;
import javax.swing.JFrame;
import javax.swing.JMenu;
import javax.swing.JMenuBar;
import javax.swing.JMenuItem;
import javax.swing.JSplitPane;
import javax.swing.SwingUtilities;
import javax.swing.UIManager;

import nl.progaia.esbxref.dep.DependencyGraph;
import nl.progaia.esbxref.dep.INode;
import nl.progaia.esbxref.events.EventListener;
import nl.progaia.esbxref.task.Task;
import nl.progaia.esbxref.task.TaskEvent;
import nl.progaia.esbxref.task.TaskExecutor;
import nl.progaia.esbxref.tasks.AnalyzeDomainTask;
import nl.progaia.esbxref.tasks.AnalyzeXARTask;
import nl.progaia.esbxref.tasks.UnusedArtifactReportTask;
import nl.progaia.esbxref.ui.status.JStatusBar;

import com.sonicsw.deploy.tools.gui.common.DomainConnectionDialog;
import com.sonicsw.deploy.tools.gui.common.DomainConnectionModel;

public class MainFrame extends JFrame {

	private static final String WINDOW_TITLE = "Sonic ESB Cross Reference";

	private static final long serialVersionUID = 5472232300765455080L;
	
	private Preferences prefs = Preferences.userNodeForPackage(MainFrame.class);
	private static final String PREF_ROOT = MainFrame.class.getName() + ".";
	private static final String PREF_DIVIDER_LOCATION = PREF_ROOT + "dividerLocation"; 
	private static final String PREF_WINDOW_X = PREF_ROOT + "x"; 
	private static final String PREF_WINDOW_Y = PREF_ROOT + "y"; 
	private static final String PREF_WINDOW_WIDTH = PREF_ROOT + "width"; 
	private static final String PREF_WINDOW_HEIGHT = PREF_ROOT + "height";
	private static final String PREF_OPEN_FILE = PREF_ROOT + "openFile";
	private static final String PREF_LAST_DIR = PREF_ROOT + "lastDir";
	
	private JSplitPane splitPane;
	private DepGraphPanel graphPanel;
	private XRefResultsPanel resultsPanel;
	private HistoryPanel historyPanel;

	private File currentFile;
	private String currentFilename;
	private JStatusBar statusBar;
	private final TaskExecutor worker;

	private JMenuItem analyzeDomainItem;
	private JMenuItem analyzeXARItem;
	private JMenuItem saveAnalysisItem;
	private JMenuItem loadAnalysisItem;
	private JMenuItem findUnusedItem;
	private JMenuItem quitItem;
	private JMenuItem exportToCSVItem;
	private JMenuItem exportToHTMLItem;

	public MainFrame(TaskExecutor worker) {
		super(WINDOW_TITLE);
		setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
		addWindowListener(new OpenPreviousFileWindowListener());
		addWindowListener(new SavePrefsWindowListener());
		
		initComponents();
		setCurrentFile(null);
		
		int x = prefs.getInt(PREF_WINDOW_X, -1);
		int y = prefs.getInt(PREF_WINDOW_Y, -1);
		
		if(x == -1 || y == -1) {
			setLocationByPlatform(true);
		} else {
			setLocation(x, y);
			
			int width = prefs.getInt(PREF_WINDOW_WIDTH, -1);
			int height = prefs.getInt(PREF_WINDOW_HEIGHT, -1);
			if(width != -1 && height != -1)
				setPreferredSize(new Dimension(width, height));
		}
		
		worker.addListener(new TaskEventListener(this));
		worker.addListener(new StatusBarManipulator());
		this.worker = worker;
	}

	private void initComponents() {
		getContentPane().setLayout(new BorderLayout());
		setJMenuBar(initMenuBar());
		
//		getContentPane().add(initHistoryPanel(), BorderLayout.NORTH);
		
		splitPane = new JSplitPane();
		splitPane.setLeftComponent(initDepGraphView());
		splitPane.setRightComponent(initResultsPanel());
		splitPane.setDividerLocation(
				prefs.getInt(PREF_DIVIDER_LOCATION, -1));
		getContentPane().add(splitPane, BorderLayout.CENTER);
		
		getContentPane().add(initStatusBar(), BorderLayout.SOUTH);
	}

	private HistoryPanel initHistoryPanel() {
		historyPanel = new HistoryPanel();
		return historyPanel;
	}

	private XRefResultsPanel initResultsPanel() {
		resultsPanel = new XRefResultsPanel();
		resultsPanel.setSelectionListener(new NodeSelectionListener() {
			public void nodeSelected(INode node) {
				if(graphPanel != null) {
					graphPanel.selectNode(node);
				}
			}
		});
		return resultsPanel;
	}

	private DepGraphPanel initDepGraphView() {
		graphPanel = new DepGraphPanel();
		graphPanel.setSelectionListener(new NodeSelectionListener() {
			public void nodeSelected(INode selectedNode) {
				if(resultsPanel != null)
					resultsPanel.setDisplayedNode(selectedNode);
				
				if(historyPanel != null)
					historyPanel.addHistoryNode(selectedNode);
			}
		});
		return graphPanel;
	}

	private JStatusBar initStatusBar() {
		statusBar = new JStatusBar();
		return statusBar;
	}

	private JMenuBar initMenuBar() {
		JMenuBar menuBar = new JMenuBar();
		
		JMenu fileMenu = new JMenu("File");
		JMenu analyzeMenu = new JMenu("Analyze");
		
		analyzeDomainItem = new JMenuItem("Analyze domain...");
		analyzeDomainItem.addActionListener(new ActionListener() {
			public void actionPerformed(ActionEvent e) {
				analyzeDomain();
			}
		});
		
		analyzeXARItem = new JMenuItem("Analyze xar file...");
		analyzeXARItem.addActionListener(new ActionListener() {
			public void actionPerformed(ActionEvent e) {
				analyzeXAR();
			}
		});
		
		saveAnalysisItem = new JMenuItem("Save analysis");
		saveAnalysisItem.addActionListener(new ActionListener() {
			public void actionPerformed(ActionEvent e) {
				saveAnalysis();
			}
		});
		
		loadAnalysisItem = new JMenuItem("Load analysis");
		loadAnalysisItem.addActionListener(new ActionListener() {
			public void actionPerformed(ActionEvent e) {
				loadAnalysis();
			}
		});
		
		findUnusedItem = new JMenuItem("Find unused artifacts");
		findUnusedItem.addActionListener(new ActionListener() {
			public void actionPerformed(ActionEvent e) {
				findUnused();
			}
		});
		
		quitItem = new JMenuItem("Quit");
		quitItem.addActionListener(new ActionListener() {
			public void actionPerformed(ActionEvent e) {
				quit();
			}
		});
		
		exportToCSVItem = new JMenuItem("Export to CSV");
		exportToCSVItem.addActionListener(new ActionListener() {
			public void actionPerformed(ActionEvent e) {
				exportToCSV();
			}
		});
		
		exportToHTMLItem = new JMenuItem("Export to HTML");
		exportToHTMLItem.addActionListener(new ActionListener() {
			public void actionPerformed(ActionEvent e) {
				exportToHTML();
			}
		});
		
		// Assemble the file menu
		fileMenu.add(saveAnalysisItem);
		fileMenu.add(loadAnalysisItem);
		fileMenu.insertSeparator(2);
		fileMenu.add(exportToCSVItem);
		fileMenu.add(exportToHTMLItem);
		fileMenu.insertSeparator(5);
		fileMenu.add(quitItem);
		
		// Assemble the Analyze menu
		analyzeMenu.add(analyzeDomainItem);		
		analyzeMenu.add(analyzeXARItem);
		analyzeMenu.insertSeparator(2);
		analyzeMenu.add(findUnusedItem);
		
		menuBar.add(fileMenu);
		menuBar.add(analyzeMenu);
		
		return menuBar;
	}
	
	protected void analyzeDomain() {
		DomainConnectionModel model = getConnectionModel();

		if(model == null)
			return;
		
		Task t = new AnalyzeDomainTask(model);
		t.addListener(new EventListener<TaskEvent>() {
			public void processEvent(final TaskEvent event) {
				switch(event.getId()) {
				
				case TASK_ERROR:
					((Task)event.getSource()).removeListener(this);
					break;
					
				case TASK_FINISHED:
					SwingUtilities.invokeLater(new Runnable() {
						public void run() {
							graphPanel.setDependencyGraph((DependencyGraph) event.getInfo());
							setCurrentFile(new File("Untitled.xref"));
						}
					});
					break;
					
				}
			}
		});
		worker.execute(t);
	}
	
	private DomainConnectionModel getConnectionModel() {
        DomainConnectionDialog dialog;
        dialog = new DomainConnectionDialog(this);
        
//        DomainConnectionModel model = new DomainConnectionModel();
        dialog.setLocationRelativeTo(this);
        dialog.pack();
        dialog.setVisible(true);
        if(!dialog.isOK())
            return null;
        
        return dialog.getModel();
	}
	
	protected void analyzeXAR() {
		JFileChooser chooser = new JFileChooser();
		chooser.setAcceptAllFileFilterUsed(true);
		chooser.addChoosableFileFilter(new ExtensionFileFilter(".xar", "ESB Archive files"));
		chooser.showDialog(this, "Analyze XAR");
		
		if(chooser.getSelectedFile() == null)
			return;

		final File file = chooser.getSelectedFile();
		if(file == null)
			return;
		
		Task t = new AnalyzeXARTask(file);
		t.addListener(new EventListener<TaskEvent>() {
			public void processEvent(final TaskEvent event) {
				switch(event.getId()) {
				
				case TASK_ERROR:
					((Task)event.getSource()).removeListener(this);
					break;
					
				case TASK_FINISHED:
					SwingUtilities.invokeLater(new Runnable() {
						public void run() {
							graphPanel.setDependencyGraph((DependencyGraph) event.getInfo());
							setCurrentFile(new File("Untitled.xref"));
						}
					});
					break;
					
				}
			}
		});
		worker.execute(t);
	}
	
	protected void saveAnalysis() {
		JFileChooser chooser = new JFileChooser();
		chooser.setCurrentDirectory(getLastDir());
		chooser.setAcceptAllFileFilterUsed(true);
		chooser.addChoosableFileFilter(new ExtensionFileFilter(".xref", "Cross reference files"));
		chooser.showDialog(this, "Save analysis");
		
		if(chooser.getSelectedFile() != null) {
			setLastDir(chooser.getCurrentDirectory());
			File selectedFile = chooser.getSelectedFile();
			
			if(!selectedFile.getName().toLowerCase().endsWith(".xref"))
				selectedFile = new File(selectedFile.getAbsolutePath() + ".xref");
			
			final File file = selectedFile;
			final DependencyGraph graph = graphPanel.getDependencyGraph();
			
			setCurrentFile(file);
			worker.execute(new Task() {
				@Override
				public void execute() throws Exception {
					FileOutputStream fout = new FileOutputStream(file);
					GZIPOutputStream zout = new GZIPOutputStream(fout);
					ObjectOutputStream oos = new ObjectOutputStream(zout);
					oos.writeObject(graph);
					oos.flush();
					zout.flush();
					zout.finish();
					oos.close();
					zout.close();
					fout.close();
				}
				
				@Override
				public String toString() {
					return "Saving " + file.getAbsolutePath();
				}
			});
		}
	}
	
	protected void loadAnalysis() {
		JFileChooser chooser = new JFileChooser();
		chooser.setCurrentDirectory(getLastDir());
		chooser.setAcceptAllFileFilterUsed(true);
		chooser.addChoosableFileFilter(new ExtensionFileFilter(".xref", "Cross reference files"));
		chooser.showDialog(this, "Load analysis");
		
		if(chooser.getSelectedFile() == null)
			return;

		setLastDir(chooser.getCurrentDirectory());
		loadAnalysis(chooser.getSelectedFile());
	}
	
	protected void loadAnalysis(final File file) {
		Task t = new Task() {
			private DependencyGraph graph;
			
			@Override
			public void execute() throws Exception {
				FileInputStream fin = new FileInputStream(file);
				GZIPInputStream zin = new GZIPInputStream(fin);
				ObjectInputStream oin = new ObjectInputStream(zin);				
				graph = (DependencyGraph) oin.readObject();
				fin.close();
			}
			
			@Override
			protected Object getInfo() {
				return graph;
			}
			
			@Override
			public String toString() {
				return "Loading " + file.getAbsolutePath();
			}
		};
		
		t.addListener(new EventListener<TaskEvent>() {
			public void processEvent(final TaskEvent event) {
				switch(event.getId()) {
				
				case TASK_ERROR:
					((Task)event.getSource()).removeListener(this);
					break;
					
				case TASK_FINISHED:
					SwingUtilities.invokeLater(new Runnable() {
						public void run() {
							graphPanel.setDependencyGraph((DependencyGraph) event.getInfo());
							setCurrentFile(file);
						}
					});
					break;
					
				}
			}
		});

		worker.execute(t);
	}
	
	protected void exportToCSV() {
		JFileChooser chooser = new JFileChooser();
		chooser.setCurrentDirectory(getLastDir());
		chooser.setAcceptAllFileFilterUsed(true);
		chooser.addChoosableFileFilter(new ExtensionFileFilter(".csv", "CSV files"));
		chooser.showDialog(this, "Export to CSV");
		
		if(chooser.getSelectedFile() != null) {
			setLastDir(chooser.getCurrentDirectory());
			final File file = chooser.getSelectedFile();
			final DependencyGraph graph = graphPanel.getDependencyGraph();
			
			worker.execute(new Task() {
				@Override
				public void execute() throws Exception {
					graph.dumpToCSVFile(file);
				}
				
				@Override
				public String toString() {
					return "Exporting to " + file.getAbsolutePath();
				}
			});
		}
	}
	
	protected void exportToHTML() {
		JFileChooser chooser = new JFileChooser();
		chooser.setCurrentDirectory(getLastDir());
		chooser.setAcceptAllFileFilterUsed(true);
		chooser.addChoosableFileFilter(new ExtensionFileFilter(".html", "HTML files"));
		chooser.showDialog(this, "Export to HTML");
		
		if(chooser.getSelectedFile() != null) {
			setLastDir(chooser.getCurrentDirectory());
			final File file = chooser.getSelectedFile();
			final DependencyGraph graph = graphPanel.getDependencyGraph();
			
			worker.execute(new Task() {
				@Override
				public void execute() throws Exception {
					graph.dumpToHTMLFile(file);
				}
				
				@Override
				public String toString() {
					return "Exporting to " + file.getAbsolutePath();
				}
			});
		}
	}
	
	protected void findUnused() {
		JFileChooser chooser = new JFileChooser();
		chooser.setAcceptAllFileFilterUsed(true);
		chooser.showDialog(this, "Save unused artifact report");
		
		if(chooser.getSelectedFile() != null) {
			final File file = chooser.getSelectedFile();
			final DependencyGraph graph = graphPanel.getDependencyGraph();
			
			worker.execute(new UnusedArtifactReportTask(graph, file));
		}
	}
	
	protected void quit() {
		setVisible(false);
		dispose();
		System.exit(0);
	}
	
	public void setLastDir(File dir) {
		prefs.put(PREF_LAST_DIR, dir.getAbsolutePath());
	}
	
	public File getLastDir() {
		String dirname = prefs.get(PREF_LAST_DIR, null);
		
		if(dirname == null || dirname.length()==0)
			return null;
		
		File dir = new File(dirname);
		if(dir.exists() && dir.isDirectory())
			return dir;
		
		return null;
	}
	
	public String getCurrentFilename() {
		return currentFilename;
	}

	public File getCurrentFile() {
		return this.currentFile;
	}
	
	public void setCurrentFile(File file) {
		this.currentFile = file;
		this.currentFilename = file != null ? file.getName() : null;
		
		if(getCurrentFilename() == null) {
			setTitle(WINDOW_TITLE);
		} else {
			setTitle(WINDOW_TITLE + " - " + getCurrentFilename());
		}
		
		boolean haveFile = getCurrentFilename() != null;
		saveAnalysisItem.setEnabled(haveFile);
		exportToCSVItem.setEnabled(haveFile);
		exportToHTMLItem.setEnabled(haveFile);
		findUnusedItem.setEnabled(haveFile);
	}

	/**
	 * Whenever a task is executing, set the status bar to busy and display the
	 * task name.
	 * 
	 * @author Gerco Dries (gdr@progaia-rs.nl)
	 *
	 */
	private class StatusBarManipulator implements EventListener<TaskEvent> {
		private boolean taskError = false;
		
		public void processEvent(final TaskEvent event) {	
			switch(event.getId()) {
			case TASK_STARTED:
			case TASK_PROGRESS_INFO:
				taskError = false;
				SwingUtilities.invokeLater(new Runnable() {
					public void run() {
						statusBar.setText(event.getSource().toString());
						statusBar.setBusy(true);
					}
				});
				break;
				
			case TASK_ERROR:
				taskError = true;
				final Exception ex = (Exception)event.getInfo();
				
				SwingUtilities.invokeLater(new Runnable() {
					public void run() {
						statusBar.setErrorText(ex.getMessage());
					}
				});
				break;
				
			case TASK_FINISHED:
				final boolean error = taskError;
				SwingUtilities.invokeLater(new Runnable() {
					public void run() {
						if(!error)
							statusBar.setText("");
						statusBar.setBusy(false);
					}
				});
				taskError = false;
				break;
			}
		}	
	}

	/**
	 * Load the last open file if there is one.
	 * @author gerco
	 *
	 */
	private class OpenPreviousFileWindowListener extends WindowAdapter {

		/* (non-Javadoc)
		 * @see java.awt.event.WindowAdapter#windowOpened(java.awt.event.WindowEvent)
		 */
		@Override
		public void windowOpened(WindowEvent e) {
			String fileName = prefs.get(PREF_OPEN_FILE, "");
			if(fileName != null && fileName.length() > 0) {
				File file = new File(fileName);
				if(file.exists() && file.canRead())
					loadAnalysis(file);
			}
		}
		
	}
	
	/**
	 * This class saves the preferences when the window is about to close.
	 * 
	 * @author Gerco Dries (gdr@progaia-rs.nl)
	 *
	 */
	private class SavePrefsWindowListener extends WindowAdapter {
		@Override
		public void windowClosing(WindowEvent e) {
			prefs.putInt(PREF_DIVIDER_LOCATION, splitPane.getDividerLocation());
			prefs.putInt(PREF_WINDOW_X, getLocation().x);
			prefs.putInt(PREF_WINDOW_Y, getLocation().y);
			prefs.putInt(PREF_WINDOW_WIDTH, getSize().width);
			prefs.putInt(PREF_WINDOW_HEIGHT, getSize().height);
			
			if(currentFile != null && currentFile.exists())
				prefs.put(PREF_OPEN_FILE, currentFile.getAbsolutePath());
			else
				prefs.remove(PREF_OPEN_FILE);
		}
	}
	
	static {
		try {
			UIManager.setLookAndFeel(UIManager.getSystemLookAndFeelClassName());
			if(Boolean.TRUE.equals(Toolkit.getDefaultToolkit().getDesktopProperty("awt.dynamicLayoutSupported")))
				Toolkit.getDefaultToolkit().setDynamicLayout(true);
		} catch (Exception e) {
			e.printStackTrace();
		}
	}
}
