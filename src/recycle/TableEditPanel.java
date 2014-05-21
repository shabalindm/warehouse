package recycle;


import gui.MainFrame;

import java.awt.BorderLayout;
import java.awt.FlowLayout;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.event.AdjustmentEvent;
import java.awt.event.AdjustmentListener;
import java.awt.event.KeyEvent;
import java.awt.event.KeyListener;
import java.sql.SQLException;
import java.text.DateFormat;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
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

import recycle.AbstractTableEditPanel.EJTable;
import recycle.AbstractTableEditPanel.TableColumnWidhtListener;
import recycle.AbstractTableEditPanel.deleteBtmListener;
import recycle.AbstractTableEditPanel.insertBtmListener;
import recycle.AbstractTableEditPanel.refreshBtnistener;
import recycle.AbstractTableEditPanel.whereBtnListener;
import dao.DAO;

public class TableEditPanel extends JPanel {
	
	public AbstractItemsTableModel tableViewModel;
	
	private JTable tableView;	
	private JTable newRows;
	
	private ListOfArraysModel newRowsModel;

	private JButton insertBtn; 
	private JButton deleteBtn;
	private JButton refreshBtn;
	private JButton setWhereBtn;	
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
				newRows.getColumnModel().getColumn(order[colIngex]).setPreferredWidth(widht);
				}
		}
		@Override
		public void columnSelectionChanged(ListSelectionEvent e) {	}
	}

	/**Конструктор*/ 
	 public TableEditPanel(AbstractItemsTableModel tableViewModel) {
		this.tableViewModel = tableViewModel;
		
		initTables();		
		
		JSplitPane splitPane = new JSplitPane(JSplitPane.VERTICAL_SPLIT);
		initSplitPane(splitPane);
		
		JPanel btnPanel = new JPanel();
		initBtnPanel(btnPanel);			
		
		setupPanel(btnPanel, splitPane);
	}

	/**
	 * @param btnPanel
	 * @param splitPane
	 */
	private void setupPanel(JPanel btnPanel, JSplitPane splitPane) {
		setLayout(new BorderLayout());
		add(btnPanel, BorderLayout.NORTH);
		add(splitPane, BorderLayout.CENTER);
	}

	/**
	 * @param splitPane
	 */
	private void initSplitPane(JSplitPane splitPane) {
		final JScrollPane upper = new JScrollPane(tableView);
		final JScrollPane  down = new JScrollPane(newRows) ;
		
		upper.setHorizontalScrollBarPolicy(ScrollPaneConstants.HORIZONTAL_SCROLLBAR_NEVER);		
		
		down.setHorizontalScrollBarPolicy(ScrollPaneConstants.HORIZONTAL_SCROLLBAR_AS_NEEDED );
		
		down.getHorizontalScrollBar().addAdjustmentListener( new AdjustmentListener() {
			
			@Override
			public void adjustmentValueChanged(AdjustmentEvent e) {
				upper.getHorizontalScrollBar().setValue(down.getHorizontalScrollBar().getValue());
				
			}
		});
		splitPane.setBottomComponent(down);
		splitPane.setLeftComponent(upper);		
		
		splitPane.setContinuousLayout(true);
		splitPane.setOneTouchExpandable(true);
		splitPane.setDividerLocation(400);	
		
	}

	/**Создает кнопки и размещает их на панели
	 * @param btnPanel Панель, на которой эти кнопки размещаются. В принципе, ее можно нарисовать как угодно;
	 */
	protected void initBtnPanel(JPanel btnPanel) {
		deleteBtn = makeButton("Удалить", null, new deleteBtmListener());
		insertBtn = makeButton("Вставить", null, new insertBtmListener());
		refreshBtn = makeButton("Обновить", null, new refreshBtnistener());
		setWhereBtn = makeButton("Условия", null, new whereBtnListener());
		btnPanel.setLayout(new FlowLayout(FlowLayout.LEFT));
		btnPanel.add(insertBtn);
		btnPanel.add(deleteBtn);
		btnPanel.add(refreshBtn);
		btnPanel.add(setWhereBtn);
	}

	/**Создает таблицы таблицы JTable. Модель верхней, привязанной к базе, берется готовая. Mодель нижней - создается */	
	private void initTables() {			
		tableView = new JTable(tableViewModel);		
		tableView.getColumnModel().addColumnModelListener( new TableColumnWidhtListener() );
		tableView.setAutoCreateRowSorter(true);
		tableView.setCellSelectionEnabled(true);	
		tableView.setAutoResizeMode(JTable.AUTO_RESIZE_OFF);	
		
		newRowsModel = new ListOfArraysModel(tableViewModel.getColumnCount());
		newRows = new EJTable(newRowsModel);
		newRows.setTableHeader(null);
		newRows.setAutoResizeMode(JTable.AUTO_RESIZE_OFF);
		newRows.setCellSelectionEnabled(true);	
	}

	

	/** Слушатель кнопки удаления строк*/
	class deleteBtmListener implements ActionListener{

		@Override
		public void actionPerformed(ActionEvent e) {
		//	MainFrame.commited = false;
			try{
				int[] deleted = tableView.getSelectedRows();
				tableViewModel.deleteRows(deleted);				
			}
			finally {
				tableViewModel.fireTableStructureChanged();
			}		
		}}
	
	/** Слушатель кнопки вставки строк*/
	class insertBtmListener implements ActionListener{
		@Override
		public void actionPerformed(ActionEvent e) {
		//	MainFrame.commited = false;
			List <String []> insertedRows = new ArrayList<>();
			for (int i = 0; i < newRowsModel.getRowCount(); i++){
				for( String s : newRowsModel.getRowAt(i)){ 
					if ( s != null && !s.matches("\\s*")){ // идем по записи и ищем в ней нулевое или состоящее из одних пробелов значение
						if(insertedRows.add(newRowsModel.getRowAt(i)));
						break;
					}//if
				}//for
			}// for
		 tableViewModel.addRows(insertedRows);
		 tableViewModel.updateCache();
		 tableViewModel.fireTableDataChanged();
		 if (insertedRows.size() == 0)
			 newRowsModel.reset();
		 else 
			 for (String [] row : insertedRows )
				 newRowsModel.getTable().retainAll(insertedRows);
		 newRowsModel.fireTableDataChanged();
		}//actionPerformed
	}//deleteBtmListener



	/** Слушатель кнопки "Условия"*/
	 class whereBtnListener implements ActionListener {
		
		@Override
		public void actionPerformed(ActionEvent e) {	
			 final JFrame dialog = new JFrame();
			 dialog.setLocationByPlatform(true);
			 dialog.setLayout(new BorderLayout());
			 final JTextArea input = new JTextArea(10, 50 );
			 dialog.add(input, BorderLayout.CENTER );
			 input.setText(tableViewModel.whereCond);
			 input.setLineWrap(true);
			 JButton okBtn = makeButton("OK", null, new ActionListener(){

				@Override
				public void actionPerformed(ActionEvent e) {
					tableViewModel.whereCond = input.getText();
					tableViewModel.updateCache();
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
			 		+ "WHERE "  + dao.getColumnNames()[0] + " &lt 10 <br> ORDER BY " + dao.getColumnNames()[0] + "</html>";
			 dialog.add(new JLabel(example), BorderLayout.NORTH);
			 
			 dialog.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
			 dialog.pack();
			 dialog.setAlwaysOnTop(true);
			 dialog.setVisible(true);
		}

	}

	 /** Слушатель кнопки "Обновить" */
	 class refreshBtnistener implements ActionListener {

		@Override
		public void actionPerformed(ActionEvent e) {
			tableViewModel.updateCache();

		}

	}

	
	
/** Это таблица, которая умеет вставлять данные и системного буфера при нажатии ctrl+V и удалять из по кнопке delete*/
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

	public void close() {
		tableViewModel.close();
		
	}


}
