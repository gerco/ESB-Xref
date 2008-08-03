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
package nl.progaia.esbxref.task;

import nl.progaia.esbxref.events.EventSource;
import nl.progaia.esbxref.task.TaskEvent.EVENT;

/**
 * This class is the base class for all tasks to be executed on the TaskExecutor. 
 * When the execute() method throws an exception and the Task is used as a Runnable, 
 * the run() method will catch it and dispatch TASK_ERROR and clear the task queue.
 * 
 * @author gerco
 *
 */
public abstract class Task extends EventSource<TaskEvent> implements Runnable {

	private TaskExecutor executor;
	protected long startTime;
	
	TaskExecutor getExecutor() {
		return executor;
	}

	void setExecutor(TaskExecutor executor) {
		this.executor = executor;
	}

	public final void run() {
		startTime = System.currentTimeMillis();
		dispatchTaskStarted();
		
		try {
			execute();
		} catch (Exception e) {
			// Tell the executor to clear the task queue
			executor.clearQueue();
			
			// Tell the application that there was an error
			dispatchTaskError(e);
		}
		
		dispatchTaskFinished();
	}

	protected void dispatchTaskStarted() {
		dispatchEvent(new TaskEvent(EVENT.TASK_STARTED, getInfo(), this));
	}

	protected void dispatchTaskError(Exception e) {
		dispatchEvent(new TaskEvent(EVENT.TASK_ERROR, e, this));
	}
	
	protected void dispatchTaskFinished() {
		dispatchEvent(new TaskEvent(EVENT.TASK_FINISHED, getInfo(), this));
	}

	/**
	 * This method should perform the actual work. Any exceptions thrown will be converted
	 * into a TASK_ERROR event on the EventListener<TaskEvent>.
	 * 
	 * @throws Exception
	 */
	public abstract void execute() throws Exception;
	
	/**
	 * This method should return status information for the running task. It may be called
	 * one or more times when a task dispatches TASK_PROGRESS. It will not be called when
	 * TASK_PROGRESS is never dispatched.
	 * 
	 * @return The status of the running task
	 */
	public String getStatus() {
//		return "Running for " + (System.currentTimeMillis() - startTime) + " milliseconds";
		return this.toString();
	}
	
	/**
	 * Return an Object describing the current status of the Task. This object is included in
	 * events published from the task (except TASK_PROGRESS and TASK_ERROR). The default 
	 * implementation returns null
	 * 
	 * @return
	 */
	protected Object getInfo() {
		return null;
	}
	
	/**
	 * The maximum progress value reported through TASK_PROGRESS events. When this value is
	 * reached, the task is considered complete. By default, this method returns 1.
	 * 
	 * @return The highest value to ever be reported through TASK_PROGRESS.
	 */
	public int getProgressMaximum() {
		return 1;
	}
	
	/**
	 * Raise the TASK_PROGRESS event with this task as the source and 'current' as the value
	 * 
	 * @param current The amount of progress that has been made (in total)
	 */
	protected void reportProgress(int current) {
		dispatchEvent(new TaskEvent(EVENT.TASK_PROGRESS, current, this));
	}
	
	/**
	 * When this Task is a true background task, the progress dialog will not
	 * pop up and allow the user to continue working while this task executes.
	 */
	public boolean isBackground() {
		return false;
	}
}
