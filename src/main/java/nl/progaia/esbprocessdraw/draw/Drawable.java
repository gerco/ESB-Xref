package nl.progaia.esbprocessdraw.draw;

import java.awt.Dimension;
import java.awt.Graphics;

/**
 * Defines a drawable component. Supports methods for drawing it on a variety 
 * of surfaces (currently, only a Java2D Graphics is available. Native SVG 
 * support should be added at some time)
 * 
 * @author Gerco Dries (gdr@progaia-rs.nl)
 *
 */
public interface Drawable {
	public void draw(Graphics g);
	public abstract Dimension getSize();
}
