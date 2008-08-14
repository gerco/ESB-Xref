package nl.progaia.esbprocessdraw.draw;

import java.awt.Color;
import java.awt.Dimension;
import java.awt.Graphics;

/**
 * This Drawable arranges it's components vertically.
 * 
 * @author Gerco Dries (gdr@progaia-rs.nl)
 *
 */
public class VerticalDrawable extends CompositeDrawable {
	
	// By default, there is 15 pixels of space between the components
	int spacing = 15;
	
	public VerticalDrawable() {
		super();
		
		// Set a default top and bottom margin equal to the spacing
		setMarginTop(getSpacing());
		setMarginBottom(getSpacing());
	}

	/**
	 * Draws the child components stacked vertically
	 */
	public void drawComponents(Graphics g) {
		Dimension mySize = getComponentsSize();
		
		// Draw the border and set initial values for the drawing coordinates
		int y = 0;
		
		// Draw all child components
		for(Drawable drawable: this) {
			Dimension d = drawable.getSize();
			
			// Draw the component on a subset of our Graphics object (Centered)
			int x = (mySize.width/2) - (d.width/2);
//			System.out.println("Drawing component at (" + x + ", " + y + ")");
			drawable.draw(g.create(x, y, (int)d.getWidth(), (int)d.getHeight()));
			
			// Increase Y to draw the next component below the previous one.
			int lastY = y;
			y += d.getHeight() + getSpacing();
			
			// Draw a line from the bottom of the last component to the top of the next
			// except for the last component
			if(y < mySize.height) {
				g.setColor(Color.black);
				g.drawLine(
						mySize.width/2, lastY + d.height, 
						x + d.width/2, y);
			}
		}
	}
	
	/**
	 * The width of this Drawable is the maximum width of the child components
	 * and the height is the total height of the children plus the spacing on
	 * both the top and bottom.
	 */
	@Override
	protected Dimension getComponentsSize() {
		double width = 0;
		double height = 0;
		
		for(Drawable drawable: this) {
			Dimension d = drawable.getSize();
			
			if(width < d.getWidth())
				width = d.getWidth();
			
			height += d.getHeight() + getSpacing();
		}
				
		/**
		 * At this point, we counted 1 spacing too many, subtract it
		 */ 
		return new Dimension((int)width, (int)height - getSpacing());
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
