package nl.progaia.esbprocessdraw.draw;

import java.awt.Color;
import java.awt.Dimension;
import java.awt.FontMetrics;
import java.awt.Graphics;

public class TitledBorder extends SolidBorder {
	private final String title;

	public TitledBorder(String title) {
		super(18, 5, 5, 5);
		this.title = title;
	}

	public TitledBorder(String title, int top, int left, int bottom, int right) {
		super(18, left, bottom, right);
		this.title = title;
		setColor(Color.blue);
	}

	@Override
	public void draw(Graphics g, Dimension d) {
		super.draw(g, d);
		FontMetrics fm = g.getFontMetrics();
		
		// Draw a box for the title
		g.setColor(getColor());
		if(isRounded()) {
			g.fillRoundRect(0, 0, d.width, getTopWidth(), 10, 10);
		} else {
			g.fillRect(0, 0, d.width, getTopWidth());
		}
		
		// Draw the title itself
		g.setColor(Color.white);
		g.drawString(title, (d.width/2) - (fm.stringWidth(title)/2), 12);
	}
}
