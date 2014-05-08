package dao;


import java.awt.Color;
import java.awt.Component;
import java.awt.BorderLayout;
import java.awt.Container;
import java.awt.Dimension;
import java.awt.FlowLayout;
import java.awt.GridLayout;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.event.AdjustmentEvent;
import java.awt.event.AdjustmentListener;
import java.awt.event.ComponentEvent;
import java.awt.event.ComponentListener;
import java.awt.event.KeyEvent;
import java.awt.event.KeyListener;
import java.awt.event.WindowAdapter;
import java.awt.event.WindowEvent;
import java.sql.Connection;
import java.sql.SQLException;

import javax.swing.Icon;
import javax.swing.JButton;
import javax.swing.JFrame;
import javax.swing.JLabel;
import javax.swing.JOptionPane;
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
import javax.swing.table.TableCellRenderer;
import javax.swing.table.TableColumn;

public class TableViewer extends JFrame {
	private Connection conn;
	boolean commited = true;
	
	private String whereCond = "";
		
	JTable tableView;	
	JTable newRows;
	ItemsTableModel tableViewModel;
	ListOfArraysModel newRowsModel;
	
	JScrollPane upper = new JScrollPane();
	JScrollPane  down = new JScrollPane() ;
	
	JButton commitBtn;
	JButton rollbackBtn;
	JButton insertBtn; 
	JButton deleteBtn;
	InfoTextArea information = new InfoTextArea ();
	private DAO dao;
	
	/**Слушатель, который следит за изменением порядка и ширины стобцов верхней таблицы и повторяет эти операции для нижней таблицы.
	 * В результате таблицы ведут себя как одна с разделителем */
	 class TableColumnWidhtListener implements TableColumnModelListener {
		 private int [] order = new int[tableViewModel.getColumnCount()];
		{for(int i = 0; i < order.length; i++)
					order[i] = i;    	}
		 
		@Override
		public void columnAdded(TableColumnModelEvent e) {	}
		@Override
		public void columnRemoved(TableColumnModelEvent e) {			
		}
		@Override
		public void columnMoved(TableColumnModelEvent e) {
			if(e.getFromIndex() != e.getToIndex() ){				
				newRows.moveColumn(e.getFromIndex(), e.getToIndex());
				int temp = order[e.getFromIndex()];
				order[e.getFromIndex()]= order[e.getToIndex()];
				order[e.getToIndex()] = temp;
			}	 
			 	 
		}
		@Override
		public void columnMarginChanged(ChangeEvent e) { 
			
			if ( tableView.getTableHeader().getResizingColumn() !=null){
				int colIngex = tableView.getTableHeader().getResizingColumn().getModelIndex();
				int widht = tableView.getTableHeader().getResizingColumn().getWidth();
				System.out.println(colIngex); System.out.println(order[colIngex]);
				newRows.getColumnModel().getColumn(order[colIngex]).setPreferredWidth(widht);
				}
		}
		@Override
		public void columnSelectionChanged(ListSelectionEvent e) {	}
	}

	 /**Конструктор*/
	public TableViewer(DAO dao){	
		conn = dao.getConnection();
		this.dao = dao;
		
		initTables(dao);
		
		commitBtn = makeButton("Сохранить", null, new SaveBtnListener()  );
		
		rollbackBtn = makeButton("Откатить", null, new rollBackListener()  );	
		
		deleteBtn = makeButton("Удалить", null, new deleteBtmListener());
		insertBtn = makeButton("Вставить", null, new insertBtmListener());
		
		information.setEditable(false);
		information.setLineWrap(true);
		information.setText(tableViewModel.sqlInfoMSG);	
		information.setRows(3);
		
		/* Расстановка компонентов во фрейме*/
		
		upper.setHorizontalScrollBarPolicy(ScrollPaneConstants.HORIZONTAL_SCROLLBAR_NEVER);		
		
		down.setHorizontalScrollBarPolicy(ScrollPaneConstants.HORIZONTAL_SCROLLBAR_AS_NEEDED );
		
		down.getHorizontalScrollBar().addAdjustmentListener( new AdjustmentListener() {
			
			@Override
			public void adjustmentValueChanged(AdjustmentEvent e) {
				upper.getHorizontalScrollBar().setValue(down.getHorizontalScrollBar().getValue());
				
			}
		});
		
		JSplitPane splitPane = new JSplitPane(JSplitPane.VERTICAL_SPLIT, upper, down);
		splitPane.setContinuousLayout(true);
		splitPane.setOneTouchExpandable(true);
		
		
		JScrollPane infopane =  new JScrollPane(information);
		//infopane.setHorizontalScrollBarPolicy(ScrollPaneConstants.VERTICAL_SCROLLBAR_ALWAYS);
		infopane.setHorizontalScrollBarPolicy(ScrollPaneConstants.HORIZONTAL_SCROLLBAR_AS_NEEDED);
			
		JPanel btmPanel = new JPanel();
		btmPanel.setLayout(new GridLayout(2,2));
		btmPanel.add(commitBtn);
		btmPanel.add(rollbackBtn);
		btmPanel.add(insertBtn);
		btmPanel.add(deleteBtn);
		
		JPanel controlPanel = new JPanel();
		infopane.setMaximumSize(new Dimension(10000, 40));
		controlPanel.setLayout( new BorderLayout());
		controlPanel.add(btmPanel, BorderLayout.WEST);
		controlPanel.add(infopane, BorderLayout.CENTER);
		
		Container panel = getContentPane();
		panel.setLayout(new BorderLayout());
		panel.add(controlPanel, BorderLayout.NORTH);
		panel.add(splitPane, BorderLayout.CENTER);
		setSize(400, 600);
	//	pack();
		addWindowListener(new CloseOperationListener());
		setDefaultCloseOperation(DO_NOTHING_ON_CLOSE);
		
		splitPane.setDividerLocation(400);
		
		
	}

	

