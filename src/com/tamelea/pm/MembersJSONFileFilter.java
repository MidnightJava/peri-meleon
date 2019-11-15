package com.tamelea.pm;

import java.io.File;

import javax.swing.filechooser.FileFilter;

public final class MembersJSONFileFilter extends FileFilter {

	@Override
	public boolean accept(File f) {
		return f.getName().endsWith(".json") || f.isDirectory();
	}

	@Override
	public String getDescription() {
		return "Exported data (*.json)";
	}

}
