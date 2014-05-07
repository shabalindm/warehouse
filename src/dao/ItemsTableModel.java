package dao;

import java.sql.Connection;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;
import java.util.ArrayList;
import java.util.List;

import javax.swing.ScrollPaneConstants;
import javax.swing.table.AbstractTableModel;

public class ItemsTableModel extends AbstractTableModel{
	private Connection conn;
	private List<Item> cache  = new ArrayList<>();
	public String sqlInfoMSG = " ";
	
	private String whereCond;
	String[] columnNames;
	private String tableName;
	private String tablePK;
	private DAO dao;
	private Class[] columnClasses;
	
	

	public ItemsTableModel(DAO dao,  String whereCond)  {
		this.dao = dao;
		this.whereCond = whereCond;
		tableName = dao.getTableName();
		columnNames = dao.getColumnNames();
		columnClasses = dao.getColumnClassNames();
		conn = dao.getConnection();
		
		updateCache();	
		
	}


	public void updateCache()  {
			Statement stmt = null;
			ResultSet rs = null;
			String sql = "select * from " + tableName  + whereCond;
			try{			
				stmt = conn.createStatement();
				rs = stmt.executeQuery(sql);
				// ������� ���
				cache.clear();
				//�������� ������ �� ��������������� ������ � ���
				while (rs.next()){
					Item item = new Item();
					dao.pollFieldsFromResultSet(item, rs, null);
					cache.add(item);
				}						
			} catch (SQLException e) {
				// TODO Auto-generated catch block
				sqlInfoMSG = e.getMessage();
				e.printStackTrace();
			}
			
		
		
		finally{try {stmt.close();
		} catch (SQLException e) {
			sqlInfoMSG = e.getMessage();
			e.printStackTrace();
		}}
	}
	

	@Override
	public String getColumnName(int column) {
		// TODO Auto-generated method stub
		return columnNames[column];
	}


	@Override
	public int getRowCount() {
		return cache.size();
	}

	@Override
	public int getColumnCount() {
		return columnNames.length;
	}

	@Override
	public Object getValueAt(int rowIndex, int columnIndex) {
		Item item = cache.get(rowIndex);
		if (columnIndex ==0)
			return item.getId();
		return item.getField(columnIndex);
	}
	
	/** ������� ����� item � ���������� ��� � ����. �������� - ������ ����������, �������� � ���� ������������� �� ����������*/
	public void addRow(String[] record) {
		Item item = new Item();
		item.setId(record[0]);
		item.setOtherCols(new Item[record.length-1]);
		for (int i = 1; i<record.length; i++ )
			item.setField(i, record[i]);
		
		// ��������� � ����
		try {
			dao.storeNew(item);
		} catch (SQLException e) {
			sqlInfoMSG = e.getMessage();
			e.printStackTrace();
		}		 
	}

	@Override
	public void setValueAt(Object aValue, int rowIndex, int columnIndex) {
		Item item = cache.get(rowIndex); 
		try{	
			
			if (columnIndex ==0){
				 //item.setId(aValue);
				 dao.changeID(item.getId(), aValue);
				 // ��������� item � ����������� ID
				 item.setId(aValue);
			}
			else{
				item.setField(columnIndex, aValue);			
				dao.store(item);
			}
		} catch (SQLException e){	
			sqlInfoMSG = e.getMessage();
			e.printStackTrace();
		}	
		finally {
			fireTableCellUpdated(rowIndex, columnIndex);
			try {
			dao.refresh(item);
		} catch (SQLException e) {
			e.printStackTrace();
		}}
	}
	
	@Override
	public boolean isCellEditable(int rowIndex, int columnIndex) {			
		return true;
	}	
	
	public void deleteRows(int [] deleted) {		
		ArrayList<Item> list = new ArrayList<>();
		try{
			for(int rowNum : deleted ){
				Item item = cache.get(rowNum);
				dao.delete(item);
				list.add(item); // ����� �������� ��������� � ������ ������� ��������� �� ��
			}
		} catch (SQLException e) {
			sqlInfoMSG = e.getMessage();
			e.printStackTrace();
		}
		finally{
			cache.removeAll(list);
		}
	}
}
