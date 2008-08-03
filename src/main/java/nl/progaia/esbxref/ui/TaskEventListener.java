/**
 * Sonic Message Manager is a JMS Test Client replacement with 
 * ease of use as the primary design goal.
 * 
 * Copyright 2008, Gerco Dries (gdr@progaia-rs.nl).
 * 
 * Sonic Message Manager is free software: you can redistribute it 
 * and/or modify it under the terms of the GNU General Public License 
 * as published by the Free Software Foundation, either version 2 of 
 * the License, or (at your option) any later version.
 *
 * Sonic Message Manager is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU General Public License for more details.
 *
 * You should have received a copy of the GNU General Public License
 * along with Sonic Message Manager.  If not, see 
 * <http://www.gnu.org/licenses/>.
 */
package nl.progaia.esbxref.ui;

import java.awt.Component;
import java.io.PrintWriter;
import java.io.StringWriter;
import java.lang.reflect.InvocationTargetException;

import javax.swing.JOptionPane;
import javax.swing.SwingUtilities;

import nl.progaia.esbxref.events.EventListener;
import nl.progaia.esbxref.task.Task;
import nl.progaia.esbxref.task.TaskEvent;
import nl.progaia.esbxref.ui.progress.ProgressMonitor;
import nl.progaia.esbxref.ui.progress.ProgressUtil;


class TaskEventListener implements EventListener<TaskEvent> {
	private static final String MANAGE_PERMISSION_DENIED = 
		"com.sonicsw.mf.common.security.ManagePermissionDeniedException";

	private static final String CONFIGURE_PERMISSION_DENIED = 
		"com.sonicsw.mf.common.security.ConfigurePermissionDeniedException";
	
	private static final boolean DEBUG = "TRUE".equalsIgnoreCase(System.getProperty("developer"));
	
	// Only a single task is allowed to be active at any time!!!
	private ProgressMonitor monitor;
	private final Component parent;
	
	
	public TaskEventListener(Component parent) {
		this.parent = parent;
	}
	
	public void processEvent(TaskEvent event) {
		if(DEBUG) System.out.println(event);
		
		switch(event.getId()) {
		case TASK_STARTED:
			if(monitor != null) {
				if(DEBUG) showMessage(parent, "Error!", "Attempt to execute a task before the previous one finished!", true);
				if(monitor != null) {
					monitor.setCurrent(null, monitor.getTotal());
					monitor = null;
				}
			}
			
            try {
            	Task task = (Task)event.getSource();
            	if(!task.isBackground()) {
	            	monitor = ProgressUtil.createModalProgressMonitor(parent, 
	            			task.getProgressMaximum(), 
	            			task.getProgressMaximum() == 1, 
	            			1750);
	            	monitor.start(task.toString());
            	}
			} catch (InterruptedException e) {
			} catch (InvocationTargetException e) {
			} 
			break;
			
		case TASK_PROGRESS:
			if(monitor != null) {
				Task task = (Task)event.getSource();
				monitor.setCurrent(task.getStatus(), (Integer)event.getInfo());
			}
			break;
			
		case TASK_FINISHED:
			if(monitor != null) {
				monitor.setCurrent(null, monitor.getTotal());
				monitor = null;
			}
			break;
			
		case TASK_ERROR:
			if(monitor != null) {
				monitor.setCurrent(null, monitor.getTotal());
				monitor = null;
			}
			
			if(!((Task)event.getSource()).isBackground()) {
				String message = translateExceptionMessage((Exception)event.getInfo());
				showMessage(parent, event.getSource().toString(), message, true);
			}
			
			break;
		}
	}
	
	private String translateExceptionMessage(Throwable e) {
		if(e == null)
			return "Unknown error! (Exception in TaskEvent was null)";
		
		if(DEBUG) return captureStackTrace(e);
		
		// Handle known Exceptions first. If the Exception is unknown, dig deeper
		if(e instanceof java.net.UnknownHostException)
			return "The host was not found";
		
		if(MANAGE_PERMISSION_DENIED.equals(e.getClass().getName())
		|| CONFIGURE_PERMISSION_DENIED.equals(e.getClass().getName()))
			return "You do not have permission to perform this action";
				
		// The Exception was not a known one. Dig deeper.
		if(e.getCause() != null) 
			return //"[translateExceptionMessage(" + e.getClass().getName() + ")] " + 
				translateExceptionMessage(e.getCause());

		// Reached the end of the exception stack
		return e.getMessage();
	}

	private String captureStackTrace(Throwable e) {
		// Capture the stacktrace for in the error message
		StringWriter sw = new StringWriter();
		PrintWriter pw = new PrintWriter(sw);
		e.printStackTrace(pw);
		
		return sw.toString();
	}

	private void showMessage(final Component parent, final String title, final String message, final boolean error) {
		SwingUtilities.invokeLater(new Runnable() {
			public void run() {
				JOptionPane.showMessageDialog(parent, 
						message, title, 
						error ? JOptionPane.ERROR_MESSAGE : JOptionPane.INFORMATION_MESSAGE);
			}
		});
	}		
}