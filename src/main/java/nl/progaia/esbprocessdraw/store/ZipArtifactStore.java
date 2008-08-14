package nl.progaia.esbprocessdraw.store;

import java.io.File;
import java.io.IOException;
import java.util.zip.ZipEntry;
import java.util.zip.ZipException;
import java.util.zip.ZipFile;

import javax.xml.bind.JAXBContext;
import javax.xml.bind.JAXBException;
import javax.xml.bind.Unmarshaller;

import nl.progaia.esbprocessdraw.schema.Process;

public class ZipArtifactStore implements ArtifactStore {
	
	private static String BASE_PROCESS = "ESB/Processes";

	private final ZipFile zipFile;

	private Unmarshaller unmarshaller;
	
	public ZipArtifactStore(File zipFile) throws ZipException, IOException, JAXBException {
		this.zipFile = new ZipFile(zipFile);	
		
//		printContents();
		
		// Initialize JAXB
		JAXBContext context = JAXBContext.newInstance(Process.class);
		this.unmarshaller = context.createUnmarshaller();
	}
	
//	private void printContents() {
//		for(Enumeration<? extends ZipEntry> e = zipFile.entries(); e.hasMoreElements();) {
//			System.out.println(e.nextElement());
//		}
//	}

	public Process getProcess(String name) {
		try {
			String entryName = BASE_PROCESS + '/' + name + ".xml";
//			System.out.println("Finding entry " + entryName);
			ZipEntry entry = getZipEntry(entryName);
			if(entry == null)
				return null;
			
			return (Process)unmarshaller.unmarshal(zipFile.getInputStream(entry));
		} catch (IOException e) {
			System.out.println("Error while loading process " + name);
			e.printStackTrace();
			return null;
		} catch (JAXBException e) {
			System.out.println("Error while loading process " + name);
			e.printStackTrace();
			return null;
		}
		
	}

	/**
	 * The separator in Zip files is supposed to be the '/' character. Some tools however
	 * create zip files with '\' as the separator when running on Windows. Attempt to
	 * find the entry using '/' characters and retry using '\' if not found.
	 * 
	 * @param entryName
	 * @return
	 */
	private ZipEntry getZipEntry(String entryName) {
		ZipEntry entry = zipFile.getEntry(entryName);
		if(entry == null)
			entry = zipFile.getEntry(entryName.replace('/', '\\'));
		return entry;
	}

}
