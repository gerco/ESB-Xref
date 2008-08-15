package nl.progaia.esbxref.ui;

import java.awt.Color;
import java.awt.Graphics;

import javax.swing.JPanel;

import nl.progaia.esbprocessdraw.ProcessRenderer;
import nl.progaia.esbprocessdraw.draw.esb.ESBProcess;

public class ESBProcessPanel extends JPanel {
	private ESBProcess process;

	public ESBProcessPanel() {
		setBackground(Color.WHITE);
		setOpaque(true);
	}
	
	@Override
	public void paint(Graphics g) {
		super.paint(g);
		
		if(process != null) {
//			System.out.println("Rendering " + process.getName());
			ProcessRenderer.renderProcess(process, g);
		}
	}

	public ESBProcess getProcess() {
		return process;
	}

	public void setProcess(ESBProcess process) {
		
		this.process = process;
		if(process != null)
			setPreferredSize(process.getSize());
			
		revalidate();
		repaint();
	}
}
