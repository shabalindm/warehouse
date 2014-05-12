package dao;

import java.sql.Connection;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;
import java.sql.Timestamp;
import java.text.DateFormat;
import java.text.ParseException;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;

import javax.swing.ScrollPaneConstants;
import javax.swing.table.AbstractTableModel;

public class ItemsTableModel extends AbstractTableModel{
	private List<Item> cache  = new ArrayList<>();
	private int rowCount;
	private String whereCond = "";
	private DAO dao;
	private MessageListener listener;
	DateFormat f;
	
	public ItemsTableModel(DAO dao)  {
		this.dao = dao;
	}
	
	public void setMessageListener(MessageListener listener){
		this.listener = listener;
	}
	
	private void announce(String msg){
		if (listener != null)
			listener.setText(msg);		
	}
	
	public DAO getDAO() {
		return dao;
	}
	
		
	public String getWhereCond() {
		return whereCond;
	}

	public void setWhereCond(String whereCond) {
		this.whereCond = whereCond;
	}

	public void updateCache()  {
			Statement stmt = null;
			ResultSet rs = null;
			String sql = "select ";
			for (String column : dao.getColumnNames())
				sql += column + ", ";
			sql += "ROWID" + " from " + dao.getTableName() + " " + whereCond;
			System.out.println(sql);
			try{			
				stmt = dao.getConnection().createStatement();
				rs = stmt.executeQuery(sql);
				// очищаем кэш
				cache.clear();
				//выбираем данные из результирующего набора в кеш
				while (rs.next()){
					Item item = dao.getEmptyItem();
					item.pollFieldsFromRSRowID(rs, dao.getColumnNames(), "ROWID");
					cache.add(item);
				}	
			} catch (SQLException e) {
				// TODO Auto-generated catch block
				announce(e.getMessage());
				e.printStackTrace();
			}		

		finally{ fireTableDataChanged();
			try {rs.close();
				stmt.close();
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
		 Object val = cache.get(rowIndex).getVal(columnIndex);
		if (java.util.Date.class.isAssignableFrom(dao.getColumnClasses()[columnIndex]) && val != null )
			val = f.format(val);			
		return val;
	}
	
	/** Создает новый item и записывает его в базу. Внимание - просто записывает, значение в кеше автоматически не появляется*/
	public void addRows(List<String[]> records) {
		try {
			while (records.size() > 0){
				Item item = dao.getEmptyItem();
				String[] record = records.get(0);
				for (int i = 0; i<record.length; i++ ){					
					if (java.util.Date.class.isAssignableFrom(dao.getColumnClasses()[i]) && 
							record[i] != null && !record[i].matches("\\s*") ){
						try {
							item.setVal(i, new Timestamp((((Date)f.parseObject(record[i])).getTime())));
						} catch (ParseException e) {
							announce("Неправильный формат даты");
							return; 
						}
					}		
					else{ 				
						item.setVal(i, record[i]);}
				}

				// добавляем в базу
				dao.storeNew2(item);
				// удаляем из коллекции
				records.remove(record);
			}
			announce(null);
			} catch (SQLException e) {
				announce(e.getMessage());
				e.printStackTrace(); 
			}
		
		}

	@Override
	public void setValueAt(Object aValue, int rowIndex, int columnIndex) {
		Item item = cache.get(rowIndex); 
		if (java.util.Date.class.isAssignableFrom(dao.getColumnClasses()[columnIndex])&& aValue != null )
			try {
				aValue = new Timestamp((((Date)f.parseObject((String) aValue)).getTime()));
			} catch (ParseException e1) {
					announce("Неправильный формат даты");
					return;
			}
		try{				
			item.setVal(columnIndex, aValue);			
			dao.storebyRowId(item);
			announce(null);
			fireTableCellUpdated(rowIndex, columnIndex);

		} catch (SQLException e){
			
			announce(e.getMessage());
			e.printStackTrace();
		}	
		finally {
			try {
				dao.refreshByRowID(item);
			} catch (SQLException e) {
				e.printStackTrace();
			}
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
				dao.deleteByRowId(item);
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
