package recycle;

import java.sql.Connection;
import java.sql.SQLException;
import java.sql.Statement;
import java.sql.Timestamp;
import java.text.ParseException;
import java.util.ArrayList;
import java.util.Date;

import dao.DAO;
import dao.Item;

public class Model1 extends MultiItemModel {

	Model1(Connection conn){
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
					{1,  6},
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
			updateCache();
		} catch (SQLException e) {
			e.printStackTrace();
		}
	}
	
	@Override
	String getSQL() {
		return "Select * from КОМПЛЕКТУЮЩИЕ join СПЕЦИФИКАЦИЯ using (КОМПЛ_ID)"; 
	}

	@Override
	protected void storeRecord(Item[] items) throws SQLException {
		if(items[1].getVal(9) != null) {//items[1].getVal(9) -  это Компл ID в записи из  таблицы комплектующих
			daos[1].storeNew2(items[1]);
				return;
		}
		Statement stmt = conn.createStatement();
		String tu = (items[0].getVal(3) == null ) ? " is null " : " = '" + items[0].getVal(3).toString().trim().toUpperCase() + "'";
		String mark = (items[0].getVal(4) == null ) ? " is null " : " = '" + items[0].getVal(4).toString().trim().toUpperCase() + "'";		
		String sqlFind = "SELECT * FROM КОМПЛЕКТУЮЩИЕ WHERE ТУ " + tu + " AND МАРКА " + mark;
		System.out.println(sqlFind);
		rs = stmt.executeQuery(sqlFind);
		if (rs.next()){
			items[1].setVal(9, rs.getObject(1));
			daos[1].storeNew2(items[1]);
			return;
			}
		else {
			throw new SQLException("Не могу найти соответсвие");
			
		}
			
		
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
	public void setValueAt(Object aValue, int rowIndex, int columnIndex) {
	    int daoNum = columnsMap[columnIndex][0];
		int fieldNum = getFieldNum(columnIndex);
		if(daoNum!= 1 )  return; //редактируемое поле не отностится к специцификации	
		if (fieldNum == 0) {
			announce("Не могу изменить СПЕЦ_ID");
			return;}	
		super.setValueAt(aValue, rowIndex, columnIndex);
		
	}



}
