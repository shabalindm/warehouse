package components;

import java.awt.Color;
import java.awt.Component;
import java.awt.Toolkit;
import java.awt.datatransfer.Clipboard;
import java.awt.datatransfer.DataFlavor;
import java.awt.datatransfer.Transferable;
import java.awt.event.KeyEvent;
import java.awt.event.KeyListener;
import java.math.BigDecimal;
import java.sql.Timestamp;
import java.text.DateFormat;
import java.util.Date;
import java.util.List;

import javax.swing.DefaultCellEditor;
import javax.swing.JTable;
import javax.swing.JTextField;
import javax.swing.SortOrder;
import javax.swing.table.DefaultTableCellRenderer;
import javax.swing.table.TableModel;
import javax.swing.table.TableRowSorter;



 /** Это таблица, которая умеет вставлять данные и системного буфера при нажатии ctrl+V и удалять из по кнопке delete*/
public class EJTable extends JTable implements KeyListener {
	DateFormat format = DateFormat.getDateTimeInstance();
	AbstractItemsTableModel<?> model;
	public EJTable(AbstractItemsTableModel<?> model){
		super(model);
		this.model = model;
		setCellSelectionEnabled(true);
		addKeyListener(this);	
		setRowSorter(new TableRowSorter<TableModel>(model){ 
			@Override
			public void toggleSortOrder(int column) {
	        List<? extends SortKey> sortKeys = getSortKeys();
	        if (sortKeys.size() > 0) {
	            if (sortKeys.get(0).getSortOrder() == SortOrder.DESCENDING) {
	                setSortKeys(null);
	                return;
	            }
	        }
	        super.toggleSortOrder(column);
	    }

	 });
		setRenderes();
		setEditors();
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
					getModel().setValueAt(null, convertRowIndexToModel(row), convertColumnIndexToModel(column));
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
					int modelRow = 0 ;
					if (startRow+j >= rowCount) {
						model.addRow(rowCount-1);	
						modelRow = rowCount;
					} else {
						modelRow = convertRowIndexToModel(startRow+j);
					}
					for(int i = 0; i <words.length && startCol + i < getModel().getColumnCount(); i++ ){						
						int modelCol = convertColumnIndexToModel(startCol +i);	
						Object value = words[i].trim();
						if (Date.class.isAssignableFrom(getModel().getColumnClass(modelCol))){
							try{value = new Timestamp((format.parse((String) value)).getTime());}
							catch(Exception e) {}							
						} else if (BigDecimal.class.isAssignableFrom(getModel().getColumnClass(modelCol))) {
							try {value = new BigDecimal((String) value);}
							catch (Exception e){}
						}						
						getModel().setValueAt(value, modelRow, modelCol);							 
					}					
				}
				model.fireTableRowsInserted(0, getModel().getRowCount()-1);
			} 	
		} catch (Exception e) {
			e.printStackTrace();
		}
	}

	@Override
	public void keyReleased(KeyEvent e) {}
	
	private void setEditors() {
		setDefaultEditor(BigDecimal.class,  new BigDecimalCellEditor(new JTextField()));
		setDefaultEditor(java.util.Date.class,  new DateCellEditor(new JTextField()));
	}

	public class DateCellEditor extends DefaultCellEditor {
		public DateCellEditor(final JTextField textField) {
			super(textField);
			delegate = new EditorDelegate() {  
				public void setValue(Object value) {  
					if (value instanceof Date)
						value = format.format(value);					
					textField.setText((value != null) ? value.toString() : "");  
				}  
				public Object getCellEditorValue() {
					Object result = textField.getText().trim();
					try {result =  new Timestamp((format.parse((String) result)).getTime());}
					catch (Exception e){}
					return result;  
				}  
			};  
			textField.addActionListener(delegate);
		}		

	}
	public class BigDecimalCellEditor extends DefaultCellEditor {
		public BigDecimalCellEditor(final JTextField textField) {
			super(textField);
			delegate = new EditorDelegate() {  
				public void setValue(Object value) {  				
					textField.setText((value != null) ? value.toString() : "");  
				}  
				public Object getCellEditorValue() {
					Object result = textField.getText().trim();
					try {result = new BigDecimal((String) result);}
					catch (Exception e){}
					return result;  
				}  
			};  
			textField.addActionListener(delegate);
		}		

	}
	
	public class StringCellEditor extends DefaultCellEditor {
		public StringCellEditor(final JTextField textField) {
			super(textField);
			delegate = new EditorDelegate() {  
				public void setValue(Object value) {  				
					textField.setText((value != null) ? value.toString() : "");  
				}  
				public Object getCellEditorValue() {
					Object result = textField.getText().trim();
					
//					if(aValue instanceof String){
//						aValue = ((String)aValue).trim();
//						if (aValue.equals(""))
//							aValue = null;
//					}	
					return result;  
				}  
			};  
			textField.addActionListener(delegate);
		}		

	}
	
	private void setRenderes() {
		setDefaultRenderer(Object.class, new CellRenderer());
		setDefaultRenderer(BigDecimal.class, new CellRenderer());
		setDefaultRenderer(java.util.Date.class, new CellRenderer());
	}


	public class CellRenderer extends DefaultTableCellRenderer {

		@Override
		public Component getTableCellRendererComponent(JTable table,
				Object value, boolean isSelected, boolean hasFocus, int row,
				int column) {
			setBackground(null);
			setForeground(null);
			int modelcolumn = table.convertColumnIndexToModel(column);
			int modelrow = table.convertRowIndexToModel(row);	
			boolean rightType = true;
			if (value != null){
				rightType = value.getClass().isAssignableFrom(model.getColumnClass(modelcolumn));
				if (value instanceof java.util.Date ){
					value = format.format(value);
				}
			}

			super.getTableCellRendererComponent( table,	 value,  isSelected,  hasFocus,  row,	 column);		

			if (model.isDeletedRow(modelrow)){				
				if(!isSelected)			setBackground(new Color(255, 150, 150));
				else 					setBackground(new Color(150, 80, 80));
			} 

			else if(model.isNewRow(modelrow)){				
					if(!isSelected)			setBackground(new Color(160, 255, 160)); 
					else 					setBackground(new Color(0, 140, 30));
			}	

			else if (model.isUpdatedSell(modelrow, modelcolumn)){//Обычная строчка с обновленным значением				
					if(!isSelected)			setBackground(new Color(16, 255, 160)); 
					else 					setBackground(new Color(0, 140, 30));
			}
					
			if (!rightType){
				setForeground(Color.RED);
				
			}
			return this;
		}//getTableCellRendererComponent
	}//CellRenderer


	
   
}