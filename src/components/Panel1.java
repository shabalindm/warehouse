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

/** Таблица для ввода и редактирования спецификаций*/
public class Panel1 extends TableEditPanel<Item[]> {

	private class SearchBtnListener implements ActionListener {

		@Override
		public void actionPerformed(ActionEvent e) {
			for (int veiwRow : table.getSelectedRows()){
				((Model1)model).find(table.convertRowIndexToModel(veiwRow));
			}

		}
	}

	public Panel1(Connection conn ) {
		super(new Model1(conn));
		for (int i = 0; i< model.getColumnCount(); i++){
			if(((Model1) model).index1(i) != 0)
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

class Model1 extends SimpleJoinModel {

	
	public Model1(Connection conn){
		try {
			this.conn = conn;		
			daos = new DAO [2];
			daos[0] = new DAO(conn, "СПЕЦИФИКАЦИЯ", "СПЕЦ_ID");
			daos[1] = new DAO(conn, "КОМПЛЕКТУЮЩИЕ", "КОМПЛ_ID");
			
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
					{0,  9},
					{0,  10},
					{0,  11},
					
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
		return "Select * from КОМПЛЕКТУЮЩИЕ join СПЕЦИФИКАЦИЯ using (КОМПЛ_ID)"; 
	}

	
	/**Ищет в таблице коплектующих подходящую запись и обновляет поля, с ней связанные*/
	public void find(int rowIndex){
		Item[] items = getRow(rowIndex);	
		String sqlFind;
		if(items[0].getVal(9) instanceof BigDecimal) {//items[0].getVal(9) -  это Компл ID в записи из  таблицы комплектующих
			 sqlFind = "SELECT * FROM КОМПЛЕКТУЮЩИЕ WHERE КОМПЛ_ID = " + items[0].getVal(9);
		} else{
			String tu = (items[1].getVal(3) == null ) ? " is null " : " = '" + items[1].getVal(3).toString().trim().toUpperCase() + "'";
			String mark = (items[1].getVal(4) == null ) ? " is null " : " = '" + items[1].getVal(4).toString().trim().toUpperCase() + "'";		
			sqlFind = "SELECT * FROM КОМПЛЕКТУЮЩИЕ WHERE ТУ " + tu + " AND МАРКА " + mark;
		}
		System.out.println(sqlFind);
		try{
			Statement stmt = conn.createStatement();
			ResultSet rs = stmt.executeQuery(sqlFind);
			if (rs.next()){
				items[0].setVal(9, rs.getObject("КОМПЛ_ID"));
				items[1].pollFieldsFromResultSet(rs, daos[1].getColumnNames());
			}
			fireTableRowsUpdated(rowIndex, rowIndex);
			stmt.close();
		} catch (Exception e){e.printStackTrace();}
		
	}


}

