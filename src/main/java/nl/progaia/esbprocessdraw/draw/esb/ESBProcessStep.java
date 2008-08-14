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

public class ESBProcessStep implements Drawable {

	private final String processName;
	private Color bgColor;
	private       Image icon;
	
	public ESBProcessStep(String processName) {
		super();
		this.processName = processName;
		bgColor = new Color(252, 251, 226);
		
		try {
			icon = ImageIO.read(getClass().getResource("esbp.gif"));
		} catch (IOException e) {
			icon = null;
			e.printStackTrace();
		}
	}

	/**
	 * Draw the process name in a rounded rect
	 */
	public void draw(Graphics g) {
		Dimension d = getSize();
		FontMetrics fm = g.getFontMetrics();
		
		g.setColor(bgColor);
		g.fillRoundRect(0, 0, d.width-1, d.height-1, 10, 10);
		
		// Draw the outline
		g.setColor(Color.gray);
		g.drawRoundRect(0, 0, d.width-1, d.height-1, 10, 10);		
				
		// Draw the process name (centered in the box)
		int textWidth = fm.stringWidth(processName);
		int iconWidth = icon.getWidth(null);
		int totalWidth = iconWidth + 2 + textWidth;
		
		g.setColor(Color.black);
		g.setFont(g.getFont().deriveFont(Font.BOLD));
		g.drawImage(icon, (d.width/2) - (totalWidth/2), 5, null);
		g.drawString(processName, (d.width/2) - (totalWidth/2) + iconWidth + 2, 17);		
	}

	/**
	 * Return 10 + the name of the process * 8 pixels. 
	 * Fixed height of 40 pixels.
	 */
	public Dimension getSize() {
		int nameWidth = icon.getWidth(null) + 2 + processName.length() * 8;
		int refWidth = 0; // step.getEndpointRef().length() * 8;
		int width = nameWidth < refWidth ? refWidth : nameWidth; 
			
		return new Dimension(10 + width , 40);
	}

}
