package dao;

import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;
import java.sql.Timestamp;
import java.text.ParseException;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;

public class ItemsTableModel extends AbstractItemsTableModel<Item>{

	public DAO dao;
	public ItemsTableModel(DAO dao)  {
		this.dao = dao;
		conn  = dao.getConnection();
	}
		
	
	public DAO getDAO() {
		return dao;
	}
	
		
	
	 String getSQL(){		
		String sql = "select ";
		for (String column : dao.getColumnNames())
			sql += column + ", ";
		sql += "ROWID" + " from " + dao.getTableName() + " " + whereCond;
		System.out.println(sql);
		return( sql);		 
	 }
	 

	void readMore() {
		try{
			int i = 0;
		while (i < 10 && rs.next() ){
			Item item = dao.getEmptyItem();
			item.pollFieldsFromRSRowID(rs, dao.getColumnNames(), "ROWID");
			cache.add(item);
			i++;			
		}
		//System.out.println("Прочитано " +(i));
		}catch (SQLException e){e.printStackTrace();}
		
		
	}

	@Override
	public String getColumnName(int column) {
		// TODO Auto-generated method stub
		return dao.getColumnNames()[column];
	}


	
	@Override
	public int getColumnCount() {
		return dao.getColumnNames().length;
	}

	@Override
	public Object getValueAt(int rowIndex, int columnIndex) {
		while(rowIndex >= cache.size()){
		//	System.out.println("Запрошен индекс " + rowIndex + " Размер кеша " + cache.size() + "всего " + rowCount);
			readMore();				
			}
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
	
	
	/**
	 * @param item
	 * @param list
	 * @throws SQLException
	 */
	protected void deleteFromDBAndAddToList(Item item, ArrayList<Item> list)
			throws SQLException {
		dao.deleteByRowId(item);
		list.add(item); // после удаления добавляем в список успешно удаленных из БД
	}
	
	
	public void close(){try {
		rs.close();
		dao.close();
	} catch (SQLException e) {
		e.printStackTrace();
	}}
}
