package components;

import java.awt.Color;
import java.awt.Component;

import javax.swing.JTable;
import javax.swing.table.DefaultTableCellRenderer;

public class GreyCellRenderer extends DefaultTableCellRenderer {

		@Override
		public Component getTableCellRendererComponent(JTable table,
				Object value, boolean isSelected, boolean hasFocus, int row,
				int column) {
			setBackground(null);
			super.getTableCellRendererComponent( table,	 value,  isSelected,  hasFocus,  row,	 column);	
			if(!isSelected)			setBackground(new Color(220, 220, 220));
			//else 					setBackground(new Color(150, 80, 80));
			return this;
		}//getTableCellRendererComponent
	}//CellRenderer


