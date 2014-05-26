package components;

import java.awt.Color;
import java.awt.Component;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.math.BigDecimal;
import java.sql.Connection;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;

import javax.swing.JButton;
import javax.swing.JPanel;
import javax.swing.JTable;
import javax.swing.table.DefaultTableCellRenderer;

import dao.DAO;
import dao.Item;

/** Òàáëèöà äëÿ ââîäà è ğåäàêòèğîâàíèÿ òğåáîâàíèé*/
public class Panel5 extends TableEditPanel<Item[]> {

	private class SearchBtnListener implements ActionListener {

		@Override
		public void actionPerformed(ActionEvent e) {
			for (int veiwRow : table.getSelectedRows()){
				((Model5)model).find(table.convertRowIndexToModel(veiwRow));
			}

		}
	}
	
	   
	public Panel5(Connection conn ) {
		super(new Model5(conn));
		for (int i = 0; i< model.getColumnCount(); i++){
			if(((Model5) model).index1(i) != 0)
				table.getColumnModel().getColumn(i).setCellRenderer(new GreyCellRenderer());
				
		}
	}

	@Override
	protected void setupControlPanel(JPanel btnPanel) {
		super.setupControlPanel(btnPanel);
		JButton searchBtn = makeButton("Íàéòè", null, new SearchBtnListener());
		searchBtn.setToolTipText("Âûäåëè ñòğî÷êè, äëÿ êîòîğûõ íóæíî ïğîèçâåñòè ïîèñê");
		btnPanel.add(searchBtn);
	}

	
}

class Model5 extends SimpleJoinModel {

	
	public Model5(Connection conn){
		try {
			this.conn = conn;		
			daos = new DAO [3];
			daos[0] = new DAO(conn, "ÒĞÅÁÎÂÀÍÈß", "ÒĞÅÁ_ID");			
			daos[1] = new DAO(conn, "ÊÎÌÏËÅÊÒÓŞÙÈÅ", "ÊÎÌÏË_ID");
			daos[2] = new DAO(conn, "ÊÎÌÏËÅÊÒÓŞÙÈÅ", "ÊÎÌÏË_ID");
			
			columnsMap = new int[][]{
					
					{0,  0},//ÒĞÅÁ_ID     NOT NULL NUMBER         
					{0,  1},//ÍÎÌ_ÒĞÅÁ    NOT NULL VARCHAR2(40)   
					{0,  2},//ÑÏÅÖ_ID     NOT NULL NUMBER         
					{0,  3},//ÍÎÌ_ÇÀßÂÊÈ           VARCHAR2(40)   
					{0,  4},//ÇÀÏĞÎØÅÍÎ            NUMBER(7,3)    
					{0,  5},//ÂÛÄÀÍÎ               NUMBER(7,3)  
					{0,  6},//ID_ÇÀÏĞÎØ            NUMBER 
					     
					//{1,  0},//ÊÎÌÏË_ID     NOT NULL NUMBER        
					{1,  1},//ÃĞÓÏÏÀ                VARCHAR2(100) 
					{1,  2},//ÍÀÈÌÅÍÎÂÀÍÈÅ          VARCHAR2(200) 
					{1,  3},//ÒÓ                    VARCHAR2(100) 
					{1,  4},//ÌÀĞÊÀ                 VARCHAR2(100) 
					{1,  5},//ÅÄ_ÈÇÌÅĞÅÍÈß          VARCHAR2(40)  
					{1,  6},//ÍÀ_ÑÊËÀÄÅ             NUMBER(7,3)   
					{1,  7},//ÊÎÌÏ_ÈÍÔÎ             VARCHAR2(400) 
					//{1,  8},//ÄÀÒÀ_ÊÎÌÏ             DATE  
					
					{0,  7},//ID_ÂÛÄÀÍ    NOT NULL NUMBER  					
					
					//{2,  0},//ÊÎÌÏË_ID     NOT NULL NUMBER        
					{2,  1},//ÃĞÓÏÏÀ                VARCHAR2(100) 
					{2,  2},//ÍÀÈÌÅÍÎÂÀÍÈÅ          VARCHAR2(200) 
					{2,  3},//ÒÓ                    VARCHAR2(100) 
					{2,  4},//ÌÀĞÊÀ                 VARCHAR2(100) 
					{2,  5},//ÅÄ_ÈÇÌÅĞÅÍÈß          VARCHAR2(40)  
					{2,  6},//ÍÀ_ÑÊËÀÄÅ             NUMBER(7,3)   
					{2,  7},//ÊÎÌÏ_ÈÍÔÎ             VARCHAR2(400) 
					//{2,  8},//ÄÀÒÀ_ÊÎÌÏ             DATE  
										
					{0,  8},//ÄÀÒÀ_ÒĞÅÁ            DATE           
					{0,  9},//ÄÀÒÀ_ÂÛÄÀ×È          DATE           
					{0,  10},//ÈÍÔÎ                 VARCHAR2(1000) 
					
					

			};
		} catch (SQLException e) {
			e.printStackTrace();
		}
	}
	
