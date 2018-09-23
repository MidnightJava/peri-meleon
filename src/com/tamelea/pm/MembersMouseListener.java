package com.tamelea.pm;

import java.awt.event.MouseAdapter;
import java.awt.event.MouseEvent;

public class MembersMouseListener extends MouseAdapter{
	
	private View view;

	public MembersMouseListener(View view){
		this.view = view;
	}
	
	@Override
	public void mouseClicked(MouseEvent e){
		if (e.getClickCount() == 2){
			view.editMember();
		}
	}
}