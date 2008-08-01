package nl.progaia.esbxref;

import com.sonicsw.deploy.IArtifact;
import com.sonicsw.deploy.artifact.ESBArtifact;

public class ArtifactDisplay {

	/**
	 * @param args
	 */
	public static void main(String[] args) {
		IArtifact a = new ESBArtifact("/Processes/someProcess");
		
		System.out.println(a.getArchiveParentPath());
		System.out.println(a.getArchivePath());
		System.out.println(a.getDisplayType());
		System.out.println(a.getExtension());
		System.out.println(a.getName());
		System.out.println(a.getParentPath());
		System.out.println(a.getPath());
		System.out.println(a.getRootDirectory());
	}

}
