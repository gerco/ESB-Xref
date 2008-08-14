package nl.progaia.esbprocessdraw.store;

import java.io.File;

import javax.xml.bind.JAXBContext;
import javax.xml.bind.JAXBException;
import javax.xml.bind.Unmarshaller;

import nl.progaia.esbprocessdraw.schema.Process;

public class FileArtifactStore implements ArtifactStore {

	private static String BASE_PROCESS = "ESB/Processes";
	
	private final File processBase;
	
	private final Unmarshaller unmarshaller;
	
	public FileArtifactStore(File baseDirectory) throws JAXBException {
		super();
		
		// Set the base directory and the different artifact bases in it
		this.processBase = new File(baseDirectory, BASE_PROCESS);
		
		// Initialize JAXB
		JAXBContext context = JAXBContext.newInstance(Process.class);
		this.unmarshaller = context.createUnmarshaller();
	}

	public Process getProcess(String name) {
		try {
			return (Process) unmarshaller.unmarshal(new File(processBase, name + ".xml"));
		} catch (JAXBException e) {
			System.out.println("Error while loading process " + name);
			e.printStackTrace();
			return null;
		}
	}

}
