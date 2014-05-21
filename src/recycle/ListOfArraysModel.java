package recycle;
import java.awt.Toolkit;
import java.awt.datatransfer.Clipboard;
import java.awt.datatransfer.DataFlavor;
import java.awt.datatransfer.Transferable;
import java.util.ArrayList;
import java.util.List;

import javax.swing.event.TableModelListener;
import javax.swing.table.AbstractTableModel;
import javax.swing.table.TableModel;

public class ListOfArraysModel extends AbstractTableModel {
	
private List<String[]> table  = new ArrayList<>();

public List<String[]> getTable() {
	return table;
}


public void setTable(List<String[]> table) {
	this.table = table;
}


private int columnCount; 	
	
	public ListOfArraysModel(int columnCount){
		this.columnCount = columnCount;
		reset();	// очищает данные и вставляет 10 пустых строчек	
	}
	
	
	public void reset(){
		table.clear();
		for (int i = 0; i < 10; i++)
			addRow();		
	}
	
	public void addRow(){
		table.add(new String[columnCount]); 
	}
	
	@Override
	public int getRowCount() {
		// TODO Auto-generated method stub
		return table.size();
	}

	@Override
	public int getColumnCount() {
		// TODO Auto-generated method stub
		return columnCount ;		
	}

	@Override
	public Object getValueAt(int rowIndex, int columnIndex) {
		return table.get(rowIndex)[columnIndex];
	}

	@Override
	public void setValueAt(Object aValue, int rowIndex, int columnIndex) {		
		table.get(rowIndex)[columnIndex] = (aValue != null)? aValue.toString():null;		
	}


	@Override
	public boolean isCellEditable(int rowIndex, int columnIndex) {			
		return true;
	}
   
	
	public void insertFromBuffer(int startRow, int startCol) {	
		 try {			 
			 Clipboard c = Toolkit.getDefaultToolkit().getSystemClipboard();
			 Transferable t = c.getContents(null);
			 if ( t.isDataFlavorSupported(DataFlavor.stringFlavor) ){
				 Object o = t.getTransferData( DataFlavor.stringFlavor );
				 String data = (String)t.getTransferData( DataFlavor.stringFlavor );
				 
				 String[] lines = data.split("\n");
				 for (int j = 0; j<lines.length; j++){
					 String[] words = lines[j].split("\t");
					 if (startRow+j >= table.size()){
						 addRow();
						 fireTableRowsInserted(0, getRowCount());
					 }
					 for(int i = 0; i <words.length && startCol +i<columnCount; i++ ){						 	
						 setValueAt(words[i], startRow+j, startCol +i);	 	 
						 
					 }					
				 }
		     } 	
		 } catch (Exception e) {
				e.printStackTrace();
		 }
	 
		
	}
	

	public String [] getRowAt(int i) {
		// TODO Auto-generated method stub
		return table.get(i);
	}


	public String []  removeRow(int i) {
		return table.remove(i);
	}

}

