package components;

import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.math.BigDecimal;
import java.sql.Connection;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;

import javax.swing.JButton;
import javax.swing.JPanel;

import dao.DAO;
import dao.Item;

/** Таблица для ввода и редактирования детализации заявок*/
public class Panel6 extends TableEditPanel<Item[]> {

	private class SearchBtnListener implements ActionListener {

		@Override
		public void actionPerformed(ActionEvent e) {
			for (int veiwRow : table.getSelectedRows()){
				((Model6)model).find(table.convertRowIndexToModel(veiwRow));
			}

		}
	}

	public Panel6(Connection conn ) {
		super(new Model6(conn));
		for (int i = 0; i< model.getColumnCount(); i++){
			if(((Model6) model).index1(i) != 2)
				table.getColumnModel().getColumn(i).setCellRenderer(new GreyCellRenderer());
				
		}
	}

	@Override
	protected void setupControlPanel(JPanel btnPanel) {
		super.setupControlPanel(btnPanel);
		JButton searchBtn = makeButton("Найти", null, new SearchBtnListener());
		searchBtn.setToolTipText("Выдели строчки, для которых нужно произвести поиск");
		btnPanel.add(searchBtn);
	}

	
}

class Model6 extends MultyItemsModel {

	
	public Model6(Connection conn){
		try {
			this.conn = conn;		
			daos = new DAO [3];
			daos[0] = new DAO(conn, "КОМПЛЕКТУЮЩИЕ", "КОМПЛ_ID");
			daos[1] = new DAO(conn, "ЗАЯВКИ", "НОМ_ЗАЯВКИ");
			daos[2] = new DAO(conn, "ДЕТАЛ_ЗАЯ", "ДЕТАЛ_ЗАЯ_ID");
			columnsMap = new int[][]{
					
					{2,  0},
					{2,  1},
					
					{1,  1}, // Дата заявки
					{1,  6}, // Статус
					
					{2,  2}, // КОМПЛ_id
					{2,  3},
					{2,  4},
							
					
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
		return "Select * from ДЕТАЛ_ЗАЯ join КОМПЛЕКТУЮЩИЕ using (КОМПЛ_ID) join  ЗАЯВКИ  using (ном_заявки)"; 
	}

	@Override
	void deleteFromDB(Item[] items) throws SQLException {
		items[2].delete();
	}

	@Override
	public boolean isCellEditable(int rowIndex, int columnIndex) {
		if(columnIndex == 0 )
			return	false;
		return true;
	}
	@Override
	void insertIntoDB(Item[] items) throws SQLException {
			items[2].storeNew2();
		}
		

	@Override
	void updateInDB(Item[] items) throws SQLException {
		items[2].store();
		
	}
	
	/**Ищет в таблице коплектующих подходящую запись и обновляет поля, с ней связанные*/
	public void find(int rowIndex){
		Item[] items = getRow(rowIndex);	
		String sqlFind;
		if(items[2].getVal(2) instanceof BigDecimal) {//items[1].getVal(2) -  это Компл ID в записи из  таблицы ДЕТАЛ_ЗАЯ
			 sqlFind = "SELECT * FROM КОМПЛЕКТУЮЩИЕ WHERE КОМПЛ_ID = " + items[2].getVal(2);
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
				items[2].setVal(2, rs.getObject("КОМПЛ_ID"));
				items[0].pollFieldsFromResultSet(rs, daos[0].getColumnNames());
			}
			fireTableRowsUpdated(rowIndex, rowIndex);
			stmt.close();
		} catch (Exception e){e.printStackTrace();}
		
	}


}

