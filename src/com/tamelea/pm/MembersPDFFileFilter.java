package com.tamelea.pm;

import java.io.File;

import javax.swing.filechooser.FileFilter;

public final class MembersPDFFileFilter extends FileFilter {

	@Override
	public boolean accept(File f) {
		return f.getName().endsWith(".pdf") || f.isDirectory();
	}

	@Override
	public String getDescription() {
		return "Files exported as PDF (*.pdf)";
	}

}
