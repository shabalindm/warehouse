package components;

import java.awt.Component;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.sql.Connection;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;

import javax.swing.JButton;
import javax.swing.JOptionPane;
import javax.swing.JPanel;

import dao.DAO;
import dao.Item;

public class Panel2 extends TableEditPanel<Item[]> {

	private class btnListener implements ActionListener {

		@Override
		public void actionPerformed(ActionEvent e) {
			for (int veiwRow : table.getSelectedRows()){
				((Model2)model).find(table.convertRowIndexToModel(veiwRow));
			}
			model.announce("OK");
		}
	}

	public Panel2(Connection conn) {
		super(new Model2(conn));		
	}

	@Override
	protected void setupControlPanel(JPanel btnPanel) {
		super.setupControlPanel(btnPanel);
		JButton btntn = makeButton("��������� ������", null, new btnListener());
		btntn.setToolTipText("���� ����� ��������� ��� ������� ����� ������. ������ ������ �������");
		btnPanel.add(btntn);
	}

	
}

//*������ ��� ����� ����������
 class Model2 extends MultyItemsModel {
	
	public Model2(Connection conn){
		try {
			this.conn = conn;		
			daos = new DAO [3];
			daos[0] = new DAO(conn, "����������", "����_ID");
			daos[1] = new DAO(conn, "������������", "����_ID");
			daos[2] = new DAO(conn, "�������������", "�����_ID");
			
			columnsMap = new int[][]{
					
				//	{0,  0},
					{0,  1},
					{0,  3}, // ���_������
					{0,  4},
					{0,  5},
					{0,  6},
					{0,  7},
					{0,  8},
					{0,  2}, // ����ID
					
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
		return "select  *  from  ������������� JOIN ������������ USING (�����_ID)  full join ���������� USING (����_ID)"; 
	}

	@Override
	void deleteFromDB(Item[] items) throws SQLException {
		items[0].delete();
	}

	@Override
	public boolean isCellEditable(int rowIndex, int columnIndex) {
		// TODO Auto-generated method stub
		return true;
	}
	@Override
	void insertIntoDB(Item[] items) throws SQLException {
			items[0].storeNew2();
		}
		

	@Override
	void updateInDB(Item[] items) throws SQLException {
		Object trb_id = items[0].getId();
		Object trb_name = items[0].getVal(1);
		if (trb_id !=null){
			if (trb_name !=null){
				items[0].store(); 
				
				}
			else
				items[0].delete();
		} else{
			if (trb_name !=null)
				items[0].storeNew2();			
		}
			
		
		//items[0].store(); 
		
	}
	
	/**���������, ���� �� � ������� ������ ������ ������, ���� ��� - ���������� �������*/
	public void find(int rowIndex){
		Item[] items = getRow(rowIndex);	
		
		if(items[0].getVal(3) == null) //items[0].getVal(3)-  ��� ����� ������ � ������� ����������
			return;
		
		String sqlCheck = "SELECT Count(*) FROM ������ WHERE ���_������ = '" + items[0].getVal(3) +"'";
		String sqlFind = "INSERT INTO ������ ( ���_������ ) VALUES ( '"+ items[0].getVal(3) + "')";
		
		System.out.println(sqlFind);
		try{
			Statement stmt = conn.createStatement();
			ResultSet rs = stmt.executeQuery(sqlCheck);
			rs.next();
			if (rs.getInt(1) == 1)
				return; // ����� ������ ��� ����
			
			int result = JOptionPane.showConfirmDialog((Component) null, "������� ������ � ������� " + items[0].getVal(3) + "?",
					"", JOptionPane.YES_NO_OPTION);
			if (result == 0)
				stmt.executeUpdate(sqlFind);
			
			stmt.close();
		} catch (Exception e){e.printStackTrace();}
		
	}


}
