package com.tamelea.pm;

import java.io.File;

import javax.swing.filechooser.FileFilter;

public final class MembersPERFileFilter extends FileFilter {

	@Override
	public boolean accept(File f) {
		return f.getName().endsWith(".per") || f.isDirectory();
	}

	@Override
	public String getDescription() {
		return "Encrypted member databases (*.per)";
	}

}