	@Override
	protected String getSQL() {
		return "SELECT ÒĞÅÁ_ID,"
				+ "  ÍÎÌ_ÒĞÅÁ,"
				+ "  ÑÏÅÖ_ID,"
				+ "  ÍÎÌ_ÇÀßÂÊÈ,"
				+ "  ÇÀÏĞÎØÅÍÎ,"
				+ "  ÂÛÄÀÍÎ,"
				+ "  ID_ÇÀÏĞÎØ,"
				+ "  ID_ÂÛÄÀÍ,"
				+ "  ÄÀÒÀ_ÒĞÅÁ,"
				+ "  ÄÀÒÀ_ÂÛÄÀ×È,"
				+ "  ÒĞÅÁ_ÈÍÔÎ,"
				+ " "
				+ "  K1.ÃĞÓÏÏÀ Ç_ÃĞÓÏÏÀ,"
				+ "  K1.ÍÀÈÌÅÍÎÂÀÍÈÅ Ç_ÍÀÈÌÅÍÎÂÀÍÈÅ, "
				+ "  K1.ÒÓ Ç_ÒÓ, "
				+ "  K1.ÌÀĞÊÀ Ç_ÌÀĞÊÀ, "
				+ "  K1.ÅÄ_ÈÇÌÅĞÅÍÈß Ç_ÅÄ_ÈÇÌÅĞÅÍÈß,"
				+ "  K1.ÍÀ_ÑÊËÀÄÅ Ç_ÍÀ_ÑÊËÀÄÅ,"
				+ "  K1.ÊÎÌÏ_ÈÍÔÎ Ç_ÊÎÌÏ_ÈÍÔÎ,"
				+ "  K1.ÄÀÒÀ_ÊÎÌÏ Ç_ÄÀÒÀ_ÊÎÌÏ,"
				+ " "
				+ " K2.ÃĞÓÏÏÀ Â_ÃĞÓÏÏÀ,"
				+ "  K2.ÍÀÈÌÅÍÎÂÀÍÈÅ Â_ÍÀÈÌÅÍÎÂÀÍÈÅ,"
				+ "  K2.ÒÓ Â_ÒÓ,"
				+ "  K2.ÌÀĞÊÀ Â_ÌÀĞÊÀ,"
				+ "  K2.ÅÄ_ÈÇÌÅĞÅÍÈß Â_ÅÄ_ÈÇÌÅĞÅÍÈß,"
				+ "  K2.ÍÀ_ÑÊËÀÄÅ Â_ÍÀ_ÑÊËÀÄÅ,"
				+ "  K2.ÊÎÌÏ_ÈÍÔÎ Â_ÊÎÌÏ_ÈÍÔÎ,"
				+ "  K2.ÄÀÒÀ_ÊÎÌÏ Â_ÄÀÒÀ_ÊÎÌÏ"
				+ " FROM ÒĞÅÁÎÂÀÍÈß Join êîìïëåêòóşùèå  K1 on (ID_ÇÀÏĞÎØ = K1.ÊÎÌÏË_ID) "
				+ "JOIN êîìïëåêòóşùèå  K2 on (ID_ÂÛÄÀÍ = K2.ÊÎÌÏË_ID) "; 
	}

	
		
