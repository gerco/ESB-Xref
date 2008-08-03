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
package nl.progaia.esbxref.ui.status;

import java.awt.BorderLayout;
import java.awt.Dimension;
import java.net.URL;

import javax.swing.BorderFactory;
import javax.swing.Icon;
import javax.swing.ImageIcon;
import javax.swing.JLabel;
import javax.swing.JPanel;

import nl.progaia.esbxref.ui.progress.Throbber;

public class JStatusBar extends JPanel {
	private final JLabel statusLabel;
	private final Throbber throbber;
	private final Icon errorIcon;
	
	public JStatusBar() {
		errorIcon = loadErrorIcon();
		
		setLayout(new BorderLayout());
		setBorder(BorderFactory.createEmptyBorder(0, 5, 0, 5));
		setPreferredSize(new Dimension(10, 23));

		statusLabel = new JLabel();
		throbber = new Throbber();
			
		add(statusLabel, BorderLayout.WEST);
		add(throbber, BorderLayout.EAST);
		
		setText("");
		setBusy(false);
	}
	
	private Icon loadErrorIcon() {
		URL iconURL = getClass().getResource("error_16x16.gif");
		return new ImageIcon(iconURL);
	}

	public void setText(String text) {
		statusLabel.setText(text);
		statusLabel.setIcon(null);
	}
	
	public void setBusy(boolean busy) {
		throbber.setVisible(busy);
	}

	public void setErrorText(String message) {
		statusLabel.setText(message);
		statusLabel.setIcon(errorIcon);
	}
}
