package components;

import java.math.BigDecimal;
import java.sql.Connection;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;

import dao.DAO;
import dao.Item;


/**������ � ������������, ������������� �� ����� ������*/
public class DPanel2 extends   DetaledPanel<Item[]>  {

	public DPanel2(Connection conn, Item mainItem,  Object fKValue, String fKName) {
		super(new DModel2(conn, fKValue,  fKName), mainItem);
		for (int i = 0; i< model.getColumnCount(); i++){
			if(((DModel2) model).index1(i) != 0)
				table.getColumnModel().getColumn(i).setCellRenderer(new GreyCellRenderer());
				
		}
	}

	

}



class DModel2 extends SimpleJoinModel {
	private Object fKValue;
	private String fKName;
	private int fKPos;
	
	public DModel2(Connection conn, Object fKValue, String fKName) {
		this(conn);
		this.fKValue =  fKValue;
		
		this.fKName =fKName;
		fKPos = daos[0].getFieldIndex(fKName);
		if ( fKName instanceof String)
			setWhereCond(" where " + fKName + " = '" + fKValue + "'");
		else
			setWhereCond(" where " + fKName + " = " + fKValue);
		
	}

	


	
	public DModel2(Connection conn){
		try {
			this.conn = conn;		
			daos = new DAO [2];
			daos[1] = new DAO(conn, "�������������", "�����_ID");
			daos[0] = new DAO(conn, "�����_���", "�����_���_ID");
			columnsMap = new int[][]{
					
					{0,  0},
					//{0,  1},
					{0,  2}, // �����_id
					{0,  3},
					{0,  4},
							
					
					{1,  1},
					{1,  2},
					{1,  3},
					{1,  4},
					{1,  5},
					{1,  6},
					{1,  7},
					{1,  8},

			};
		} catch (SQLException e) {
			e.printStackTrace();
		}
	}
	
	@Override
	protected String getSQL() {
		return "Select * from �����_��� join ������������� using (�����_ID) join  ������  using (���_������)"; 
	}

	
	
	/**���� � ������� ������������ ���������� ������ � ��������� ����, � ��� ���������*/
	public void find(int rowIndex){
		Item[] items = getRow(rowIndex);	
		String sqlFind;
		if(items[0].getVal(2) instanceof BigDecimal) {//items[0].getVal(2) -  ��� ����� ID � ������ ��  ������� �������������
			 sqlFind = "SELECT * FROM ������������� WHERE �����_ID = " + items[0].getVal(2);
		} else{
			String tu = (items[1].getVal(3) == null ) ? " is null " : " = '" + items[1].getVal(3).toString().trim().toUpperCase() + "'";
			String mark = (items[1].getVal(4) == null ) ? " is null " : " = '" + items[1].getVal(4).toString().trim().toUpperCase() + "'";		
			sqlFind = "SELECT * FROM ������������� WHERE �� " + tu + " AND ����� " + mark;
		}
		System.out.println(sqlFind);
		try{
			Statement stmt = conn.createStatement();
			ResultSet rs = stmt.executeQuery(sqlFind);
			if (rs.next()){
				items[0].setVal(2, rs.getObject("�����_ID"));
				items[1].pollFieldsFromResultSet(rs, daos[2].getColumnNames());
			}
			fireTableRowsUpdated(rowIndex, rowIndex);
			stmt.close();
		} catch (Exception e){e.printStackTrace();}
		
	}

	@Override
	protected Item[] getEmptyRow() {
		 
		Item[] items = super.getEmptyRow();
	    items[0].setVal(fKPos, fKValue);
		return items;
	}

}


