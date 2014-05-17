package components;

import java.sql.SQLException;

import dao.DAO;
import dao.Item;
/** Модель таблицы, в качестве строчек имеющая значения Item. Связана с базой через значения поля RowID
 * В основе модели лежит DAO - он определяет все параметры*/
public class ItemsModel extends AbstractItemsTableModel<Item>{

	private DAO dao;
		
	
	
	public ItemsModel(DAO dao)  {
		this.dao = dao;
		conn  = dao.getConnection();
		updateData();
	}


	public DAO getDAO() {
		return dao;
	}


	protected String getSQL(){		
		String sql = "select ";
		for (String column : dao.getColumnNames())
			sql += column + ", ";
		sql += "ROWID" + " from " + dao.getTableName() + " " + whereCond;
		System.out.println(sql);
		return( sql);		 
	}


	Item getRowFromResultSet(int row) {
		try{
			rs.absolute(row+1);
			Item item = getEmptyRow();
			item.pollFieldsFromRSRowID(rs, dao.getColumnNames(), "ROWID");
			return item;
		}		
		catch (SQLException e){e.printStackTrace();}
		return null;		
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
		Item item = getRow(rowIndex);
		return  item.getVal(columnIndex);		
	}
	
	
	@Override
	public void setValueAt(Object aValue, int rowIndex, int columnIndex) {
		Item item = getRow(rowIndex);				
		item.setVal(columnIndex, aValue);
		markCellModified(rowIndex, columnIndex);
	}
	
	@Override
	public boolean isCellEditable(int rowIndex, int columnIndex) {			
		return true;
	}	
	
	


	@Override
	public Class<?> getColumnClass(int columnIndex) {
		// TODO Auto-generated method stub
		return dao.getColumnClasses()[columnIndex];
	}


	@Override
	Item getEmptyRow() {
		// TODO Auto-generated method stub
		return dao.getEmptyItem();
	}

	@Override
	void deleteFromDB(Item item) throws SQLException {
		dao.deleteByRowId(item);		
	}

	@Override
	void insertIntoDB(Item item) throws SQLException {
		dao.storeNew2(item);
		
	}


	@Override
	void updateInDB(Item item) throws SQLException {
		dao.storeNew2(item);
		
	}


	public void close(){		
		dao.close();
		super.close();
	}

	
	
}
