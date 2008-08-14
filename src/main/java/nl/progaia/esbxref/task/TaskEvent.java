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

import java.util.EventObject;

import nl.progaia.esbxref.events.EventSource;


public class TaskEvent extends EventObject {

	/**
	 * <tt>TASK_STARTED</tt> is dispatched when a task starts executing. The source
	 * is the Task itself, the info object is null by default.
	 * <p>
	 * <tt>TASK_PROGRESS</tt> is dispatched when a task has progress to report. The info 
	 * object is an Integer object, the source is the Task itself. Some Tasks do not 
	 * dispatch this event, some do. It is optional.
	 * <p>
	 * <tt>TASK_PROGRESS_INFO</tt> is dispatched by a task when it wants to change it's
	 * progress attributes. When a task does not know the maximum amount of progress it
	 * will start as '1' (or indeterminate). When a task later knows more about how long
	 * the operation will taks, it dispatches this event. The info object is a Integer
	 * with the maximum value of the progress indicator.
	 * <p> 
	 * <tt>TASK_FINISHED</tt> is dispatched when a task has stopped executing. This event
	 * always occurs, whether there has been an error or not. The Task itself is the source
	 * and the info object is null by default.
	 * <p>
	 * <tt>TASK_ERROR</tt> reports an error in the Task. The info object is the Exception
	 * that occurred, the Task is the source.
	 * 
	 * @author Gerco Dries (gdr@progaia-rs.nl)
	 *
	 */
	public static enum EVENT {
		TASK_STARTED,
		TASK_PROGRESS,
		TASK_PROGRESS_INFO,
		TASK_FINISHED,
		TASK_ERROR
	}
	
	private final EVENT id;
	private final Object info;
	
	public TaskEvent(EVENT id, Object info, EventSource<TaskEvent> source) {
		super(source);
		this.id = id;
		this.info = info;
	}

	public Object getInfo() {
		return info;
	}

	public EVENT getId() {
		return id;
	}

	@Override
	@SuppressWarnings("unchecked")
	public EventSource<TaskEvent> getSource() {
		return (EventSource<TaskEvent>)super.getSource();
	}

	@Override
	public String toString() {
		return getSource() + ": " + getId() + " (" + getInfo() + ")";
	}
}
