package com.tamelea.pm;

import java.io.File;

import javax.swing.filechooser.FileFilter;

public final class MembersXMLFileFilter extends FileFilter {

	@Override
	public boolean accept(File f) {
		return f.getName().endsWith(".xml") || f.isDirectory();
	}

	@Override
	public String getDescription() {
		return "Member databases (*.xml)";
	}

}
