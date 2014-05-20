package components;

import java.math.BigDecimal;
import java.sql.Connection;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;

import dao.DAO;
import dao.Item;

public class Model1 extends MultyItemsModel {

	
	public Model1(Connection conn){
		try {
			this.conn = conn;		
			daos = new DAO [2];
			daos[0] = new DAO(conn, "КОМПЛЕКТУЮЩИЕ", "КОМПЛ_ID");
			daos[1] = new DAO(conn, "СПЕЦИФИКАЦИЯ", "СПЕЦ_ID");
			columnsMap = new int[][]{
					
					{1,  0},
					{1,  1},
					{1,  2},
					{1,  3},
					{1,  4},
					{1,  5},
					{1,  6},
					{1,  7},
					{1,  8},
					{1,  9},
					{1,  10},
					{1,  11},
					
					{0,  1},
					{0,  2},
					{0,  3},
					{0,  4},
					{0,  5},
					{0,  6},
					{0,  7},
					{0,  8},

			};
		} catch (SQLException e) {
			e.printStackTrace();
		}
	}
	
	@Override
	protected String getSQL() {
		return "Select * from КОМПЛЕКТУЮЩИЕ join СПЕЦИФИКАЦИЯ using (КОМПЛ_ID)"; 
	}

	@Override
	void deleteFromDB(Item[] items) throws SQLException {
		daos[1].delete(items[1]);
	}

	@Override
	public boolean isCellEditable(int rowIndex, int columnIndex) {
		// TODO Auto-generated method stub
		return true;
	}
	@Override
	void insertIntoDB(Item[] items) throws SQLException {
			daos[1].storeNew2(items[1]);
		}
		

	@Override
	void updateInDB(Item[] items) throws SQLException {
		daos[1].store(items[1]);
		
	}
	
	/**Ищет в таблице коплектующих подходящую запись и обновляет поля, с ней связанные*/
	public void find(int rowIndex){
		Item[] items = getRow(rowIndex);	
		String sqlFind;
		if(items[1].getVal(9) instanceof BigDecimal) {//items[1].getVal(9) -  это Компл ID в записи из  таблицы комплектующих
			 sqlFind = "SELECT * FROM КОМПЛЕКТУЮЩИЕ WHERE КОМПЛ_ID = " + items[1].getVal(9);
		} else{
			String tu = (items[0].getVal(3) == null ) ? " is null " : " = '" + items[0].getVal(3).toString().trim().toUpperCase() + "'";
			String mark = (items[0].getVal(4) == null ) ? " is null " : " = '" + items[0].getVal(4).toString().trim().toUpperCase() + "'";		
			sqlFind = "SELECT * FROM КОМПЛЕКТУЮЩИЕ WHERE ТУ " + tu + " AND МАРКА " + mark;
		}
		System.out.println(sqlFind);
		try{
			Statement stmt = conn.createStatement();
			ResultSet rs = stmt.executeQuery(sqlFind);
			if (rs.next()){
				items[1].setVal(9, rs.getObject("КОМПЛ_ID"));
				items[0].pollFieldsFromResultSet(rs, daos[0].getColumnNames());
			}
			fireTableRowsUpdated(rowIndex, rowIndex);
			stmt.close();
		} catch (Exception e){e.printStackTrace();}
		
	}


}