	/** Слушатель кнопки удаления строк*/
	class deleteBtmListener implements ActionListener{

		@Override
		public void actionPerformed(ActionEvent e) {
			try{
				int[] deleted = tableView.getSelectedRows();
				tableViewModel.deleteRows(deleted);				
			}
			finally {
				information.setText(tableViewModel.sqlInfoMSG);
				tableViewModel.fireTableStructureChanged();
			}		
		}}
	
	/** Слушатель кнопки вставки строк*/
	class insertBtmListener implements ActionListener{

		@Override
		public void actionPerformed(ActionEvent e) {
			for (int i = 0; i < newRowsModel.getRowCount(); i++){
				try{
					for( String s : newRowsModel.getRowAt(i)){ 
						if ( s != null && !s.matches("\\s+")){ // идем по записи и ищем в ней нулевое или состоящее из одних пробелов значение
							tableViewModel.addRow(newRowsModel.removeRow(i));
							break;
						}
					}												
				}
				finally {	
					information.setText(tableViewModel.sqlInfoMSG);
					tableViewModel.updateCache();
					tableViewModel.fireTableStructureChanged();
				}
				
			}		
		}}
	
	/** Слушатель, проверяющий, зафиксированы ли изменения в базе и выводящий диалог с предложеним зафиксировать*/
	class CloseOperationListener extends WindowAdapter{
		public void windowClosing(WindowEvent e){
			if (!commited){ 
				int result = JOptionPane.showConfirmDialog((Component) null, "Сохранить изменененные данные?",
						"alert", JOptionPane.YES_NO_CANCEL_OPTION);
				if (result == 0){
					try {	conn.commit();	}
					catch (SQLException e1) {e1.printStackTrace();}
					TableViewer.this.dispose();
				}
				else if (result == 1){
					try {	conn.rollback();}
					catch (SQLException e1) {e1.printStackTrace();}
					TableViewer.this.dispose();
				}
				else //result == 2
					return;
					 	
			}
			TableViewer.this.dispose();

		}
	}
	
	/** Слушатель кнопки "Сохранить"*/
	class SaveBtnListener implements ActionListener  {			
		@Override
		public void actionPerformed(ActionEvent e) {
			try {
				conn.commit();
				commited = true;
			} catch (SQLException e1) {
				e1.printStackTrace();
				information.setText(e1.getMessage());
			}	}}
	
	/** Слушатель кнопки "Откатить"*/
	class rollBackListener implements ActionListener {			
		@Override
		public void actionPerformed(ActionEvent e) {
			try {
				conn.rollback();
				commited = true;
			} catch (SQLException e1) {
				e1.printStackTrace();
				information.setText(e1.getMessage());
			} 	
		initTables(dao);}}
	
	/**Считывает данные из базы, создает на их основе новые таблицы и и размещает их во фрейме*/	
	private void initTables(DAO dao) {		
			tableViewModel = new ItemsTableModel(dao, whereCond);
		
		
		tableView = new JTable(tableViewModel);		
		tableView.getColumnModel().addColumnModelListener( new TableColumnWidhtListener() );
		tableView.setAutoCreateRowSorter(true);
		tableView.setCellSelectionEnabled(true);
	
		tableView.setAutoResizeMode(JTable.AUTO_RESIZE_OFF);
		tableViewModel.addTableModelListener( new TableModelListener(){
			@Override
			public void tableChanged(TableModelEvent e) {
				commited = false;		
				information.setText(tableViewModel.sqlInfoMSG);
			}});
		
		
		
		newRowsModel = new ListOfArraysModel(tableViewModel.getColumnCount());
		newRows = new EJTable(newRowsModel);
		newRows.setTableHeader(null);
		newRows.setAutoResizeMode(JTable.AUTO_RESIZE_OFF);
		newRows.setCellSelectionEnabled(true);
		
		upper.setViewportView(tableView);
		down.setViewportView(newRows);
		information.setText(tableViewModel.sqlInfoMSG);
		
		
	}

	class EJTable extends JTable implements KeyListener {
		private ListOfArraysModel model;
		
		public EJTable(ListOfArraysModel model){
			super(model);
			this.model = model;		
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
					System.out.println(startRow + " "+ startRow);
					model.insertFromBuffer(startRow, startCol);
					repaint();
					}	 
				}
			else if (e.getKeyCode () == KeyEvent.VK_DELETE){
				for(int row : getSelectedRows ())
					for ( int column: getSelectedColumns())
						model.setValueAt(null, row, column);
				repaint();
			}
		}

		@Override
		public void keyReleased(KeyEvent e) {}
	}
	
	/** Создает новую кнопку*/
	private JButton makeButton(String name, Icon icon,
			ActionListener actionListener) {
		JButton btn = new JButton(name, icon);
		btn.addActionListener(actionListener);
		
		return btn;
	}
}

