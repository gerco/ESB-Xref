package nl.progaia.esbxref;

import java.io.ByteArrayOutputStream;
import java.io.File;
import java.io.IOException;
import java.io.InputStream;
import java.util.HashMap;
import java.util.Map;
import java.util.zip.ZipEntry;
import java.util.zip.ZipFile;

import javax.xml.parsers.DocumentBuilder;
import javax.xml.parsers.DocumentBuilderFactory;

import org.w3c.dom.Document;

import com.sonicsw.deploy.IArtifact;
import com.sonicsw.deploy.IArtifactTraversalContext;
import com.sonicsw.deploy.artifact.ESBArtifact;
import com.sonicsw.deploy.artifact.SonicFSArtifact;
import com.sonicsw.deploy.storage.AbstractArtifactStorage;

/**
 * Implements an artifact storage for SDM models. Only implements ESB and SonicFS artifacts!
 * @author n421326
 */
public class SDMArtifactStorage extends AbstractArtifactStorage {
	private File modelDir;
	private Map<String, ZipEntry> artifacts = new HashMap<String, ZipEntry>();
	private Map<ZipEntry, ZipFile> xars = new HashMap<ZipEntry, ZipFile>();
	
	public SDMArtifactStorage() {
		setStoreName("Storage[SDM]");
	}
	
	public void setModelDir(File modelDir) {
		this.modelDir = modelDir;
		readAllXarFiles();
	}
	
	private void readAllXarFiles() {
		File xarsDir = new File(modelDir, "xars");
		if(!xarsDir.exists() || !xarsDir.isDirectory())
			return;
		
		File[] xarFiles = xarsDir.listFiles(new ExtensionFilenameFilter("xar"));
		for(File xarFile: xarFiles) {
			if(xarFile.canRead())
				readXarFile(xarFile);
		}
	}
	
	private void readXarFile(File xarFile) {
		try {
			ZipFile xar = new ZipFile(xarFile);
			for(ZipEntry entry: new EnumerationIterable<ZipEntry>(xar.entries())) {
				if(entry.isDirectory())
					continue;
				artifacts.put(entry.getName(), entry);
				xars.put(entry, xar);
			}
		} catch (IOException e) {
			throw new RuntimeException(e);
		}
	}
	
	@Override
	protected void _listConfigBeanArtifacts(IArtifact arg0, IArtifactTraversalContext arg1) throws Exception {
//		System.out.println(String.format("_listConfigBeanArtifacts(%s, %s)", arg0, arg1));
	}

	@Override
	protected void _listESBArtifacts(IArtifact parent, IArtifactTraversalContext context) throws Exception {
//		System.out.println(String.format("_listESBArtifacts(%s, %s)", parent.getArchivePath(), context));
		String zipEntryPrefix = getZipEntryName(parent);
		for(String name: artifacts.keySet()) {
			if(name.startsWith(zipEntryPrefix) && name.toLowerCase().endsWith(".xml")) {
				ESBArtifact artifact = new ESBArtifact(parent, getESBName(name));
				context.addTraversed(artifact);
			}
		}
	}

	@Override
	protected void _listSonicFSArtifacts(IArtifact parent, IArtifactTraversalContext context) throws Exception {
//		System.out.println(String.format("_listSonicFSArtifacts(%s, %s)", parent, context));
		String zipEntryPrefix = getZipEntryName(parent);
		for(String name: artifacts.keySet()) {
			if(name.startsWith(zipEntryPrefix)) {
				context.addTraversed(new SonicFSArtifact(parent, name.substring(zipEntryPrefix.length())));
			}
		}
	}

	@Override
	protected void _listSystemArtifacts(IArtifactTraversalContext arg0) throws Exception {
//		System.out.println(String.format("_listSystemArtifacts(%s)", arg0));
	}

	public void deleteAll() throws Exception {
		throw new RuntimeException("Operation not implemented");
	}

	public boolean exists(IArtifact artifact) throws Exception {
		String name = getZipEntryName(artifact);
		return artifacts.containsKey(name);
	}

	private InputStream getContentsAsStream(IArtifact artifact) throws Exception {
		String name = getZipEntryName(artifact);
		if(name.startsWith("ESB"))
			name = name + ".xml";
		
		ZipEntry entry = artifacts.get(name);
		if(entry == null) {
			System.out.println(name + " not found");
			return null;
		} else {
			return xars.get(entry).getInputStream(entry);
		}
	}
	
	public byte[] getContentsAsBytes(IArtifact artifact) throws Exception {
		InputStream in = getContentsAsStream(artifact);
		ByteArrayOutputStream bos = new ByteArrayOutputStream();
		if(in != null) {
			int numBytes = -1;
			byte[] buffer = new byte[10240];
			do {
				numBytes = in.read(buffer);
				if(numBytes > -1)
					bos.write(buffer, 0, numBytes);
			} while(numBytes > -1);
		}
		return bos.toByteArray();
	}

	public Document getContentsAsDom(IArtifact artifact) throws Exception {
		DocumentBuilderFactory dbf = DocumentBuilderFactory.newInstance();
		dbf.setNamespaceAware(true);
		DocumentBuilder db = dbf.newDocumentBuilder();
		return db.parse(getContentsAsStream(artifact));
	}

	public String getContentsAsString(IArtifact artifact) throws Exception {
		byte[] bytes = getContentsAsBytes(artifact);
		return new String(bytes);
	}

	public void store(IArtifact arg0, Object arg1) throws Exception {
		throw new RuntimeException("Operation not implemented");
	}

	private String getESBName(String name) {
		String fileName = name.substring(name.lastIndexOf('/'));
		return fileName.substring(1, fileName.lastIndexOf('.'));
	}
	
	private String getZipEntryName(IArtifact artifact) {
		return artifact.getArchivePath().substring(1);
	}
}
