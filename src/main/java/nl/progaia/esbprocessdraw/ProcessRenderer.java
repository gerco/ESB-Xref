package nl.progaia.esbprocessdraw;

import java.awt.Color;
import java.awt.Dimension;
import java.awt.Graphics;
import java.io.StringReader;

import javax.xml.bind.JAXBContext;
import javax.xml.bind.JAXBException;
import javax.xml.bind.Unmarshaller;

import nl.progaia.esb.Process;
import nl.progaia.esbprocessdraw.draw.esb.ESBProcess;

public class ProcessRenderer {
	
	public static void renderProcess(ESBProcess esbProcess, Graphics graphics) {
		// Create a buffered image in which to draw
		Dimension size = esbProcess.getSize();
//        BufferedImage img = 
//        	new BufferedImage(
//        			size.width, 
//        			size.height, 
//        			BufferedImage.TYPE_INT_RGB);
//        Graphics2D graphics = img.createGraphics();
        graphics.setColor(Color.white);
		graphics.fillRect(0, 0, size.width, size.height);
		
//		long startTime = System.currentTimeMillis();
//		System.out.print("Rendering: ");
		esbProcess.draw(graphics);
//		System.out.println((System.currentTimeMillis()-startTime) + "ms");
	}

	public static ESBProcess unmarshalProcess(String xml) {
		if(xml == null)
			return null;
		
		try {
			JAXBContext context = JAXBContext.newInstance(Process.class);
			Unmarshaller unmarshaller = context.createUnmarshaller();
			return new ESBProcess((Process)unmarshaller.unmarshal(
					new StringReader(xml)));
		} catch (JAXBException e) {
			e.printStackTrace();
			return null;
		}
	}
	
//	public static ESBProcess loadProcess(String processName) {
//		long startTime = System.currentTimeMillis();
//		System.out.print("Loading process " + processName + ": ");
//		Drawable loadedProcess = ESBProcess.getProcess(processName);
//		System.out.println((System.currentTimeMillis()-startTime) + "ms");
//		
//		if(loadedProcess instanceof ESBProcess)
//			return (ESBProcess)loadedProcess;
//		else
//			return null;
//	}

}
