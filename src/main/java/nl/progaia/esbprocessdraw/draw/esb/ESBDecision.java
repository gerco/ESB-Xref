package nl.progaia.esbprocessdraw.draw.esb;

import java.awt.Color;
import java.awt.Dimension;
import java.awt.Graphics;
import java.util.List;

import nl.progaia.esbprocessdraw.draw.Border;
import nl.progaia.esbprocessdraw.draw.Drawable;
import nl.progaia.esbprocessdraw.draw.HorizontalDrawable;
import nl.progaia.esbprocessdraw.schema.ItineraryType.Decision;
import nl.progaia.esbprocessdraw.schema.ItineraryType.Decision.Option;

public class ESBDecision extends HorizontalDrawable {

	private static final int DIAMOND_SIZE = 15;
//	private final Decision decision;

	public ESBDecision(Decision decision) {
		super();
//		this.decision = decision;
//		System.out.println("Found decision: " + decision.getName());

		// Empty border
		setBorder(new Border());
		
		List<Option> options = decision.getOption();
		for(Option option: options) {
			ESBItinerary itinerary = new ESBItinerary(option);
			itinerary.setMarginTop(0);
			itinerary.setMarginBottom(0);
			add(itinerary);
		}
	}

	@Override
	public void draw(Graphics g) {
//		System.out.println("Drawing decision: " + decision.getName());
		Dimension mySize = getSize();
		int y = 0;
		int center = mySize.width/2;
		
		// Reserve space for the diamond
		y += DIAMOND_SIZE * 2;

		// Insert a space to draw the lines in
		y += DIAMOND_SIZE * 4;
		
		// Draw lines to each of the components
		int x = 0;
		for(Drawable drawable: this) {
			x += drawable.getSize().width/2;
			g.drawLine(
					center, DIAMOND_SIZE, 
					x, y);
			x += drawable.getSize().width/2 + getSpacing();
		}
		
		// Draw the diamond at the top to hide the tops of the lines
		g.setColor(new Color(252, 251, 226));
		g.fillPolygon(
				new int[] {center, center + DIAMOND_SIZE, center          , center - DIAMOND_SIZE}, 
				new int[] {0     , DIAMOND_SIZE         , DIAMOND_SIZE * 2,  DIAMOND_SIZE}, 
				4);
		g.setColor(Color.gray);
		g.drawPolygon(
				new int[] {center, center + DIAMOND_SIZE, center          , center - DIAMOND_SIZE}, 
				new int[] {0     , DIAMOND_SIZE         , DIAMOND_SIZE * 2,  DIAMOND_SIZE}, 
				4);
		
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
	 * The size of a decision is the size of it's children plus the height of
	 * the triangle.
	 */
	@Override
	public Dimension getSize() {
		Dimension d = super.getSize();
		return new Dimension(d.width, d.height+DIAMOND_SIZE*6);
	}
	
}
