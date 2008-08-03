package nl.progaia.esbxref;

import nl.progaia.esbxref.task.TaskExecutor;
import nl.progaia.esbxref.ui.MainFrame;

public class ESBXref {
	public static void main(String[] args) {
		TaskExecutor worker = new TaskExecutor();
		
		System.setProperty("apple.laf.useScreenMenuBar", "true");
		System.setProperty("com.sonicsw.xq.home", "dummy/value");
		
		MainFrame frame = new MainFrame(worker);
		frame.pack();
		frame.setVisible(true);
	}
}
