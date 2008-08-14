package nl.progaia.esbprocessdraw.draw;

import java.awt.Dimension;
import java.awt.Graphics;

/**
 * This Drawable arranges it's components horizontally.
 * 
 * @author Gerco Dries (gdr@progaia-rs.nl)
 *
 */
public class HorizontalDrawable extends CompositeDrawable {

	// By default, there is 5 pixels of space between the components
	private int spacing = 5;
		
	/**
	 * Draws the child components stacked horizontally
	 */
	public void drawComponents(Graphics g) {		
		// Draw the border and set initial values for the drawing coordinates
		int x = 0;
		
		// Draw all child components
		for(Drawable drawable: this) {
			Dimension d = drawable.getSize();
			
			// Draw the component on a subset of our Graphics object
//			System.out.println("Drawing component at (" + curX + ", 0)");
			drawable.draw(g.create(x, 0, (int)d.getWidth(), (int)d.getHeight()));
			
			// Increase X to draw the next component next to the previous one.
			x += d.getWidth() + getSpacing();
		}
	}
	
	/**
	 * The height of this Drawable is the maximum height of the child components
	 * and the width is the total width of the children plus spacing.
	 */
	@Override
	protected Dimension getComponentsSize() {
		double width = 0;
		double height = 0;
		
		for(Drawable drawable: this) {
			Dimension d = drawable.getSize();
			
			if(height < d.getHeight())
				height = d.getHeight();
			
			width += d.getWidth() + getSpacing();
		}
		
		return new Dimension((int)width - getSpacing(), (int)height);
	}	

	/**
	 * Set the spacing between the components
	 * 
	 * @param spacing The amount of spacing in pixels
	 */
	public void setSpacing(int spacing) {
		this.spacing = spacing;
	}

	/**
	 * Get the spacing between the components
	 * 
	 * @return The spacing in pixels
	 */
	public int getSpacing() {
		return spacing;
	}

}
