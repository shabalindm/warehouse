package recycle;


import java.awt.BorderLayout;
import java.awt.Component;
import java.awt.Container;
import java.awt.Dimension;
import java.awt.FlowLayout;
import java.awt.GridLayout;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.event.AdjustmentEvent;
import java.awt.event.AdjustmentListener;
import java.awt.event.KeyEvent;
import java.awt.event.KeyListener;
import java.awt.event.WindowAdapter;
import java.awt.event.WindowEvent;
import java.sql.SQLException;
import java.text.DateFormat;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.List;

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

import dao.DAO;

public class TableViewer extends JFrame {
	DateFormat f = new SimpleDateFormat("MM/dd/yy");

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
	private JButton refreshBtn;
	private JButton setWhereBtn;
	
	InfoTextArea information;
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
	public TableViewer(DAO dao){	
		this.dao = dao;		
				
		commitBtn = makeButton("Сохранить", null, new SaveBtnListener() );
		
		rollbackBtn = makeButton("Откатить", null, new rollBackListener());	
		
		deleteBtn = makeButton("Удалить", null, new deleteBtmListener());
		insertBtn = makeButton("Вставить", null, new insertBtmListener());
		refreshBtn = makeButton("Обновить", null, new refreshBtnistener());
		setWhereBtn = makeButton("Условия", null, new whereBtnListener());
		
		information = new InfoTextArea ();
		information.setEditable(false);
		information.setLineWrap(true);
		information.setRows(3);
		
		initTables(dao);
		
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
		btmPanel.setLayout(new GridLayout(2,3));
		btmPanel.add(commitBtn);
		btmPanel.add(rollbackBtn);
		btmPanel.add(insertBtn);
		btmPanel.add(deleteBtn);
		btmPanel.add(refreshBtn);
		btmPanel.add(setWhereBtn);
		
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
				tableViewModel.fireTableStructureChanged();
			}		
		}}
	
	/** Слушатель кнопки вставки строк*/
	class insertBtmListener implements ActionListener{

		@Override
		public void actionPerformed(ActionEvent e) {
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

	/** Слушатель, проверяющий, зафиксированы ли изменения в базе и выводящий диалог с предложеним зафиксировать*/
	class CloseOperationListener extends WindowAdapter{
		public void windowClosing(WindowEvent e){
			if (!commited){ 
				int result = JOptionPane.showConfirmDialog((Component) null, "Сохранить измененные данные?",
						"alert", JOptionPane.YES_NO_CANCEL_OPTION);
				if (result == 0){
					try {	dao.getConnection().commit();	}
					catch (SQLException e1) {e1.printStackTrace();}
					TableViewer.this.dispose();
				}
				else if (result == 1){
					try {	dao.getConnection().rollback();}
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
				dao.getConnection().commit();
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
				dao.getConnection().rollback();
				commited = true;
			} catch (SQLException e1) {
				e1.printStackTrace();
				information.setText(e1.getMessage());
			} 	
		initTables(dao);}}
	
	/** Слушатель кнопки "Условия"*/
	 class whereBtnListener implements ActionListener {
		
		@Override
		public void actionPerformed(ActionEvent e) {	
			 final JFrame dialog = new JFrame();
			 dialog.setLocationByPlatform(true);
			 dialog.setLayout(new BorderLayout());
			 final JTextArea input = new JTextArea(10, 50 );
			 dialog.add(input, BorderLayout.CENTER );
			 input.setText(whereCond);
			 input.setLineWrap(true);
			 JButton okBtn = makeButton("OK", null, new ActionListener(){

				@Override
				public void actionPerformed(ActionEvent e) {
					whereCond = input.getText();
					tableViewModel.setWhereCond(whereCond);
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
			 		+ "WHERE "  + dao.getTablePK() + " &lt 10 <br> ORDER BY " + dao.getTablePK() + "</html>";
			 dialog.add(new JLabel(example), BorderLayout.NORTH);
			 
			 dialog.setDefaultCloseOperation(EXIT_ON_CLOSE);
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

	
	/**Считывает данные из базы, создает на их основе новые таблицы и и размещает их во фрейме*/	
	@SuppressWarnings("unchecked")
	private void initTables(DAO dao) {		
		tableViewModel = new ItemsTableModel(dao);
		tableViewModel.setMessageListener(information);	
		tableViewModel.updateCache();
		
		tableView = new JTable(tableViewModel);		
		tableView.getColumnModel().addColumnModelListener( new TableColumnWidhtListener() );
		tableView.setAutoCreateRowSorter(true);
		tableView.setCellSelectionEnabled(true);
		tableViewModel.f = f;
//		for (int i = 0; i < tableViewModel.getColumnCount(); i++ ){
//			if (java.util.Date.class.isAssignableFrom(tableViewModel.getDAO().getColumnClasses()[i]) )
//				tableView.getColumnModel().getColumn(i).setCellRenderer(new DateRenderer(f));
//			}
		
		tableView.setAutoResizeMode(JTable.AUTO_RESIZE_OFF);
		tableViewModel.addTableModelListener( new TableModelListener(){
			@Override
			public void tableChanged(TableModelEvent e) {
				commited = false;
			}});		
		
		
		newRowsModel = new ListOfArraysModel(tableViewModel.getColumnCount());
		newRows = new EJTable(newRowsModel);
		newRows.setTableHeader(null);
		newRows.setAutoResizeMode(JTable.AUTO_RESIZE_OFF);
		newRows.setCellSelectionEnabled(true);
		
		upper.setViewportView(tableView);
		down.setViewportView(newRows);		
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
}

