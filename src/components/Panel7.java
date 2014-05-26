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
			if(((Model7) model).index1(i) != 0)
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

class Model7 extends SimpleJoinModel {

	
	public Model7(Connection conn){
		try {
			this.conn = conn;		
			daos = new DAO [4];
			daos[3] = new DAO(conn, "�������������", "�����_ID");
			daos[2] = new DAO(conn, "������", "���_������");
			daos[1] = new DAO(conn, "���������", "����_ID");
			daos[0] = new DAO(conn, "�����_���������", "�_����_ID");
			
			columnsMap = new int[][]{
					
					{0,  0},//�_����_ID NOT NULL NUMBER      
					{0,  1},//����_ID   NOT NULL NUMBER   
					
					{1,  2}, // ����� ���������
					
					{2,  0}, // ���_������
					{2,  1}, // ���� ������
					{2,  6}, // ������
					
					{0,  2},//�����_ID  NOT NULL NUMBER      
					{0,  3},//���_��             NUMBER(7,3) 
					
					{3,  1},
					{3,  2},
					{3,  3},
					{3,  4},
					{3,  5},
					{3,  6},
					{3,  7},
					{3,  8},

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

	
	
	/**���� � ������� ������������ ���������� ������ � ��������� ����, � ��� ���������*/
	public void find(int rowIndex){
		Item[] items = getRow(rowIndex);	
		String sqlFind;
		if(items[0].getVal(2) instanceof BigDecimal) {//items[0].getVal(2) -  ��� ����� ID � ������ ��  ������� �������������
			 sqlFind = "SELECT * FROM ������������� WHERE �����_ID = " + items[0].getVal(2);
		} else{
			String tu = (items[3].getVal(3) == null ) ? " is null " : " = '" + items[3].getVal(3).toString().trim().toUpperCase() + "'";
			String mark = (items[3].getVal(4) == null ) ? " is null " : " = '" + items[3].getVal(4).toString().trim().toUpperCase() + "'";		
			sqlFind = "SELECT * FROM ������������� WHERE �� " + tu + " AND ����� " + mark;
		}
		System.out.println(sqlFind);
		try{
			Statement stmt = conn.createStatement();
			ResultSet rs = stmt.executeQuery(sqlFind);
			if (rs.next()){
				items[0].setVal(2, rs.getObject("�����_ID"));
				items[3].pollFieldsFromResultSet(rs, daos[3].getColumnNames());
			}
			fireTableRowsUpdated(rowIndex, rowIndex);
			stmt.close();
		} catch (Exception e){e.printStackTrace();}
		
	}

}


