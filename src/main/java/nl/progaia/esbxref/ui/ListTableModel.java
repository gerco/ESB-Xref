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

import java.util.List;

import javax.swing.table.AbstractTableModel;

public abstract class ListTableModel<T> extends AbstractTableModel {
	protected List<T> data;
	protected String[] columnNames;
	protected Class<?>[] columnTypes;
	
	public List<T> getData() {
		return data;
	}
	
	public void setData(List<T> data) {
		this.data = data;
		fireTableDataChanged();
	}
	
	public void addRow(T item) {
		if(data == null)
			throw new IllegalStateException("Cannot add rows when data == null");
		
		data.add(item);
		fireTableRowsInserted(data.size(), data.size());
	}
	
	public void setColumnNames(String[] names) {
		columnNames = names;
	}
	
	public void setColumnTypes(Class<?>[] types) {
		columnTypes = types;
	}
	
	public int getColumnCount() {
		return columnNames.length;
	}

	public int getRowCount() {
		return (data == null) ? 0 : data.size();
	}

	public Class<?> getColumnClass(int col) {
		return columnTypes[col];
	}

	public String getColumnName(int col) {
		return columnNames[col];
	}
	
	public T getRowItem(int row) {
		return 
			(data == null) ? null :
				(row < 0 || row > data.size()) ? null :
					data.get(row);
	}
	
	public void setRowItem(int row, T item) {
		data.set(row, item);
		fireTableRowsUpdated(row, row);
	}
	
	
	public int getItemRow(T item) {
		if(data.contains(item)) {
			return data.indexOf(item);
		} else {
			return -1;
		}
	}
	
	public Object getValueAt(int row, int col) {
		T item =
			(data == null) ? null :
				(row < 0 || row >= data.size()) ? null :
					(col < 0 || col >= getColumnCount()) ? null :
						data.get(row);
		
		if(item == null)
			return null;
		
		return getColumnValue(item, col);
	}

	public abstract Object getColumnValue(T item, int columnIndex);
}
