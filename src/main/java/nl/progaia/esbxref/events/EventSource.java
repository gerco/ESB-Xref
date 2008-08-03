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
package nl.progaia.esbxref.events;

import java.util.ArrayList;

public abstract class EventSource<T> {
	private final ArrayList<EventListener<T>> listeners = 
		new ArrayList<EventListener<T>>();
	
	public void addListener(EventListener<T> listener) {
		if(!listeners.contains(listener)) {
			listeners.add(listener);
		}
	}
	
	public void removeListener(EventListener<T> listener) {
		if(listeners.contains(listener)) {
			listeners.remove(listener);
		}
	}
	
	protected void dispatchEvent(T event) {
		for(EventListener<T> listener: listeners) {
			listener.processEvent(event);
		}
	}
}
