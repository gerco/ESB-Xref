package nl.progaia.esbxref.ui.progress;

import java.awt.Component;
import java.awt.Dialog;
import java.awt.Frame;
import java.awt.Window;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.lang.reflect.InvocationTargetException;

import javax.swing.SwingUtilities;
import javax.swing.Timer;
import javax.swing.event.ChangeEvent;
import javax.swing.event.ChangeListener;

/** 
 * MySwing: Advanced Swing Utilites 
 * Copyright (C) 2005  Santhosh Kumar T 
 * <p/> 
 * This library is free software; you can redistribute it and/or 
 * modify it under the terms of the GNU Lesser General Public 
 * License as published by the Free Software Foundation; either 
 * version 2.1 of the License, or (at your option) any later version. 
 * <p/> 
 * This library is distributed in the hope that it will be useful, 
 * but WITHOUT ANY WARRANTY; without even the implied warranty of 
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the GNU 
 * Lesser General Public License for more details. 
 */ 
public class ProgressUtil{ 
    static class MonitorListener implements ChangeListener, ActionListener{ 
        ProgressMonitor monitor; 
        Window owner; 
        Timer timer; 
 
        public MonitorListener(Window owner, ProgressMonitor monitor){ 
            this.owner = owner; 
            this.monitor = monitor; 
        } 
 
        public void stateChanged(ChangeEvent ce){ 
            ProgressMonitor monitor = (ProgressMonitor)ce.getSource(); 
            if(monitor.getCurrent()!=monitor.getTotal()){ 
                if(timer==null){ 
                    timer = new Timer(monitor.getMilliSecondsToWait(), this); 
                    timer.setRepeats(false); 
                    timer.start(); 
                } 
            }else{ 
                if(timer!=null && timer.isRunning()) 
                    timer.stop(); 
                monitor.removeChangeListener(this); 
            } 
        } 

        /**
         * This runs on the event dispatch thread because it's invoked from
         * a javax.swing.Timer!
         */
        public void actionPerformed(ActionEvent e){ 
        	/**
        	 * Perform a last-second check to see if the monitor hasn't already 
        	 * finished it's task. Keep the monitor locked while the dialog is
        	 * being set up to prevent a race condition from locking the dialog in
        	 * place in the middle of the screen.
        	 */ 
        	synchronized(monitor) {
	        	if(monitor.getCurrent() != monitor.getTotal()) {
		            monitor.removeChangeListener(this);
		            
		            /** 
		             * Kick off the progress dialog and wait with releasing the 
		             * monitor until this is done and has registered it's 
		             * changeListener. That will ensure that future calls 
		             * to setCurrent() will update the dialog (and possibly 
		             * close it).
		             */
			        final ProgressDialog dlg = owner instanceof Frame 
			            ? new ProgressDialog((Frame)owner, monitor) 
			            : new ProgressDialog((Dialog)owner, monitor); 
			        dlg.pack(); 
			        dlg.setLocationRelativeTo(owner);
			        
			        /**
			         * Now make the dialog visible asynchronously. It has already
			         * registered as a changeListener so should hide itself as soon
			         * as a change event comes along. Another thread will not be 
			         * able to trigger stateChanged before the dialog is visible
			         * because we are still holding the monitor lock while inserting
			         * this event into the event queue.
			         */
			        SwingUtilities.invokeLater(new Runnable() {
						public void run() {
							dlg.setVisible(true); 
						}
			        });
	        	}
        	}
        } 
    } 
 
    public static ProgressMonitor createModalProgressMonitor(final Component owner, final int total, final boolean indeterminate, final int milliSecondsToWait) throws InterruptedException, InvocationTargetException {
    	final ResultHolder<ProgressMonitor> result = new ResultHolder<ProgressMonitor>();
    	
		SwingUtilities.invokeAndWait(new Runnable() {
			public void run() {
				result.setResult(ProgressUtil.createModalProgressMonitorInternal(owner, total, indeterminate, milliSecondsToWait));
			}
		});
    	
    	return result.getResult();
    } 
    
    private static ProgressMonitor createModalProgressMonitorInternal(Component owner, int total, boolean indeterminate, int milliSecondsToWait) {
        ProgressMonitor monitor = new ProgressMonitor(total, indeterminate, milliSecondsToWait); 
        Window window = owner instanceof Window 
                ? (Window)owner 
                : SwingUtilities.getWindowAncestor(owner); 
        monitor.addChangeListener(new MonitorListener(window, monitor)); 
        return monitor; 
    }
    
	private static class ResultHolder<T> {
		private T result;
		
		public void setResult(T result) {
			this.result = result;
		}
		
		public T getResult() {
			return result;
		}
	}
}
