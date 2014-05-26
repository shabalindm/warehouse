package components;

import java.sql.SQLException;

import dao.Item;
/**��� ������ ������������ ����� �������� ������� ���� ������, �� � ��� ������� ��� � ���������� ����, �� ������� ��� ���������
 * ������������ ���������� Item[0] ��� ������ ������� ������*/
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
