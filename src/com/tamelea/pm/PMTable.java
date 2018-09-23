package com.tamelea.pm;

import java.awt.Color;
import java.awt.Component;
import java.awt.Graphics;
import java.awt.event.MouseEvent;
import java.awt.event.MouseListener;

import javax.swing.BorderFactory;
import javax.swing.JLabel;
import javax.swing.JTable;
import javax.swing.SwingConstants;
import javax.swing.UIManager;
import javax.swing.border.LineBorder;
import javax.swing.table.AbstractTableModel;
import javax.swing.table.JTableHeader;
import javax.swing.table.TableCellRenderer;


@SuppressWarnings("serial")
public class PMTable extends JTable{
	private static final Color lineColor = Color.lightGray;

	public PMTable(AbstractTableModel model){
		super(model);
		setBorder(new LineBorder(lineColor));
		setupRowSorter();

	}

	public PMTable(){
		super();
		setBorder(new LineBorder(lineColor));
		setupRowSorter();
	}
	
	//workaround for OSX, to make sort indicator appear
	private void setupRowSorter() {
		setAutoCreateRowSorter(true);
		getTableHeader().addMouseListener(new MouseListener() {

			@Override
			public void mouseClicked(MouseEvent e) {
				getTableHeader().putClientProperty("JTableHeader.selectedColumn", getTableHeader().columnAtPoint(e.getPoint()));
				if (getTableHeader().getClientProperty("JTableHeader.sortDirection") == null) {
					getTableHeader().putClientProperty("JTableHeader.sortDirection", "ascending");
				} else if (getTableHeader().getClientProperty("JTableHeader.sortDirection").equals("ascending")) {
					getTableHeader().putClientProperty("JTableHeader.sortDirection", "decending");//misspelling is required, per Apple documentation
				} else {
					getTableHeader().putClientProperty("JTableHeader.sortDirection", "ascending");
				}
			}

			@Override
			public void mouseEntered(MouseEvent arg0) {
				// TODO Auto-generated method stub
				
			}

			@Override
			public void mouseExited(MouseEvent arg0) {
				// TODO Auto-generated method stub
				
			}

			@Override
			public void mousePressed(MouseEvent arg0) {
				// TODO Auto-generated method stub
				
			}

			@Override
			public void mouseReleased(MouseEvent arg0) {
				// TODO Auto-generated method stub
				
			}
			
		});
	}

	/* Manually paint horizontal and vertical lines for the table, since
	/  JTable.setShowHorizontalLines() and JTable.setShowVerticalLines() are
	/  ignored in the OSX JVM */
	public void paintComponent(Graphics g){
		super.paintComponent(g);
		if (PeriMeleon.getOSName() == OSName.WINDOWS){
			return;
		}
		int y = 0, x = 0;
		g.setColor(lineColor);
		//draw horizontal lines between rows
		for (int i = 0; i < getRowCount(); i++){
			y = y + getRowHeight(i);
			g.drawLine(0, y, getSize().width, y);
		}
		//draw vertical lines between columns
		for (int i = 0; i < getColumnCount(); i++){
			x = x + getColumnModel().getColumn(i).getWidth();
			g.drawLine(x, 0, x, getSize().height);
		}
	}
	//draw border for each cell in the table header
	//If this method had a friendly name, it would be "Ode to the Stack Pointer"
	public JTableHeader createDefaultTableHeader(){
		if (PeriMeleon.getOSName() == OSName.WINDOWS){
			return new JTableHeader(this.getColumnModel());
		}
		return new JTableHeader(this.getColumnModel()){
			public TableCellRenderer getDefaultRenderer(){
				return new TableCellRenderer(){
					public Component getTableCellRendererComponent(JTable table,
							Object value,
							boolean isSelected,
							boolean hasFocus,
							int row,
							int column){
						JLabel label = new JLabel();
						label.setText(value.toString());
						label.setHorizontalAlignment(SwingConstants.CENTER);
						label.setBorder(BorderFactory.createLineBorder(lineColor));
						label.setFont(UIManager.getFont("TableHeader.font"));
						return label;						
					}
				};
			}
		};
	}
}
