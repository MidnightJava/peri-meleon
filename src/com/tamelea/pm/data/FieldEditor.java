package com.tamelea.pm.data;

import java.awt.Window;

import javax.swing.JComponent;

/**
 * Superclass of all field editors.
 *
 */
public abstract class FieldEditor {
	protected Window parent;
	protected Data data;
	protected Object initial;
	
	protected FieldEditor(Window parent, Data data, Object initial) {
		this.parent = parent;
		this.data = data;
		this.initial = initial;
	}
	
	public abstract JComponent getComponent();
	
	public abstract Object getValue();
	
	public abstract boolean isValid();
}
