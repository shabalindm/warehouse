package dao;

import java.sql.Connection;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;
import java.util.ArrayList;
import java.util.List;

import javax.swing.table.AbstractTableModel;

public class ItemsTableModel extends AbstractTableModel{
	private Connection conn;
	private List<Item> cache  = new ArrayList<>();
	
	private String whereCond;
	String[] columnNames;
	private String tableName;
	private String tablePK;
	private DAO dao;
	

	public ItemsTableModel(String tableName, String tablePK,  String whereCond,  Connection conn) throws SQLException {
		this.whereCond = whereCond;
		this.tableName = tableName;
		this.conn = conn;
		this.tablePK = tablePK;
		Statement stmt = null;
		ResultSet rs = null;
		dao = new DAO(conn, tableName, tablePK);
		String sql = "select * from " + tableName  + whereCond;
		try{			
			stmt = conn.createStatement();
			rs = stmt.executeQuery(sql);
			// выбираем имена стобцов
			columnNames = new String [rs.getMetaData().getColumnCount()];
			for(int i = 0; i < columnNames.length; i++)
				columnNames[i] = rs.getMetaData().getColumnName(i+1);	
			
			//выбираем данные из результирующего набора в кеш
			while (rs.next()){
				Item item = new Item();
				dao.pollFieldsFromResultSet(item, rs, null);
				cache.add(item);
			}						
		}
		finally{stmt.close();}		
		
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
	
	public void addRow(String[] record) throws SQLException{
		Item item = new Item();
		item.setId(record[0]);
		item.setOtherCols(new Item[record.length-1]);
		for (int i = 1; i<record.length; i++ )
			item.setField(i, record[i]);
		// добавл€ем в базу
		dao.store(item);
		// после чего добавл€ем и в кеш
		cache.add(item); 
	}

	@Override
	public void setValueAt(Object oValue, int rowIndex, int columnIndex) {
		Item item = cache.get(rowIndex); 
		Item oldValue = null;
		String oldID = null;
		try{
			String  sValue = (oValue != null)? oValue.toString():null;			
			if (columnIndex ==0){
				oldID = item.getId(); 
				item.setId(sValue);			 
			}
			else{
				oldValue = item.getField(columnIndex);
				item.setField(columnIndex, sValue);
			}
			dao.store(item);
			
		} catch (SQLException e){
			if (columnIndex ==0)
				item.setId(oldID);	
			else
				item.setField(columnIndex, oldValue);
			e.printStackTrace();
		}		
	}
	
	@Override
	public boolean isCellEditable(int rowIndex, int columnIndex) {			
		return true;
	}	
	
	public void deleteRows(int [] deleted) throws SQLException{		
		ArrayList<Item> list = new ArrayList<>();
		try{
			for(int rowNum : deleted ){
				Item item = cache.get(rowNum);
				dao.delete(item);
				list.add(item); // после удалени€ добавл€ем в список успешно удаленных из Ѕƒ
			}
		}
		finally{
			cache.removeAll(list);
		}
		

	}
	
	

}
