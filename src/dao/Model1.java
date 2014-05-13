package dao;

import java.sql.Connection;
import java.sql.SQLException;
import java.util.ArrayList;

public class Model1 extends MultiItemModel {

	Model1(Connection conn){
		try {
		this.conn = conn;		
		daos = new DAO [2];
		daos[0] = new DAO(conn, " ŒÃœÀ≈ “”ﬁŸ»≈", " ŒÃœÀ_ID");
		daos[1] = new DAO(conn, "—œ≈÷»‘» ¿÷»ﬂ", "—œ≈÷_ID");
		columnsMap = new int[][]{
				{0,  0}, 
				{0,  1},
				{0,  2},
				{0,  3},
				{0,  4},
				{0,  5},
				{0,  6},
				{0,  7},
				{0,  8},
				{1,  0},
				{1,  1},
				{1,  2},
				{1,  3},
				{1,  4},
				{1,  6},
				{1,  6},
				{1,  7},
				{1,  8},
				{1,  10},
				{1,  11},
				
		};
	} catch (SQLException e) {
		// TODO Auto-generated catch block
		e.printStackTrace();
	}}
	@Override
	protected void storeRecord(Item[] items) throws SQLException {
		// TODO Auto-generated method stub

	}

	
	@Override
	void deleteFromDBAndAddToList(Item[] item, ArrayList<Item[]> list)
			throws SQLException {
		// TODO Auto-generated method stub
		
	}

}
