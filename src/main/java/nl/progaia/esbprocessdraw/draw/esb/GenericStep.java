package nl.progaia.esbprocessdraw.draw.esb;

import java.awt.Color;
import java.awt.Dimension;
import java.awt.FontMetrics;
import java.awt.Graphics;
import java.awt.Image;
import java.io.IOException;

import javax.imageio.ImageIO;

import nl.progaia.esbprocessdraw.draw.Drawable;

public class GenericStep implements Drawable {

	private final String text;
	private       Image icon;
	
	public GenericStep(String text) {
		super();
		this.text = text;
		
		try {
			icon = ImageIO.read(getClass().getResource("esbp.gif"));
		} catch (IOException e) {
			icon = null;
			e.printStackTrace();
		}
	}

	public void draw(Graphics g) {
		Dimension d = getSize();
		FontMetrics fm = g.getFontMetrics();
		
		g.setColor(Color.magenta);
		g.drawRoundRect(0, 0, d.width-1, d.height-1, 10, 10);		
		
		// Draw the step name (centered in the box)
		int textWidth = fm.stringWidth(text);
		int iconWidth = icon.getWidth(null);
		int totalWidth = iconWidth + 2 + textWidth;
		
		g.setColor(Color.black);
		g.drawImage(icon, (d.width/2) - (totalWidth/2), 5, null);
		g.drawString(text, (d.width/2) - (totalWidth/2) + iconWidth + 2, 17);
	}

	public Dimension getSize() {
		int width = text.length() * 8;
			
		return new Dimension(10 + width , 25);
	}

}
