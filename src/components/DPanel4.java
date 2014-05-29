package components;

import gui.MainFrame;

import java.awt.BorderLayout;
import java.awt.FlowLayout;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.math.BigDecimal;
import java.sql.Connection;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;
import java.util.ArrayList;
import java.util.List;

import javax.swing.JButton;
import javax.swing.JPanel;




import dao.DAO;
import dao.Item;


/**Панель с детализацией накладных*/
public class DPanel4 extends   DetaledPanel<Item[]>  {

	private class SearchBtnListener implements ActionListener {

		@Override
		public void actionPerformed(ActionEvent e) {
			for (int veiwRow : table.getSelectedRows()){
				((DModel4)model).find(table.convertRowIndexToModel(veiwRow));
			}

		}
	}
	
	@Override
	protected void setupControlPanel(JPanel btnPanel) {
		JButton searchBtn = makeButton("Найти", null, new SearchBtnListener());
		searchBtn.setToolTipText("Выдели строчки, для которых нужно произвести поиск");
		btmPanel = new JPanel();		
		btmPanel.setLayout(new FlowLayout(FlowLayout.LEFT));
		btmPanel.add(insertBtn); 
		btmPanel.add(deleteBtn);
		btmPanel.add(unDeleteBtn);
		btmPanel.add(writeBtn);
		btmPanel.add(clearBtn);	
		btmPanel.add(searchBtn);	
		controlPanel.setLayout(new BorderLayout());
		controlPanel.add(btmPanel, BorderLayout.NORTH);	
		
	}

	public DPanel4(Connection conn, Item mainItem,  Object fKValue, String fKName) {
		super(new DModel4(conn, fKValue,  fKName), mainItem);
		for (int i = 0; i< model.getColumnCount(); i++){
			if(((DModel4) model).index1(i) != 0)
				table.getColumnModel().getColumn(i).setCellRenderer(new GreyCellRenderer());
				
		}
	}

}



class DModel4 extends SimpleJoinModel {
	private Object fKValue;
	private String fKName;
	private int fKPos;
	
	public DModel4(Connection conn, Object fKValue, String fKName) {
		this(conn);
		this.fKValue =  fKValue;
		
		this.fKName =fKName;
		fKPos = daos[0].getFieldIndex(fKName);
		if ( fKName instanceof String)
			setWhereCond(" where " + fKName + " = '" + fKValue + "'");
		else
			setWhereCond(" where " + fKName + " = " + fKValue);
		
	}

	


	
	public DModel4(Connection conn){
		try {
			this.conn = conn;		
			daos = new DAO [2];
			daos[1] = new DAO(conn, "КОМПЛЕКТУЮЩИЕ", "КОМПЛ_ID");
			daos[0] = new DAO(conn, "ДЕТАЛ_НАКЛАДНЫХ", "Д_НАКЛ_ID");
			columnsMap = new int[][]{
					
					{0,  0},
					{0,  1},
					{0,  2}, // КОМПЛ_id
					{0,  3},
					
							
					
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
		return "Select * from ДЕТАЛ_ЗАЯ join КОМПЛЕКТУЮЩИЕ using (КОМПЛ_ID) join  детал_накладных  using (компл_id)"; 
	}

	
	
	/**Ищет в таблице коплектующих подходящую запись и обновляет поля, с ней связанные*/
	public void find(int rowIndex){
		Item[] items = getRow(rowIndex);	
		String sqlFind;
		if(items[0].getVal(2) instanceof BigDecimal) {//items[0].getVal(2) -  это Компл ID в записи из  таблицы комплектующих
			sqlFind = "SELECT * FROM КОМПЛЕКТУЮЩИЕ WHERE КОМПЛ_ID = " + items[0].getVal(2);
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
				items[0].setVal(2, rs.getObject("КОМПЛ_ID"));
				items[1].pollFieldsFromResultSet(rs, daos[1].getColumnNames());
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





