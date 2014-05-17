package components;

import java.awt.Toolkit;
import java.awt.datatransfer.Clipboard;
import java.awt.datatransfer.DataFlavor;
import java.awt.datatransfer.Transferable;
import java.awt.event.KeyEvent;
import java.awt.event.KeyListener;

import javax.swing.JTable;

 /** Ёто таблица, котора€ умеет вставл€ть данные и системного буфера при нажатии ctrl+V и удал€ть из по кнопке delete*/
public class EJTable extends JTable implements KeyListener {
	
	public EJTable(AbstractItemsTableModel<?> model){
		super(model);
		setCellSelectionEnabled(true);
		addKeyListener(this);			
	}
	
	@Override
	public void keyTyped(KeyEvent e) {	}

	@Override
	public void keyPressed(KeyEvent e) {		
		if (e.getKeyCode () == KeyEvent.VK_V && e.isControlDown()){
			int startRow = getSelectedRow();
			int startCol = getSelectedColumn();
			if (startRow != -1 || startCol !=-1 ){
				insertFromBuffer(startRow, startCol);
				repaint();
				}	 
			}
		else if (e.getKeyCode () == KeyEvent.VK_DELETE){
			for(int row : getSelectedRows ())
				for ( int column: getSelectedColumns())
					getModel().setValueAt(null, row, column);
			repaint();
		}
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
					int rowCount = getModel().getRowCount();
					if (startRow+j >= rowCount) {
						((AbstractItemsTableModel<?>)getModel()).addRow(rowCount-1);
					}
					for(int i = 0; i <words.length && startCol + i < getModel().getColumnCount(); i++ ){						 	
						getModel().setValueAt(words[i], startRow+j, startCol +i);							 
					}					
				}
				((AbstractItemsTableModel<?>)getModel()).fireTableRowsInserted(0, getModel().getRowCount()-1);
			} 	
		} catch (Exception e) {
			e.printStackTrace();
		}
	}

	@Override
	public void keyReleased(KeyEvent e) {}
}