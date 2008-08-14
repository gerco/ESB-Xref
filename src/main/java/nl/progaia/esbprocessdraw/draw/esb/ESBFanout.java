package nl.progaia.esbprocessdraw.draw.esb;

import java.awt.Color;
import java.awt.Dimension;
import java.awt.Graphics;
import java.util.List;

import nl.progaia.esbprocessdraw.draw.Border;
import nl.progaia.esbprocessdraw.draw.Drawable;
import nl.progaia.esbprocessdraw.draw.HorizontalDrawable;
import nl.progaia.esbprocessdraw.schema.FanoutType;
import nl.progaia.esbprocessdraw.schema.FanoutType.Path;

/**
 * A fanout is displayed by a triangle and draws lines to each of it's
 * components.
 * 
 * @author Gerco Dries (gdr@progaia-rs.nl)
 *
 */
public class ESBFanout extends HorizontalDrawable {
	
	private static final int TRIANGLE_WIDTH = 15;
	private static final int TRIANGLE_HEIGHT = 30;
//	private final FanoutType fanout;

	public ESBFanout(FanoutType fanout) {
		super();
//		this.fanout = fanout;
//		System.out.println("Found fanout: " + fanout.getName());
		
		// Set an emty border
		setBorder(new Border());
		
		List<Path> paths = fanout.getPath();
		for(Path path: paths) {
			ESBItinerary itinerary = new ESBItinerary(path);
			itinerary.setMarginTop(0);
			itinerary.setMarginBottom(0);
			add(itinerary);
		}
	}

	@Override
	public void draw(Graphics g) {
//		System.out.println("Drawing fanout: " + fanout.getName());
		Dimension mySize = getSize();
		int y = 0;
		int center = mySize.width/2;

		// Reserve the space for the triangle
		y += TRIANGLE_HEIGHT;

		// Insert a space to draw the lines in
		y += TRIANGLE_HEIGHT * 2;
		
		// Draw lines to each of the components
		int x = 0;
		for(Drawable drawable: this) {
			x += drawable.getSize().width/2;
			g.setColor(Color.black);
			g.drawLine(
					center, TRIANGLE_HEIGHT/2, 
					x, y);
			x += drawable.getSize().width/2 + getSpacing();
		}
		
		// Draw the triangle at the top now to hide the top of the lines
		g.setColor(new Color(252, 251, 226));
		g.fillPolygon(
				new int[] {center, center + TRIANGLE_WIDTH, center - TRIANGLE_WIDTH}, 
				new int[] {0     , TRIANGLE_HEIGHT        , TRIANGLE_HEIGHT}, 
				3);
		g.setColor(Color.gray);
		g.drawPolygon(
				new int[] {center, center + TRIANGLE_WIDTH, center - TRIANGLE_WIDTH}, 
				new int[] {0     , TRIANGLE_HEIGHT        , TRIANGLE_HEIGHT}, 
				3);		
		
		// Draw the components
		super.draw(g.create(0, y, mySize.width, mySize.height-y));
		
		// Draw lines from the bottom of each component to the bottom of the decision
//		x = 0;
//		for(Drawable drawable: this) {
//			Dimension dSize = drawable.getSize();
//			x += dSize.width/2;
//			
//			g.drawLine(
//					x, y + dSize.height, 
//					center, mySize.height);
//			x += dSize.width/2 + getSpacing();
//		}
	}

	/**
	 * The size of a fanout is the size of it's children plus the height of
	 * the triangle.
	 */
	@Override
	public Dimension getSize() {
		Dimension d = super.getSize();
		return new Dimension(d.width, d.height+TRIANGLE_HEIGHT*3);
	}
	
}
