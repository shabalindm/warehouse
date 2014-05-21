package components;

import java.sql.SQLException;
import java.util.Arrays;

import dao.DAO;
import dao.Item;
/** ћодель таблицы, в качестве строчек имеюща€ значени€ Item. —в€зана с базой через значени€ пол€ RowID
 * ¬ основе модели лежит DAO - он определ€ет все параметры*/
public class ItemsModel extends AbstractItemsTableModel<Item>{

	protected DAO dao;
		
	
	public ItemsModel(DAO dao)  {
		this.dao = dao;
		conn  = dao.getConnection();
		
	}


	public DAO getDAO() {
		return dao;
	}


	protected String getSQL(){		
		String sql = "select ";
		for (String column : dao.getColumnNames())
			sql += column + ", ";
		sql += "ROWID" + " from " + dao.getTableName() ;
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
		if(aValue instanceof String && ((String)aValue).matches("\\s*"))
			aValue = null;
		
		Object oldValue = getValueAt(rowIndex, columnIndex);			
		if(aValue == null && aValue == oldValue ||  aValue != null && aValue.equals(oldValue) ){
		// считаем что значение на самом деле не изменилось 
		} else
				markCellModified(rowIndex, columnIndex); 
		
		item.setVal(columnIndex, aValue);
		
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
	protected 	Item getEmptyRow() {
		// TODO Auto-generated method stub
		return dao.getEmptyItem();
	}

	@Override
	void deleteFromDB(Item item) throws SQLException {
		item.deleteByRowId();		
	}

	@Override
	void insertIntoDB(Item item) throws SQLException {
		item.storeNew2();
		
	}


	@Override
	void updateInDB(Item item) throws SQLException {
		item.storebyRowId();
		
	}


	public void close(){		
		dao.close();
		super.close();
	}

	
	
}
