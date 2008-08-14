package nl.progaia.esbprocessdraw.draw;

import java.awt.Color;
import java.awt.Dimension;
import java.awt.Graphics;

/**
 * Draws a border consisting of a 1-pixel solid rectangle and possibly some empty space.
 * 
 * @author Gerco Dries (gdr@progaia-rs.nl)
 *
 */
public class SolidBorder extends Border {

	private Color color = Color.black;
	private boolean rounded = false;

	public SolidBorder() {
		super();
	}

	public SolidBorder(int top, int left, int bottom, int right) {
		super(top, left, bottom, right);
	}

	@Override
	public void draw(Graphics g, Dimension d) {
//		System.out.println("Drawing SolidBorder");
		
		g.setColor(getColor());
		if(isRounded()) {
			g.drawRoundRect(0, 0, (int)d.getWidth()-1, (int)d.getHeight()-1, 10, 10);
		} else {
			g.drawRect(0, 0, (int)d.getWidth()-1, (int)d.getHeight()-1);
		}
	}

	public void setColor(Color color) {
		this.color = color;
	}

	public Color getColor() {
		return color;
	}

	public void setRounded(boolean rounded) {
		this.rounded = rounded;
	}

	public boolean isRounded() {
		return rounded;
	}

}
