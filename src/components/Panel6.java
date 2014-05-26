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

/** ������� ��� ����� � �������������� ����������� ������*/
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
			if(((Model6) model).index1(i) != 0)
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

class Model6 extends SimpleJoinModel {

	
	public Model6(Connection conn){
		try {
			this.conn = conn;		
			daos = new DAO [3];
			daos[2] = new DAO(conn, "�������������", "�����_ID");
			daos[1] = new DAO(conn, "������", "���_������");
			daos[0] = new DAO(conn, "�����_���", "�����_���_ID");
			columnsMap = new int[][]{
					
					{0,  0},
					{0,  1},
					
					{1,  1}, // ���� ������
					{1,  6}, // ������
					
					{0,  2}, // �����_id
					{0,  3},
					{0,  4},
							
					
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
		return "Select * from �����_��� join ������������� using (�����_ID) join  ������  using (���_������)"; 
	}

	
	
	/**���� � ������� ������������ ���������� ������ � ��������� ����, � ��� ���������*/
	public void find(int rowIndex){
		Item[] items = getRow(rowIndex);	
		String sqlFind;
		if(items[0].getVal(2) instanceof BigDecimal) {//items[0].getVal(2) -  ��� ����� ID � ������ ��  ������� �������������
			 sqlFind = "SELECT * FROM ������������� WHERE �����_ID = " + items[0].getVal(2);
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
				items[0].setVal(2, rs.getObject("�����_ID"));
				items[2].pollFieldsFromResultSet(rs, daos[2].getColumnNames());
			}
			fireTableRowsUpdated(rowIndex, rowIndex);
			stmt.close();
		} catch (Exception e){e.printStackTrace();}
		
	}


}

