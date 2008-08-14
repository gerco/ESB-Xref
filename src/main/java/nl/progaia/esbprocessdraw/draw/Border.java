package nl.progaia.esbprocessdraw.draw;

import java.awt.Dimension;
import java.awt.Graphics;
import java.awt.Insets;

public class Border {

	private Insets insets = new Insets(0, 0, 0, 0);
	
	/**
	 * Construct an empty border that doesn't take up space
	 */
	public Border() {};
	
	/**
	 * Construct a border that takes up the defined amount of space
	 * 
	 * @param top
	 * @param left
	 * @param bottom
	 * @param right
	 */
	public Border(int top, int left, int bottom, int right) {
		insets = new Insets(top, left, bottom, right);
	}
	
	/**
	 * This border doesn't draw anything. It only takes up space.
	 */
	public void draw(Graphics g, Dimension d) {
//		System.out.println("Drawing Border");
	}
	
	public Insets getInsets() {
		return insets;
	}

	public int getLeftWidth() {
		return insets.left;
	}

	public int getTopWidth() {
		return insets.top;
	}

	public int getBottomWidth() {
		return insets.bottom;
	}

	public int getRightWidth() {
		return insets.right;
	}	
}
