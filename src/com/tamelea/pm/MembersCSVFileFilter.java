package com.tamelea.pm;

import java.io.File;

import javax.swing.filechooser.FileFilter;

public final class MembersCSVFileFilter extends FileFilter {

	@Override
	public boolean accept(File f) {
		return f.getName().endsWith(".csv") || f.isDirectory();
	}

	@Override
	public String getDescription() {
		return "Data for import (*.csv)";
	}

}
