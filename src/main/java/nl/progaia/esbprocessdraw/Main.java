package nl.progaia.esbprocessdraw;


public class Main {
//	private static ArtifactStore artifactStore;
//	
//	public static void main(String[] args) throws Exception {
//		new Main(args);
//	}
//	
//	public Main(String[] args) throws Exception {
//		if(args.length < 2) {
//			printUsage();
//			System.exit(0);
//		}
//
//		// Get arguments
//		String storeName = args[0];
//		String processName = args[1];
//		
//		// Figure out the type of artifact store specified
//		File storeFile = new File(storeName);
//		if(!storeFile.exists()) {
//			System.out.println(storeName + " does not exist!");
//			System.exit(1);
//		} else
//		if(storeFile.isFile()) {
//			// Assume the store is a Zip/Xar file
//			setArtifactStore(new ZipArtifactStore(storeFile));
//		} else
//		if(storeFile.isDirectory()) {
//			// Assume the store is a directory where a XAR file has been extracted
//			setArtifactStore(new FileArtifactStore(storeFile));
//		}
//				
//		// Read the process
//		ESBProcess esbProcess = loadProcess(processName);
//		if(esbProcess == null) {
//			System.out.println("Unable to load process '" + processName + "'.");
//			return;
//		}
//		
//		// Render the process to an image
//	    BufferedImage img = renderProcess(esbProcess);
//		
//	    // Save the image to a file
//		String fileName = esbProcess.getName() + ".png";
//		if(args.length == 3) {
//			fileName = args[2];
//		}
//		writePNGImage(fileName, img);		
//    }
//	
//	private BufferedImage renderProcess(ESBProcess esbProcess) {
//		// Create a buffered image in which to draw
//		Dimension size = esbProcess.getSize();
//        BufferedImage img = 
//        	new BufferedImage(
//        			size.width, 
//        			size.height, 
//        			BufferedImage.TYPE_INT_RGB);
//        Graphics2D graphics = img.createGraphics();
//        graphics.setColor(Color.white);
//		graphics.fillRect(0, 0, size.width, size.height);
//		
//		long startTime = System.currentTimeMillis();
//		System.out.print("Rendering: ");
//		esbProcess.draw(graphics);
//		System.out.println((System.currentTimeMillis()-startTime) + "ms");
//		return img;
//	}
//
//	private ESBProcess loadProcess(String processName) {
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
//
//	private void printUsage() {
//		System.out.println(
//				"Usage: " + getClass().getSimpleName() + " <Artifact store> <Process name> [<Output file>]\n" +
//				"\n" +
//				"  Artifact store\n" +
//				"      A Xar file or a directory with its contents\n" +
//				"  Process name\n" +
//				"      The process to render\n" +
//				"  Output file\n" +
//				"      The file name to write to. Defaults to 'processname.png'\n");
//	}
//
//	public void writePNGImage(String fileName, RenderedImage img) throws IOException {
//		long startTime = System.currentTimeMillis();
//		System.out.print("Saving " + fileName + ": ");
//		ImageIO.write(img, "png", new File(fileName));
//		System.out.println((System.currentTimeMillis()-startTime) + "ms");
//	}
//
//	public static ArtifactStore getArtifactStore() {
//		return artifactStore;
//	}
//
//	private static void setArtifactStore(ArtifactStore artifactStore) {
//		Main.artifactStore = artifactStore;
//	}
}
