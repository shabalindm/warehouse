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

/** ������� ��� ����� � �������������� ����������� �� ���������*/
public class Panel7 extends TableEditPanel<Item[]> {

	private class SearchBtnListener implements ActionListener {

		@Override
		public void actionPerformed(ActionEvent e) {
			for (int veiwRow : table.getSelectedRows()){
				((Model7)model).find(table.convertRowIndexToModel(veiwRow));
			}

		}
	}

	public Panel7(Connection conn ) {
		super(new Model7(conn));
		for (int i = 0; i< model.getColumnCount(); i++){
			if(((Model7) model).index1(i) != 3)
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

class Model7 extends MultyItemsModel {

	
	public Model7(Connection conn){
		try {
			this.conn = conn;		
			daos = new DAO [4];
			daos[0] = new DAO(conn, "�������������", "�����_ID");
			daos[1] = new DAO(conn, "������", "���_������");
			daos[2] = new DAO(conn, "���������", "����_ID");
			daos[3] = new DAO(conn, "�����_���������", "�_����_ID");
			
			columnsMap = new int[][]{
					
					{3,  0},//�_����_ID NOT NULL NUMBER      
					{3,  1},//����_ID   NOT NULL NUMBER   
					
					{2,  2}, // ����� ���������
					
					{1,  0}, // ���_������
					{1,  1}, // ���� ������
					{1,  6}, // ������
					
					{3,  2},//�����_ID  NOT NULL NUMBER      
					{3,  3}, //���_��             NUMBER(7,3) 
					
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
		return "Select * from �����_��������� join ������������� using (�����_ID)"
				+ " join ��������� using (����_id) join  ������  using (���_������)"; 
	}

	@Override
	void deleteFromDB(Item[] items) throws SQLException {
		items[3].delete();
	}

	@Override
	public boolean isCellEditable(int rowIndex, int columnIndex) {
		if(columnIndex == 0 )
			return	false;
		return true;
	}
	@Override
	void insertIntoDB(Item[] items) throws SQLException {
			items[3].storeNew2();
		}
		

	@Override
	void updateInDB(Item[] items) throws SQLException {
		items[3].store();
		
	}
	
	/**���� � ������� ������������ ���������� ������ � ��������� ����, � ��� ���������*/
	public void find(int rowIndex){
		Item[] items = getRow(rowIndex);	
		String sqlFind;
		if(items[3].getVal(2) instanceof BigDecimal) {//items[3].getVal(2) -  ��� ����� ID � ������ ��  ������� �����_���������
			 sqlFind = "SELECT * FROM ������������� WHERE �����_ID = " + items[3].getVal(2);
		} else{
			String tu = (items[0].getVal(3) == null ) ? " is null " : " = '" + items[0].getVal(3).toString().trim().toUpperCase() + "'";
			String mark = (items[0].getVal(4) == null ) ? " is null " : " = '" + items[0].getVal(4).toString().trim().toUpperCase() + "'";		
			sqlFind = "SELECT * FROM ������������� WHERE �� " + tu + " AND ����� " + mark;
		}
		System.out.println(sqlFind);
		try{
			Statement stmt = conn.createStatement();
			ResultSet rs = stmt.executeQuery(sqlFind);
			if (rs.next()){
				items[3].setVal(2, rs.getObject("�����_ID"));
				items[0].pollFieldsFromResultSet(rs, daos[0].getColumnNames());
			}
			fireTableRowsUpdated(rowIndex, rowIndex);
			stmt.close();
		} catch (Exception e){e.printStackTrace();}
		
	}


}


