package dao;


import java.awt.Component;
import java.awt.BorderLayout;
import java.awt.Container;
import java.awt.GridLayout;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.event.WindowAdapter;
import java.awt.event.WindowEvent;
import java.sql.Connection;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;

import javax.swing.Icon;
import javax.swing.JButton;
import javax.swing.JFrame;
import javax.swing.JOptionPane;
import javax.swing.JPanel;
import javax.swing.JScrollPane;
import javax.swing.JSplitPane;
import javax.swing.JTable;
import javax.swing.JTextArea;
import javax.swing.ScrollPaneConstants;
import javax.swing.event.TableModelEvent;
import javax.swing.event.TableModelListener;

public class TableViewer extends JFrame {
	Connection conn;
	boolean commited = true;
	
	private String whereCond = "";
	private String tableName;
	private String tablePK;
	
	JTable tableView;	
	JTable newRows;
	ItemsTableModel tableViewModel;
	ListOfArraysModel newRowsModel;
	
	JButton commitBtn;
	JButton rollbackBtn;
	JButton insertBtn; 
	JButton deleteBtn;
	JTextArea information;
	

	
	public TableViewer(String tableName, String tablePK, final Connection conn){
		this.conn = conn;
		this.tableName = tableName;
		this.tablePK = tablePK;
		
		try {
			tableViewModel = new ItemsTableModel(tableName, tablePK, whereCond, conn);
		} catch (SQLException e2) {
			e2.printStackTrace();
		}
		
		tableView = new JTable(tableViewModel);		
		
		tableViewModel.addTableModelListener( new TableModelListener(){
			@Override
			public void tableChanged(TableModelEvent e) {
				commited = false;				
			}});
		
		newRowsModel = new ListOfArraysModel(tableViewModel.getColumnCount());
		newRows = new JTable(newRowsModel);
		
		commitBtn = makeButton("Сохранить", null, new ActionListener() {			
			@Override
			public void actionPerformed(ActionEvent e) {
				try {
					conn.commit();
					commited = true;
				} catch (SQLException e1) {
					e1.printStackTrace();
					information.setText(e1.getMessage());
				}	}} );
		
		rollbackBtn = makeButton("Откатить", null, new ActionListener() {			
			@Override
			public void actionPerformed(ActionEvent e) {
				try {
					conn.rollback();
					commited = true;
				} catch (SQLException e1) {
					e1.printStackTrace();
					information.setText(e1.getMessage());
				} 	
				tableViewModel.fireTableStructureChanged();}} );	
		
		deleteBtn = makeButton("Удалить", null, new deleteBtmListener());
		insertBtn = makeButton("Вставить", null, new insertBtmListener());
		
		information = new JTextArea();
		information.setLineWrap(true);
		
		/* Расстановка компонентов во фрейме*/
		JScrollPane upper = new JScrollPane(tableView);
		upper.setHorizontalScrollBarPolicy(ScrollPaneConstants.HORIZONTAL_SCROLLBAR_AS_NEEDED );
		JScrollPane down = new JScrollPane(newRows);
		down.setHorizontalScrollBarPolicy(ScrollPaneConstants.HORIZONTAL_SCROLLBAR_AS_NEEDED );
		
		JSplitPane splitPane = new JSplitPane(JSplitPane.VERTICAL_SPLIT, upper, down);
		splitPane.setContinuousLayout(true);
		splitPane.setOneTouchExpandable(true);

		
		JScrollPane infopane =  new JScrollPane(information);
		upper.setHorizontalScrollBarPolicy(ScrollPaneConstants.HORIZONTAL_SCROLLBAR_NEVER );
		JPanel btmPanel = new JPanel();
		btmPanel.setLayout(new GridLayout(2,2));
		btmPanel.add(commitBtn);
		btmPanel.add(rollbackBtn);
		btmPanel.add(insertBtn);
		btmPanel.add(deleteBtn);
		
		JPanel controlPanel = new JPanel();
		controlPanel.setLayout( new BorderLayout());
		controlPanel.add(btmPanel, BorderLayout.EAST);
		controlPanel.add(infopane, BorderLayout.CENTER);
		
		Container panel = getContentPane();
		panel.setLayout(new BorderLayout());
		panel.add(controlPanel, BorderLayout.NORTH);
		panel.add(splitPane, BorderLayout.CENTER);
		pack();	
		addWindowListener(new CloseOperationListener());
		setDefaultCloseOperation(DO_NOTHING_ON_CLOSE);
	}


	private JButton makeButton(String name, Icon icon,
			ActionListener actionListener) {
		JButton btn = new JButton(name, icon);
		btn.addActionListener(actionListener);
		
		return btn;
	}
	
	class deleteBtmListener implements ActionListener{

		@Override
		public void actionPerformed(ActionEvent e) {
			try{
				int[] deleted = tableView.getSelectedRows();
				tableViewModel.deleteRows(deleted);
			}
			catch (SQLException exc){
				exc.printStackTrace();
				information.setText(exc.getMessage());									
			}
			finally {
				tableViewModel.fireTableStructureChanged();
			}		
		}}
	
	class insertBtmListener implements ActionListener{

		@Override
		public void actionPerformed(ActionEvent e) {
			for (int i = 0; i < newRowsModel.getRowCount(); i++){
				try{
					for( String s : newRowsModel.getRowAt(i)){ 
						if ( s != null && !s.matches("//s*")){ // идем по записи и ищем в ней нулевое или состоящее из одних пробелов значение
							tableViewModel.addRow(newRowsModel.getRowAt(i));
							break;
						}
					} 
				
				tableViewModel.addRow(newRowsModel.getRowAt(i));
				}
				catch (SQLException exc){
					exc.printStackTrace();
					information.setText(exc.getMessage());									
				}
				finally {
					tableViewModel.fireTableStructureChanged();
				}
				
			}		
		}}
	
	class CloseOperationListener extends WindowAdapter{
		public void windowClosing(WindowEvent e){
			if (!commited){ 
				int result = JOptionPane.showConfirmDialog((Component) null, "Сохранить изменения?",
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
	
}

