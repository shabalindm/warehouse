package components;

import gui.MainFrame;

import java.awt.BorderLayout;
import java.awt.FlowLayout;
import java.awt.Toolkit;
import java.awt.datatransfer.Clipboard;
import java.awt.datatransfer.DataFlavor;
import java.awt.datatransfer.Transferable;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.event.AdjustmentEvent;
import java.awt.event.AdjustmentListener;
import java.awt.event.KeyEvent;
import java.awt.event.KeyListener;
import java.text.DateFormat;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

import javax.swing.Icon;
import javax.swing.JButton;
import javax.swing.JFrame;
import javax.swing.JLabel;
import javax.swing.JPanel;
import javax.swing.JScrollPane;
import javax.swing.JSplitPane;
import javax.swing.JTable;
import javax.swing.JTextArea;
import javax.swing.ScrollPaneConstants;
import javax.swing.event.ChangeEvent;
import javax.swing.event.ListSelectionEvent;
import javax.swing.event.TableColumnModelEvent;
import javax.swing.event.TableColumnModelListener;
import javax.swing.event.TableModelEvent;
import javax.swing.event.TableModelListener;
import javax.swing.table.TableModel;

public  class TableEditPanel<T> extends JPanel {
	
	AbstractItemsTableModel<T> model;
	JTable table;
	JPanel btnPanel;

	JButton insertBtn; 
	JButton deleteBtn;
	JButton unDeleteBtn;
	JButton writeBtn;
	JButton clearBtn;
	JButton whereBtn;	
	private final boolean COMMITED = true;
	
	private StateListener stateListener;	
	/**   Устанавливает слушателя состояния соединения (commited)*/
	public void setStateListener(StateListener stateListener){
		this.stateListener = stateListener;
	}
	
/**   Передает слушателю  (если он установлен) сообщение*/
	protected void setState(boolean state){
		if (stateListener != null)
			stateListener.setState(state);		
	}

	/**Конструктор*/ 
	 public TableEditPanel(AbstractItemsTableModel<T> model) {
		this.model = model;
		table = new EJTable(model);
		table.setAutoCreateRowSorter(true);
		table.setCellSelectionEnabled(true);	
		table.setAutoResizeMode(JTable.AUTO_RESIZE_OFF);
		JScrollPane scrollPane = new JScrollPane(table);
		scrollPane.setVerticalScrollBarPolicy(JScrollPane.VERTICAL_SCROLLBAR_AS_NEEDED);
		scrollPane.setHorizontalScrollBarPolicy(JScrollPane.HORIZONTAL_SCROLLBAR_AS_NEEDED);
		
		JPanel btnPanel = new JPanel();
		initBtnPanel(btnPanel);			
		
		setLayout(new BorderLayout());
		add(btnPanel, BorderLayout.NORTH);
		add(scrollPane, BorderLayout.CENTER);
	}

	 
	 /**
		 * @param btnPanel 
		 * 
		 */
	 protected void initBtnPanel(JPanel btnPanel) {
		 insertBtn  = makeButton("Вставить", null, new InsertBtnListener());
		 insertBtn.setToolTipText("Нажми Shift, чтобы вставить новую строку ПЕРЕД выделенной строкой");
		 deleteBtn = makeButton("Удалить", null, new DeleteBtnListener());
		 unDeleteBtn = makeButton("Вернуть", null, new UnDeleteBtnListener()) ;
		 writeBtn = makeButton("Записать", null, new WriteBtnListener()) ;
		 clearBtn = makeButton("Обновить", null, new ClearBtnListener()) ;
		 whereBtn = makeButton("Условия", null, new WhereBtnListener());
		 btnPanel.setLayout(new FlowLayout(FlowLayout.LEFT));

		 btnPanel.add(insertBtn); 
		 btnPanel.add(deleteBtn);
		 btnPanel.add(unDeleteBtn);
		 btnPanel.add(writeBtn);
		 btnPanel.add(clearBtn);
		 btnPanel.add(whereBtn);
	 }
	 
	
	/** Слушатель кнопки удаления строк*/
	class DeleteBtnListener implements ActionListener{

