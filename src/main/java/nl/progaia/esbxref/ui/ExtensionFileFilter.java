package nl.progaia.esbxref.ui;

import java.io.File;
import javax.swing.filechooser.FileFilter;

public class ExtensionFileFilter extends FileFilter {

	private final String extension;
	private final String description;
	
	public ExtensionFileFilter(String extension, String description) {
		this.extension = extension.toLowerCase();
		this.description = description;
	}

	@Override
	public boolean accept(File f) {
		return f.isDirectory() || f.getName().toLowerCase().endsWith(extension);
	}

	@Override
	public String getDescription() {
		return description + " (*" + extension + ")";
	}

	
}
