package dao;

import java.sql.SQLException;
import java.sql.Timestamp;
import java.text.ParseException;
import java.util.Date;
import java.util.List;

import dao.AbstractItemsTableModel;

public abstract class MultiItemModel extends AbstractItemsTableModel<Item[]> {
	DAO [] daos;
	public int [][] columnsMap; 	// 

	
	

	@Override
	public int getColumnCount() {
		return columnsMap.length;
	}

	@Override
	public Object getValueAt(int rowIndex, int columnIndex) {
		while(rowIndex >= cache.size()){
			//	System.out.println("Запрошен индекс " + rowIndex + " Размер кеша " + cache.size() + "всего " + rowCount);
			readMore();				
		}
		
		int daoNum = columnsMap[columnIndex][0];
		int fieldNum = columnsMap[columnIndex][1];
		Object val = cache.get(rowIndex)[daoNum].getVal(fieldNum);
		if (java.util.Date.class.isAssignableFrom(daos[daoNum].getColumnClasses()[fieldNum]) && val != null )
			val = f.format(val);			
		return val;
	}

	@Override
	String getSQL() {
		// TODO Auto-generated method stub
		return "Select * from КОМПЛЕКТУЮЩИЕ join СПЕЦИФИКАЦИЯ using КОМПЛ_ID";
		
	}

	@Override
	void readMore() {
		try{
			int i = 0;
		while (i < 10 && rs.next() ){
			Item [] items = new Item[daos.length ];
			for(int k = 0; k < daos.length; k++ ){
				items[k] = daos[k].getEmptyItem();
				items[k].pollFieldsFromResultSet(rs, daos[k].getColumnNames());
			}
			cache.add(items);
			i++;
		}
		//System.out.println("Прочитано " +(i));
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
							announce("Неправильный формат даты");
							return; 
						}
					}		
					else{ 				
						items[daoNum].setVal(i, record[i]);}
				}

				// добавляем в базу
				storeRecord(items);
				// удаляем из коллекции
				records.remove(record);
			}
			announce(null);
			} catch (SQLException e) {
				announce(e.getMessage());
				e.printStackTrace(); 
			}
		
		}

	/**Cохраняет массив вновь образованный массив из Item по своей логике
	 * @param items 
	 * 
	 */
	protected abstract void storeRecord(Item[] items) throws SQLException;
		


	@Override
	public void close() {
		try {
			rs.close();
		} catch (SQLException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
		for (DAO dao:daos)
			dao.close();
	}

}
