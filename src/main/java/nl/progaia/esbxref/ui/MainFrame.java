package nl.progaia.esbxref.ui;

import java.awt.BorderLayout;
import java.awt.Toolkit;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;

import javax.swing.JFrame;
import javax.swing.JMenu;
import javax.swing.JMenuBar;
import javax.swing.JMenuItem;
import javax.swing.SwingUtilities;
import javax.swing.UIManager;

import nl.progaia.esbxref.dep.DependencyGraph;
import nl.progaia.esbxref.events.EventListener;
import nl.progaia.esbxref.task.Task;
import nl.progaia.esbxref.task.TaskEvent;
import nl.progaia.esbxref.task.TaskExecutor;
import nl.progaia.esbxref.tasks.AnalyzeDomainTask;
import nl.progaia.esbxref.ui.status.JStatusBar;

public class MainFrame extends JFrame {

	private static final long serialVersionUID = 5472232300765455080L;
	
	private DepGraphPanel graphPanel;
	private JStatusBar statusBar;
	private final TaskExecutor worker;
	
	public MainFrame(TaskExecutor worker) {
		super("Sonic ESB Cross Reference");
		setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
		
		initComponents();
		
		worker.addListener(new TaskEventListener(this));
		worker.addListener(new StatusBarManipulator());
		this.worker = worker;
	}

	private void initComponents() {
		getContentPane().setLayout(new BorderLayout());
		initMenuBar();
		initDepGraphView();
		initStatusBar();
	}

	private void initDepGraphView() {
		graphPanel = new DepGraphPanel();
		getContentPane().add(graphPanel, BorderLayout.CENTER);
	}

	private void initStatusBar() {
		statusBar = new JStatusBar(); 
		getContentPane().add(statusBar, BorderLayout.SOUTH);
	}

	private void initMenuBar() {
		JMenuBar menuBar = new JMenuBar();
		
		JMenu fileMenu = new JMenu("File");
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
		
		JMenuItem quitItem = new JMenuItem("Quit");
		quitItem.addActionListener(new ActionListener() {
			public void actionPerformed(ActionEvent e) {
				quit();
			}
		});
		
		// Assemble the menu
		fileMenu.add(analyzeDomainItem);
		fileMenu.add(analyzeXARItem);
		fileMenu.insertSeparator(2);
		fileMenu.add(saveAnalysisItem);
		fileMenu.add(loadAnalysisItem);
		fileMenu.insertSeparator(5);
		fileMenu.add(quitItem);
		
		menuBar.add(fileMenu);
		
		setJMenuBar(menuBar);
	}
	
	public void analyzeDomain() {
		Task t = new AnalyzeDomainTask("Domain1", "localhost:2506", "Administrator", "Administrator");
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
	
	protected void analyzeXAR() {
		// TODO Auto-generated method stub
		
	}
	
	protected void saveAnalysis() {
		// TODO Auto-generated method stub
		
	}
	
	protected void loadAnalysis() {
		// TODO Auto-generated method stub
		
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
