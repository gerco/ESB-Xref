package nl.progaia.esbxref.ui;

import java.awt.BorderLayout;
import java.awt.Toolkit;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;

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
import nl.progaia.esbxref.tasks.UnusedArtifactReportTask;
import nl.progaia.esbxref.ui.DepGraphPanel.DepGraphSelectionListener;
import nl.progaia.esbxref.ui.status.JStatusBar;

import com.sonicsw.deploy.tools.gui.common.DomainConnectionDialog;
import com.sonicsw.deploy.tools.gui.common.DomainConnectionModel;

public class MainFrame extends JFrame {

	private static final long serialVersionUID = 5472232300765455080L;
	
	private DepGraphPanel graphPanel;
	private XRefResultsPanel resultsPanel;
	
	private JStatusBar statusBar;
	private final TaskExecutor worker;
	
	public MainFrame(TaskExecutor worker) {
		super("Sonic ESB Cross Reference");
		setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
		
		initComponents();
		setLocationByPlatform(true);
		
		worker.addListener(new TaskEventListener(this));
		worker.addListener(new StatusBarManipulator());
		this.worker = worker;
	}

	private void initComponents() {
		getContentPane().setLayout(new BorderLayout());
		setJMenuBar(initMenuBar());
		
		JSplitPane splitPane = new JSplitPane();
		splitPane.setLeftComponent(initDepGraphView());
		splitPane.setRightComponent(initResultsPanel());
		getContentPane().add(splitPane, BorderLayout.CENTER);
		
		getContentPane().add(initStatusBar(), BorderLayout.SOUTH);
	}

	private XRefResultsPanel initResultsPanel() {
		resultsPanel = new XRefResultsPanel();
		return resultsPanel;
	}

	private DepGraphPanel initDepGraphView() {
		graphPanel = new DepGraphPanel();
		graphPanel.setSelectionListener(new DepGraphSelectionListener() {
			public void nodeSelected(INode selectedNode) {
				if(resultsPanel != null)
					resultsPanel.setDisplayedNode(selectedNode);
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
		
		JMenuItem analyzeDomainItem = new JMenuItem("Analyze domain...");
		analyzeDomainItem.addActionListener(new ActionListener() {
			public void actionPerformed(ActionEvent e) {
				analyzeDomain();
			}
		});
		
		JMenuItem analyzeXARItem = new JMenuItem("Analyze xar file...");
		analyzeXARItem.addActionListener(new ActionListener() {
			public void actionPerformed(ActionEvent e) {
				analyzeXAR();
			}
		});
		analyzeXARItem.setEnabled(false);
		
		JMenuItem saveAnalysisItem = new JMenuItem("Save analysis");
		saveAnalysisItem.addActionListener(new ActionListener() {
			public void actionPerformed(ActionEvent e) {
				saveAnalysis();
			}
		});
		
		JMenuItem loadAnalysisItem = new JMenuItem("Load analysis");
		loadAnalysisItem.addActionListener(new ActionListener() {
			public void actionPerformed(ActionEvent e) {
				loadAnalysis();
			}
		});
		
		JMenuItem findUnusedItem = new JMenuItem("Find unused artifacts");
		findUnusedItem.addActionListener(new ActionListener() {
			public void actionPerformed(ActionEvent e) {
				findUnused();
			}
		});
		
		JMenuItem quitItem = new JMenuItem("Quit");
		quitItem.addActionListener(new ActionListener() {
			public void actionPerformed(ActionEvent e) {
				quit();
			}
		});
		
		JMenuItem exportToCSVItem = new JMenuItem("Export to CSV");
		exportToCSVItem.setEnabled(false);
		
		JMenuItem exportToHTMLItem = new JMenuItem("Export to HTML");
		exportToHTMLItem.setEnabled(false);
		
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
		// TODO Auto-generated method stub
		
	}
	
	protected void saveAnalysis() {
		JFileChooser chooser = new JFileChooser();
		chooser.showSaveDialog(this);
		
		if(chooser.getSelectedFile() != null) {
			final File file = chooser.getSelectedFile();
			final DependencyGraph graph = graphPanel.getDependencyGraph();
			
			worker.execute(new Task() {
				@Override
				public void execute() throws Exception {
					FileOutputStream fout = new FileOutputStream(file);
					ObjectOutputStream oos = new ObjectOutputStream(fout);
					oos.writeObject(graph);
					fout.flush();
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
		chooser.showOpenDialog(this);
		
		if(chooser.getSelectedFile() == null)
			return;

		final File file = chooser.getSelectedFile();
		
		Task t = new Task() {
			private DependencyGraph graph;
			
			@Override
			public void execute() throws Exception {
				FileInputStream fin = new FileInputStream(file);
				ObjectInputStream oin = new ObjectInputStream(fin);				
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
						}
					});
					break;
					
				}
			}
		});

		worker.execute(t);
	}
	
	protected void findUnused() {
		JFileChooser chooser = new JFileChooser();
		chooser.showSaveDialog(this);
		
		if(chooser.getSelectedFile() != null) {
			final File file = chooser.getSelectedFile();
			final DependencyGraph graph = graphPanel.getDependencyGraph();
			
			worker.execute(new UnusedArtifactReportTask(graph, file));
		}
	}
	
	protected void quit() {
		dispose();
		System.exit(0);
	}

	private class StatusBarManipulator implements EventListener<TaskEvent> {
		private boolean taskError = false;
		
		public void processEvent(final TaskEvent event) {	
			switch(event.getId()) {
			case TASK_STARTED:
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
