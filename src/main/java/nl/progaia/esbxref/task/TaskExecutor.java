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

import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

import nl.progaia.esbxref.events.EventListener;
import nl.progaia.esbxref.events.EventSource;


public class TaskExecutor extends EventSource<TaskEvent> implements EventListener<TaskEvent> // implements Executor 
{
	private ExecutorService executor;
	
	public TaskExecutor() {
		this(Executors.newSingleThreadExecutor());
	}
	
	protected TaskExecutor(final ExecutorService executor) {
		super();
		this.executor = executor;
	}
	
	public synchronized void clearQueue() {
		// This will attempt to stop the currently running task and not attempt to 
		// execute any already submitted tasks.
		executor.shutdownNow();

//		List<Runnable> waitingTasks = executor.shutdownNow();
//		System.out.println("Canceled " + waitingTasks.size() + " tasks.");		
		
		// Now create a new executor to schedule new tasks on
		this.executor = Executors.newSingleThreadExecutor();
	}
		
	public synchronized void execute(final Task task) {
		task.setExecutor(this);
		task.addListener(this);
		executor.execute(task);
	}

	public void processEvent(TaskEvent event) {
		// Forward the task event from the task to the application
		dispatchEvent(event);
	}
	
}
