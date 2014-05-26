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

/** ������� ��� ����� � �������������� ����������*/
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
		JButton searchBtn = makeButton("�����", null, new SearchBtnListener());
		searchBtn.setToolTipText("������ �������, ��� ������� ����� ���������� �����");
		btnPanel.add(searchBtn);
	}

	
}

class Model5 extends SimpleJoinModel {

	
	public Model5(Connection conn){
		try {
			this.conn = conn;		
			daos = new DAO [3];
			daos[0] = new DAO(conn, "����������", "����_ID");			
			daos[1] = new DAO(conn, "�������������", "�����_ID");
			daos[2] = new DAO(conn, "�������������", "�����_ID");
			
			columnsMap = new int[][]{
					
					{0,  0},//����_ID     NOT NULL NUMBER         
					{0,  1},//���_����    NOT NULL VARCHAR2(40)   
					{0,  2},//����_ID     NOT NULL NUMBER         
					{0,  3},//���_������           VARCHAR2(40)   
					{0,  4},//���������            NUMBER(7,3)    
					{0,  5},//������               NUMBER(7,3)  
					{0,  6},//ID_������            NUMBER 
					     
					//{1,  0},//�����_ID     NOT NULL NUMBER        
					{1,  1},//������                VARCHAR2(100) 
					{1,  2},//������������          VARCHAR2(200) 
					{1,  3},//��                    VARCHAR2(100) 
					{1,  4},//�����                 VARCHAR2(100) 
					{1,  5},//��_���������          VARCHAR2(40)  
					{1,  6},//��_������             NUMBER(7,3)   
					{1,  7},//����_����             VARCHAR2(400) 
					//{1,  8},//����_����             DATE  
					
					{0,  7},//ID_�����    NOT NULL NUMBER  					
					
					//{2,  0},//�����_ID     NOT NULL NUMBER        
					{2,  1},//������                VARCHAR2(100) 
					{2,  2},//������������          VARCHAR2(200) 
					{2,  3},//��                    VARCHAR2(100) 
					{2,  4},//�����                 VARCHAR2(100) 
					{2,  5},//��_���������          VARCHAR2(40)  
					{2,  6},//��_������             NUMBER(7,3)   
					{2,  7},//����_����             VARCHAR2(400) 
					//{2,  8},//����_����             DATE  
										
					{0,  8},//����_����            DATE           
					{0,  9},//����_������          DATE           
					{0,  10},//����                 VARCHAR2(1000) 
					
					

			};
		} catch (SQLException e) {
			e.printStackTrace();
		}
	}
	
	@Override
	protected String getSQL() {
		return "SELECT ����_ID,"
				+ "  ���_����,"
				+ "  ����_ID,"
				+ "  ���_������,"
				+ "  ���������,"
				+ "  ������,"
				+ "  ID_������,"
				+ "  ID_�����,"
				+ "  ����_����,"
				+ "  ����_������,"
				+ "  ����_����,"
				+ " "
				+ "  K1.������ �_������,"
				+ "  K1.������������ �_������������, "
				+ "  K1.�� �_��, "
				+ "  K1.����� �_�����, "
				+ "  K1.��_��������� �_��_���������,"
				+ "  K1.��_������ �_��_������,"
				+ "  K1.����_���� �_����_����,"
				+ "  K1.����_���� �_����_����,"
				+ " "
				+ " K2.������ �_������,"
				+ "  K2.������������ �_������������,"
				+ "  K2.�� �_��,"
				+ "  K2.����� �_�����,"
				+ "  K2.��_��������� �_��_���������,"
				+ "  K2.��_������ �_��_������,"
				+ "  K2.����_���� �_����_����,"
				+ "  K2.����_���� �_����_����"
				+ " FROM ���������� Join �������������  K1 on (ID_������ = K1.�����_ID) "
				+ "JOIN �������������  K2 on (ID_����� = K2.�����_ID) "; 
	}

	
		
	/**���� � ������� ������������ ���������� ������ � ��������� ����, � ��� ���������*/
	public void find(int rowIndex){
		Item[] items = getRow(rowIndex);	
		String sqlFind;
		
			if(items[0].getVal(6) instanceof BigDecimal) {//items[0].getVal(6) -  ��� ID ������������ 
				sqlFind = "SELECT * FROM ������������� WHERE �����_ID = " + items[0].getVal(6);
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
					items[0].setVal(6, rs.getObject("�����_ID"));
					items[1].pollFieldsFromResultSet(rs, daos[1].getColumnNames());
				}
				fireTableRowsUpdated(rowIndex, rowIndex);
				stmt.close();
			} catch (Exception e){e.printStackTrace();}
			
			
			if(items[0].getVal(7) instanceof BigDecimal) {//items[0].getVal(7) -  ��� ID ���������
				sqlFind = "SELECT * FROM ������������� WHERE �����_ID = " + items[0].getVal(7);
			} else{
				String tu = (items[2].getVal(3) == null ) ? " is null " : " = '" + items[2].getVal(3).toString().trim().toUpperCase() + "'";
				String mark = (items[2].getVal(4) == null ) ? " is null " : " = '" + items[2].getVal(4).toString().trim().toUpperCase() + "'";		
				sqlFind = "SELECT * FROM ������������� WHERE �� " + tu + " AND ����� " + mark;
			}
			System.out.println(sqlFind);
			try{
				Statement stmt = conn.createStatement();
				ResultSet rs = stmt.executeQuery(sqlFind);
				if (rs.next()){
					items[0].setVal(7, rs.getObject("�����_ID"));
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
			
			row[2].pollFieldsFromResultSet(rs, "ID_������ �_������ �_������������ �_�� �_����� �_��_���������  �_��_������ �_����_���� �_����_����".split("\\s+"));
			row[1].pollFieldsFromResultSet(rs, "ID_������ �_������ �_������������ �_�� �_����� �_��_���������  �_��_������ �_����_���� �_����_����".split("\\s+"));
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
			columnName = "�_"+columnName;
		if(index1(column) == 2  )
			columnName = "�_"+columnName;
		return columnName;
	}

//	@Override
//	protected int index1(int columnIndex) {
//		// TODO Auto-generated method stub
//		return super.index1(columnIndex);
//	}

	
}


