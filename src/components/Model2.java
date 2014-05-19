package components;

import java.awt.Component;
import java.math.BigDecimal;
import java.sql.Connection;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;

import javax.swing.JDialog;
import javax.swing.JOptionPane;

import dao.DAO;
import dao.Item;

//*Модель для ввода требований
public class Model2 extends MultyItemsModel {
	
	public Model2(Connection conn){
		try {
			this.conn = conn;		
			daos = new DAO [3];
			daos[0] = new DAO(conn, "ТРЕБОВАНИЯ", "ТРЕБ_ID");
			daos[1] = new DAO(conn, "СПЕЦИФИКАЦИЯ", "СПЕЦ_ID");
			daos[2] = new DAO(conn, "КОМПЛЕКТУЮЩИЕ", "КОМПЛ_ID");
			
			columnsMap = new int[][]{
					
				//	{0,  0},
					{0,  1},
					{0,  3}, // Ном_Заявки
					{0,  4},
					{0,  5},
					{0,  6},
					{0,  7},
					{0,  2}, // спецID
					
				//	{1,  0},
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
					
					{2,  1},
					{2,  2},
					{2,  3},
					{2,  4},
					{2,  5},
					{2,  6},
					{2,  7},
					{2,  8}, 				

			};
		} catch (SQLException e) {
			e.printStackTrace();
		}
	}
	
	@Override
	protected String getSQL() {
		return "select  *  from  КОМПЛЕКТУЮЩИЕ JOIN СПЕЦИФИКАЦИЯ USING (КОМПЛ_ID)  left join Требования USING (СПЕЦ_ID)"; 
	}

	@Override
	void deleteFromDB(Item[] items) throws SQLException {
		daos[0].delete(items[0]);
	}

	@Override
	public boolean isCellEditable(int rowIndex, int columnIndex) {
		// TODO Auto-generated method stub
		return true;
	}
	@Override
	void insertIntoDB(Item[] items) throws SQLException {
			daos[0].storeNew2(items[0]);
		}
		

	@Override
	void updateInDB(Item[] items) throws SQLException {
		Object trb_id = items[0].getId();
		Object trb_name = items[0].getVal(1);
		if (trb_id !=null){
			if (trb_name !=null)
				daos[0].store(items[0]); 
			else
				daos[0].delete(items[0]);
		} else{
			if (trb_name !=null)
				daos[0].storeNew2(items[0]);			
		}
			
		
		daos[0].store(items[0]); 
		
	}
	
	/**Проверяет, есть ли в таблице заявок нужная запись, если нет - предлагает создать*/
	public void find(int rowIndex){
		Item[] items = getRow(rowIndex);	
		
		if(items[0].getVal(3) == null) //items[0].getVal(3)-  это номер заявки в таблице требований
			return;
		
		String sqlCheck = "SELECT Count(*) FROM ЗАЯВКИ WHERE НОМ_ЗАЯВКИ = '" + items[0].getVal(3) +"'";
		String sqlFind = "INSERT INTO ЗАЯВКИ ( НОМ_ЗАЯВКИ ) VALUES ( "+ items[0].getVal(3) + ")";
		
		System.out.println(sqlFind);
		try{
			Statement stmt = conn.createStatement();
			ResultSet rs = stmt.executeQuery(sqlCheck);
			rs.next();
			if (rs.getInt(1) == 1)
				return; // Такая заявка уже есть
			
			int result = JOptionPane.showConfirmDialog((Component) null, "Создать заявку с номером " + items[0].getVal(3) + "?",
					"", JOptionPane.YES_NO_OPTION);
			if (result == 0)
				stmt.executeUpdate(sqlFind);
			
			stmt.close();
		} catch (Exception e){e.printStackTrace();}
		
	}


}
