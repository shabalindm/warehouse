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
	private List<Item> cache  = new ArrayList<>();
	private String whereCond;
	private DAO dao;
	private MessageListener listener; 
	
	public ItemsTableModel(DAO dao,  String whereCond)  {
		this.dao = dao;
		this.whereCond = whereCond;	
	}
	
	public void setMessageListener(MessageListener listener){
		this.listener = listener;
	}
	
	private void announce(String msg){
		if (listener != null)
			listener.setText(msg);			
	}
	
	public void updateCache()  {
			Statement stmt = null;
			ResultSet rs = null;
			String sql = "select * from " + dao.getTableName()  + whereCond;
			try{			
				stmt = dao.getConnection().createStatement();
				rs = stmt.executeQuery(sql);
				// очищаем кэш
				cache.clear();
				//выбираем данные из результирующего набора в кеш
				while (rs.next()){
					Item item = new Item(null);
					item.pollFieldsFromResultSet(rs, dao.getColumnNames());
					cache.add(item);
				}	
				announce(null); //операция успешна
			} catch (SQLException e) {
				// TODO Auto-generated catch block
				announce(e.getMessage());
				e.printStackTrace();
			}		
		
		finally{ fireTableDataChanged();
			try {stmt.close();
		} catch (SQLException e) {	
			e.printStackTrace();
		}}
	}
	

	@Override
	public String getColumnName(int column) {
		// TODO Auto-generated method stub
		return dao.getColumnNames()[column];
	}


	@Override
	public int getRowCount() {
		return cache.size();
	}

	@Override
	public int getColumnCount() {
		return dao.getColumnNames().length;
	}

	@Override
	public Object getValueAt(int rowIndex, int columnIndex) {
		Item item = cache.get(rowIndex);
		return item.getVal(columnIndex);
	}
	
	/** Создает новый item и записывает его в базу. Внимание - просто записывает, значение в кеше автоматически не появляется*/
	public boolean addRow(String[] record) {
		Item item = new Item(null);
		item.setSize(record.length);
		for (int i = 0; i<record.length; i++ )
			item.setVal(i, record[i]);
		
		// добавляем в базу
		try {
			dao.storeNew(item);
			announce(null);
			return true;
		} catch (SQLException e) {
			announce(e.getMessage());
			e.printStackTrace();
		}
		return false;		 
	}

	@Override
	public void setValueAt(Object aValue, int rowIndex, int columnIndex) {
		Item item = cache.get(rowIndex); 
		try{				
			if (columnIndex == 0){
				 dao.changeID(item.getId(), aValue);
				 // Сохраняем item с обновленным ID
				 item.setId(aValue);
				 announce(null);
			}
			else{
				item.setVal(columnIndex, aValue);			
				dao.store(item);
				announce(null);
			}
		} catch (SQLException e){	
			announce(e.getMessage());
			e.printStackTrace();
		}	
		finally {
			try {
				dao.refresh(item);
			} catch (SQLException e) {
				e.printStackTrace();
			}
			//fireTableCellUpdated(rowIndex, columnIndex);
		}
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
				list.add(item); // после удаления добавляем в список успешно удаленных из БД
				announce(null);
			}
		} catch (SQLException e) {
			announce(e.getMessage());
			e.printStackTrace();
		}
		finally{
			cache.removeAll(list);
		}
	}
}