		@Override
		public void actionPerformed(ActionEvent e) {
			int[] rowsToDelete = table.getSelectedRows();
			int currentRow = table.getSelectedRow();			
			int count = table.getSelectedRowCount();
			for (int i = 0; i <count; i++){	
				if(model.isNewRow(currentRow)){					
					model.deleteRow(currentRow);
					System.out.println( " Удаляю " + currentRow + "(новая строчка)");
				}else{
					model.deleteRow(currentRow);
					currentRow ++ ;
					System.out.println( " Удаляю " + currentRow + "(старая строчка)");
				}
					
			}
			model.fireTableRowsDeleted(rowsToDelete[0], rowsToDelete[rowsToDelete.length-1]);
		}}
	
	/** Слушатель кнопки отмены удаления строк*/
	class UnDeleteBtnListener implements ActionListener{

		@Override
		public void actionPerformed(ActionEvent e) {
			for (int rowIndex : table.getSelectedRows())			
				model.unDeleteRow(rowIndex);		
		}}
	
	/** Слушатель кнопки записи измененной таблицы в базу */
	public class WriteBtnListener implements ActionListener {

		@Override
		public void actionPerformed(ActionEvent e) {
			int errorRow = model.writeToDB();
			if (errorRow != -1){
				table.setRowSelectionInterval(errorRow, errorRow);
			} else {
				setState(!COMMITED);
		}

		}

	}
	
	/** Слушатель кнопки вставки строки*/
	class InsertBtnListener implements ActionListener{
		@Override
		public void actionPerformed(ActionEvent e) {
			int row = table.getSelectedRow();
			System.out.println(e.getModifiers());
			if(e.getModifiers() == 17) //зажата клавиша shift
				row--;
			model.addRow(row);
			table.setRowSelectionInterval(row+1, row+1);
		}
	}
	
	/** Слушатель кнопки очистки изменений строки*/
	class ClearBtnListener implements ActionListener{
		@Override
		public void actionPerformed(ActionEvent e) {			
			model.updateData();
		}
	}

	/** Слушатель кнопки "Условия"*/
	 class WhereBtnListener implements ActionListener {
		
		@Override
		public void actionPerformed(ActionEvent e) {	
			 final JFrame dialog = new JFrame();
			 dialog.setLocationByPlatform(true);
			 dialog.setLayout(new BorderLayout());
			 final JTextArea input = new JTextArea(10, 50 );
			 dialog.add(input, BorderLayout.CENTER );
			 input.setText(model.whereCond);
			 input.selectAll();
			 input.setLineWrap(true);
			 JButton okBtn = makeButton("OK", null, new ActionListener(){

				@Override
				public void actionPerformed(ActionEvent e) {
					model.whereCond = input.getText();
					model.updateData();
					dialog.dispose();			
				}});
			 
			 JButton cancelBtn = makeButton("Отмена", null, new ActionListener(){

					@Override
					public void actionPerformed(ActionEvent e) {
						dialog.dispose();			
					}});
			 JPanel btnpanel = new JPanel();
			 btnpanel.setLayout(new FlowLayout());
			 btnpanel.add(okBtn);
			 btnpanel.add(cancelBtn);
			 dialog.add(btnpanel, BorderLayout.SOUTH);
			 String example = "<html>Введите условия \"where\" и \"order by\" для sql запроса. Пример: <br> "
			 		+ "WHERE [имя столбца]  &lt 10 <br> ORDER BY [имя столбца] </html>";
			 dialog.add(new JLabel(example), BorderLayout.NORTH);
			 
			 dialog.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
			 dialog.pack();
			 dialog.setAlwaysOnTop(true);
			 dialog.setVisible(true);
		}

	}

	/** Создает новую кнопку*/
	private JButton makeButton(String name, Icon icon,
			ActionListener actionListener) {
		JButton btn = new JButton(name, icon);
		btn.addActionListener(actionListener);
		
		return btn;
	}
		

	
	
	
	
	
	
	
	
	public void close(){
		model.close();
	}

}