	/**Èùåò â òàáëèöå êîïëåêòóşùèõ ïîäõîäÿùóş çàïèñü è îáíîâëÿåò ïîëÿ, ñ íåé ñâÿçàííûå*/
	public void find(int rowIndex){
		Item[] items = getRow(rowIndex);	
		String sqlFind;
		
			if(items[0].getVal(6) instanceof BigDecimal) {//items[0].getVal(6) -  ıòî ID çàïğîøåííîãî 
				sqlFind = "SELECT * FROM ÊÎÌÏËÅÊÒÓŞÙÈÅ WHERE ÊÎÌÏË_ID = " + items[0].getVal(6);
			} else{
				String tu = (items[1].getVal(3) == null ) ? " is null " : " = '" + items[1].getVal(3).toString().trim().toUpperCase() + "'";
				String mark = (items[1].getVal(4) == null ) ? " is null " : " = '" + items[1].getVal(4).toString().trim().toUpperCase() + "'";		
				sqlFind = "SELECT * FROM ÊÎÌÏËÅÊÒÓŞÙÈÅ WHERE ÒÓ " + tu + " AND ÌÀĞÊÀ " + mark;
			}
			System.out.println(sqlFind);
			try{
				Statement stmt = conn.createStatement();
				ResultSet rs = stmt.executeQuery(sqlFind);
				if (rs.next()){
					items[0].setVal(6, rs.getObject("ÊÎÌÏË_ID"));
					items[1].pollFieldsFromResultSet(rs, daos[1].getColumnNames());
				}
				fireTableRowsUpdated(rowIndex, rowIndex);
				stmt.close();
			} catch (Exception e){e.printStackTrace();}
			
			
			if(items[0].getVal(7) instanceof BigDecimal) {//items[0].getVal(7) -  ıòî ID âûäàííîãî
				sqlFind = "SELECT * FROM ÊÎÌÏËÅÊÒÓŞÙÈÅ WHERE ÊÎÌÏË_ID = " + items[0].getVal(7);
			} else{
				String tu = (items[2].getVal(3) == null ) ? " is null " : " = '" + items[2].getVal(3).toString().trim().toUpperCase() + "'";
				String mark = (items[2].getVal(4) == null ) ? " is null " : " = '" + items[2].getVal(4).toString().trim().toUpperCase() + "'";		
				sqlFind = "SELECT * FROM ÊÎÌÏËÅÊÒÓŞÙÈÅ WHERE ÒÓ " + tu + " AND ÌÀĞÊÀ " + mark;
			}
			System.out.println(sqlFind);
			try{
				Statement stmt = conn.createStatement();
				ResultSet rs = stmt.executeQuery(sqlFind);
				if (rs.next()){
					items[0].setVal(7, rs.getObject("ÊÎÌÏË_ID"));
					items[2].pollFieldsFromResultSet(rs, daos[2].getColumnNames());
				}
				fireTableRowsUpdated(rowIndex, rowIndex);
				stmt.close();
			} catch (Exception e){e.printStackTrace();}
		}
	

	@Override
	Item[] getRowFromResultSet(int rowNumber) {
		Item[] row = getEmptyRow();		
		try {
			rs.absolute(rowNumber+1);	
			
			row[2].pollFieldsFromResultSet(rs, "ID_ÇÀÏĞÎØ Â_ÃĞÓÏÏÀ Â_ÍÀÈÌÅÍÎÂÀÍÈÅ Â_ÒÓ Â_ÌÀĞÊÀ Â_ÅÄ_ÈÇÌÅĞÅÍÈß  Â_ÍÀ_ÑÊËÀÄÅ Â_ÊÎÌÏ_ÈÍÔÎ Â_ÄÀÒÀ_ÊÎÌÏ".split("\\s+"));
			row[1].pollFieldsFromResultSet(rs, "ID_ÇÀÏĞÎØ Ç_ÃĞÓÏÏÀ Ç_ÍÀÈÌÅÍÎÂÀÍÈÅ Ç_ÒÓ Ç_ÌÀĞÊÀ Ç_ÅÄ_ÈÇÌÅĞÅÍÈß  Ç_ÍÀ_ÑÊËÀÄÅ Ç_ÊÎÌÏ_ÈÍÔÎ Ç_ÄÀÒÀ_ÊÎÌÏ".split("\\s+"));
			row[0].pollFieldsFromResultSet(rs, daos[0].getColumnNames());	
			
		} catch (SQLException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
		return row;
	}

	@Override
	public String getColumnName(int column) {
		// TODO Auto-generated method stub
		String columnName = super.getColumnName(column);
		if(index1(column) == 1  )
			columnName = "Ç_"+columnName;
		if(index1(column) == 2  )
			columnName = "Â_"+columnName;
		return columnName;
	}

//	@Override
//	protected int index1(int columnIndex) {
//		// TODO Auto-generated method stub
//		return super.index1(columnIndex);
//	}

	
}


