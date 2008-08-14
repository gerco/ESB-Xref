package nl.progaia.esbprocessdraw.draw.esb;

import java.awt.Color;
import java.awt.Dimension;
import java.awt.Font;
import java.awt.FontMetrics;
import java.awt.Graphics;
import java.awt.Image;
import java.io.IOException;

import javax.imageio.ImageIO;

import nl.progaia.esbprocessdraw.draw.Drawable;
import nl.progaia.esbprocessdraw.schema.StepType;

public class ESBStep implements Drawable {

	private final StepType step;
	private Color bgColor;
	private       Image icon;
	
	public ESBStep(StepType step) {
		super();
		this.step = step;
		bgColor = new Color(252, 251, 226);
		
		try {
			icon = ImageIO.read(getClass().getResource("service.gif"));
		} catch (IOException e) {
			icon = null;
			e.printStackTrace();
		}
		
//		System.out.println("Found step: " + step.getName());
	}

	/**
	 * Draw the step name in a rounded rect
	 */
	public void draw(Graphics g) {
//		System.out.println("Drawing step: " + step.getName());
		
		Dimension d = getSize();
		FontMetrics fm = g.getFontMetrics();
		
		g.setColor(bgColor);
		g.fillRoundRect(0, 0, d.width-1, d.height-1, 10, 10);
		
		// Draw the outline
		g.setColor(Color.gray);
		g.drawRoundRect(0, 0, d.width-1, d.height-1, 10, 10);		
		
		// Draw the endpointRef
		int refWidth = fm.stringWidth(step.getEndpointRef());
		g.setColor(Color.black);
		g.drawString(step.getEndpointRef(), (d.width/2) - (refWidth/2), 34);
		
		// Draw the step name (centered in the box)
		int textWidth = fm.stringWidth(step.getName());
		int iconWidth = icon.getWidth(null);
		int totalWidth = iconWidth + 2 + textWidth;
		
		g.setFont(g.getFont().deriveFont(Font.BOLD));
		g.drawImage(icon, (d.width/2) - (totalWidth/2), 5, null);
		g.drawString(step.getName(), (d.width/2) - (totalWidth/2) + iconWidth + 2, 17);		
	}

	/**
	 * Return 10 + the name of the step (or endpointref) * 8 pixels. 
	 * Fixed height of 40 pixels.
	 */
	public Dimension getSize() {
		int nameWidth = icon.getWidth(null) + 2 + step.getName().length() * 8;
		int refWidth = step.getEndpointRef().length() * 8;
		int width = nameWidth < refWidth ? refWidth : nameWidth; 
			
		return new Dimension(10 + width , 40);
	}

}
