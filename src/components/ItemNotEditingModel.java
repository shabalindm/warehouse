package components;

import java.sql.SQLException;

import dao.DAO;
import dao.Item;

public class ItemNotEditingModel extends ItemsModel {

	public ItemNotEditingModel(DAO dao) {
		super(dao);
		// TODO Auto-generated constructor stub
	}

	@Override
	protected String getSQL() {
		String sql = "select ";
		for (String column : dao.getColumnNames())
			sql += column + ", ";
		sql = sql.substring(0, sql.length() - 2 );
		sql += " from " + dao.getTableName() ;
		System.out.println(sql);
		return( sql);	
	}

	@Override
	Item getRowFromResultSet(int row) {
		try{
			rs.absolute(row+1);
			Item item = getEmptyRow();
			item.pollFieldsFromResultSet(rs, dao.getColumnNames());
			return item;
		}		
		catch (SQLException e){e.printStackTrace();}
		return null;
	}

	@Override
	public boolean isCellEditable(int rowIndex, int columnIndex) {	
		return false;
	}
	
	
	

	
}
