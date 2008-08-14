package nl.progaia.esbprocessdraw.draw;

import java.awt.Dimension;
import java.awt.Graphics;
import java.util.ArrayList;

/**
 * Simple Drawable implementation that allows the addition of children. It 
 * does not impose any organisation of the components and does not implement
 * the draw() or getSize() calls.
 * 
 * Subclasses should override the draw() and getSize() calls to arrange the
 * child components in some way.
 * 
 * @author Gerco Dries (gdr@progaia-rs.nl)
 *
 */
public abstract class CompositeDrawable extends ArrayList<Drawable> implements Drawable {
	
	// By default, the border is empty and takes up no space
	private Border border = new Border();
	private int marginTop = 0;
	private int marginBottom = 0;

	public CompositeDrawable() {
		super();
	}

	/**
	 * Draw the component's border and the internal components.
	 * 
	 */
	public void draw(Graphics g) {
		Dimension size = getSize();
		
		border.draw(g, size);
		
		drawComponents(g.create(
				border.getLeftWidth(), 
				border.getTopWidth() + getMarginTop(),
				(int)size.getWidth() - border.getLeftWidth() - border.getRightWidth(),
				(int)size.getHeight() - border.getTopWidth() - border.getBottomWidth() - getMarginTop() - getMarginBottom()
			));
	}

	/**
	 * The size is the size of the child components plus the border width on
	 * all sides.
	 */
	public Dimension getSize() {		
		Dimension d = getComponentsSize();
		
		double width = 
			(int)d.getWidth() + border.getLeftWidth() + border.getRightWidth();
		double height = 
			(int)d.getHeight() + border.getTopWidth() + border.getBottomWidth() +
			getMarginTop() + getMarginBottom();
		
//		System.out.println(getClass().getSimpleName() + " width = " + width);
		
		return new Dimension((int)width, (int)height);
	}
	
	/**
	 * Set the Border for this Drawable
	 * 
	 * @param border
	 */
	public void setBorder(Border border) {
		this.border = border;
	}

	public Border getBorder() {
		return border;
	}	
	
	/**
	 * Draw the child components
	 * 
	 * @param g
	 */
	protected abstract void drawComponents(Graphics g);
	
	/**
	 * Determine the size of the child components
	 * 
	 * @return
	 */
	protected abstract Dimension getComponentsSize();

	public void setMarginTop(int marginTop) {
		this.marginTop = marginTop;
	}

	public int getMarginTop() {
		return marginTop;
	}

	public void setMarginBottom(int marginBottom) {
		this.marginBottom = marginBottom;
	}

	public int getMarginBottom() {
		return marginBottom;
	} 
}
