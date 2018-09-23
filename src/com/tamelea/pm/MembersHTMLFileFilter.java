package com.tamelea.pm;

import java.io.File;

import javax.swing.filechooser.FileFilter;

public final class MembersHTMLFileFilter extends FileFilter {

	@Override
	public boolean accept(File f) {
		return f.getName().endsWith(".html") || f.isDirectory();
	}

	@Override
	public String getDescription() {
		return "Exported directory (*.html)";
	}

}
