package dao;

import java.sql.SQLException;
import java.sql.Timestamp;
import java.text.ParseException;
import java.util.Date;
import java.util.List;

import dao.AbstractItemsTableModel;

public abstract class MultiItemModel extends AbstractItemsTableModel<Item[]> {
	protected DAO [] daos;
	protected int [][] columnsMap; 	// 
	
	protected DAO getDAO (int columnNum){
		return daos[columnsMap[columnNum][0]];		
	}
	protected Item getItem (int rowIndex, int columnIndex){
		Item[] items = cache.get(rowIndex);
		if(items == null){
			pollToCache(rowIndex);	
			items = cache.get(rowIndex);
		}
		return items[columnsMap[columnIndex][0]];		
	}
	
	protected int getFieldNum(int columnNum){
		return columnsMap[columnNum][1];		
	}

	@Override
	public int getColumnCount() {
		return columnsMap.length;
	}

	@Override
	public Object getValueAt(int rowIndex, int columnIndex) {
		Object val = getItem(rowIndex, columnIndex).getVal(getFieldNum(columnIndex));
		if (java.util.Date.class.isAssignableFrom(getDAO(columnIndex).getColumnClasses()[getFieldNum(columnIndex)]) && val != null )
			val = f.format(val);			
		return val;
	}

	
	@Override
	void pollToCache(int row) {
		try{
			rs.absolute(row+1);
			Item [] items = new Item[daos.length ];
			for(int k = 0; k < daos.length; k++ ){
				items[k] = daos[k].getEmptyItem();
				items[k].pollFieldsFromResultSet(rs, daos[k].getColumnNames());
			}
			cache.put(row, items);
			
		}catch (SQLException e){e.printStackTrace();}

	}

	@Override
	public void addRows(List<String[]> records) {
		try {
			while (records.size() > 0){
				Item [] items = new Item[daos.length ];
				for (int i = 0; i < daos.length; i++)
					items[i] = daos[i].getEmptyItem();
				String[] record = records.get(0);
				for (int i = 0; i<record.length; i++ ){	
					int daoNum = columnsMap[i][0];
					int fieldNum = columnsMap[i][1];
					if (java.util.Date.class.isAssignableFrom(daos[daoNum].getColumnClasses()[fieldNum]) && 
							record[i] != null && !record[i].matches("\\s*") ){
						try {
							items[daoNum].setVal(i, new Timestamp((((Date)f.parseObject(record[i])).getTime())));
						} catch (ParseException e) {
							announce("Ќеправильный формат даты");
							return; 
						}
					}		
					else{ 				
						items[daoNum].setVal(fieldNum, record[i]);}
				}

				// добавл€ем в базу
				storeRecord(items);
				// удал€ем из коллекции
				records.remove(record);
			}
			announce(null);
			} catch (SQLException e) {
				announce(e.getMessage());
				e.printStackTrace(); 
			}
		
		}

	/**Cохран€ет массив вновь образованный массив из Item по своей логике
	 * @param items 
	 * 
	 */
	protected abstract void storeRecord(Item[] items) throws SQLException;
		


	@Override
	String getSQL() {
		// TODO Auto-generated method stub
		return null;
	}

	@Override
	void deleteFromDB(Item[] item) throws SQLException {
		// TODO Auto-generated method stub
		
	}

	@Override
	public String getColumnName(int column) {
		int daoNum = columnsMap[column][0];
		int fieldNum = columnsMap[column][1];
		return  daos[daoNum].getColumnNames()[fieldNum];
	}

	public void setValueAt(Object aValue, int rowIndex, int columnIndex) {
	    int daoNum = columnsMap[columnIndex][0];
		int fieldNum = getFieldNum(columnIndex);
		DAO dao = getDAO(columnIndex);
		Item item = getItem(rowIndex, columnIndex);		
		
		if (java.util.Date.class.isAssignableFrom(dao.getColumnClasses()[fieldNum])&& aValue != null )
			try {
				aValue = new Timestamp((((Date)f.parseObject((String) aValue)).getTime()));
			} catch (ParseException e1) {
				announce("Ќеправильный формат даты");
				return;
			}
		try{				
			item.setVal(fieldNum, aValue);			
			dao.store(item);
			announce(null);
			fireTableCellUpdated(rowIndex, columnIndex);

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
		}
	}
	
	@Override
	public void close() {
		super.close();
		for (DAO dao:daos)
			dao.close();
	}

}
