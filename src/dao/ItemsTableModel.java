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
	public String sqlInfoMSG = " ";	
	private String whereCond;
	private DAO dao;
	//ptivate 
	
	

	public ItemsTableModel(DAO dao,  String whereCond)  {
		this.dao = dao;
		this.whereCond = whereCond;
		updateCache();		
		
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
			} catch (SQLException e) {
				// TODO Auto-generated catch block
				sqlInfoMSG = e.getMessage();
				e.printStackTrace();
			}		
		
		finally{try {stmt.close();
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
	public void addRow(String[] record) {
		Item item = new Item(null);
		item.setSize(record.length);
		for (int i = 0; i<record.length; i++ )
			item.setVal(i, record[i]);
		
		// добавляем в базу
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
			if (columnIndex == 0){
				 dao.changeID(item.getId(), aValue);
				 // Сохраняем item с обновленным ID
				 item.setId(aValue);
			}
			else{
				item.setVal(columnIndex, aValue);			
				dao.store(item);
				
			}
		} catch (SQLException e){	
			sqlInfoMSG = e.getMessage();
			e.printStackTrace();
		}	
		finally {
			try {
				dao.refresh(item);
			} catch (SQLException e) {
				e.printStackTrace();
			}
			fireTableCellUpdated(rowIndex, columnIndex);
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
