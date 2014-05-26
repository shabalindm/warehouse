package components;

import java.sql.SQLException;

import dao.Item;
/**Эта модель представляет собой редактор таблицы базы данных, но к ней пришиты еще и справочные поля, на которые она ссылается
 * Использутеся соглашение Item[0] это всегда ведущая запись*/
public abstract class SimpleJoinModel extends MultyItemsModel {
	
	@Override
	void deleteFromDB(Item[] items) throws SQLException {
		items[0].delete();
	}
	
	@Override
	public boolean isCellEditable(int rowIndex, int columnIndex) {
		if(columnIndex == 0 )
			return	false;
		
		return true;
	}

	@Override
	void insertIntoDB(Item[] items) throws SQLException {
			items[0].storeNew2();
		}
		

	@Override
	void updateInDB(Item[] items) throws SQLException {
		items[0].store();
		
	}
}
