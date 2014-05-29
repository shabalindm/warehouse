package components;

import gui.MainFrame;

import java.awt.BorderLayout;
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
import javax.swing.JFrame;
import javax.swing.JPanel;

import dao.DAO;
import dao.Item;
/**������ ��� ��������� ��������� � ������� ��� ��������� �����������*/
 public class Panel4 extends TableEditPanel<Item[]> {
	 JButton detNaclBtn;
	 List<TableWindow> childWindows = new ArrayList<>();
	 
	 public Panel4(Connection conn ) {
			super(new Model4(conn));
			for (int i = 0; i< model.getColumnCount(); i++){
				if(((Model4) model).index1(i) != 0)
					table.getColumnModel().getColumn(i).setCellRenderer(new GreyCellRenderer());
					
			}
		}


	
	@Override
	protected void createBottons() {
		super.createBottons();
		detNaclBtn = makeButton("�����������", null, new ActionListener() {
				
				@Override
				public void actionPerformed(ActionEvent e) { 
					// ����� �������� ������
					int selectedRow = table.getSelectedRow();
					if (selectedRow ==-1) // ������ �� ��������
						return;
					Item selectedItem = model.getRow(table.convertRowIndexToModel(selectedRow))[0];
					Object fKValue = selectedItem.getId();					
				 	DPanel4 panel = new DPanel4 (selectedItem.dao.getConnection(), selectedItem, fKValue, "����_ID");
					panel.model.setMessageListener(model.getMessageListener());
					panel.setStateListener(MainFrame.stateListener);
					TableWindow window = new TableWindow (panel, "����������� ���������" + selectedItem.getId()); // ��������� � ����� ����
					childWindows.add(window);
					
					
				}
			});
	}
		
	@Override
	protected void setupControlPanel(JPanel controlPanel) {
		super.setupControlPanel(controlPanel);
		controlPanel.add(detNaclBtn); 
		
		
		
	}

	@Override
	public void close() throws WriteDataToDBException,
			UserCancelledOperationException {
		while(childWindows.size()>0){
			childWindows.get(0).close();
			childWindows.remove(0);
		}
		super.close();		
	}
	
	
}
 
 class Model4 extends SimpleJoinModel {
		public Model4(Connection conn){
			try {
				this.conn = conn;		
				daos = new DAO [2];
				daos[0] = new DAO(conn, "���������", "����_ID");
				daos[1] = new DAO(conn, "������", "���_������");
				
				columnsMap = new int[][]{
						
						{0,  0},//����_ID      NOT NULL NUMBER 						
						{0,  2},//���_���_����          VARCHAR2(100) 
						{0,  3},//��_���_����           DATE          
						{0,  4},//���_��                VARCHAR2(100) 
						{0,  1},//���_������   NOT NULL VARCHAR2(40)
						
						//{1,  0}, //���_������  NOT NULL VARCHAR2(40)   
						{1,  1}, //����_������          DATE           
						{1,  2}, //���_�����            VARCHAR2(40)   
						{1,  3}, //��_�����             DATE           
						{1,  4}, //���������            VARCHAR2(200)  
						{1,  5}, //�����_���            NUMBER(9,2)    
						{1,  6}, //������      NOT NULL VARCHAR2(40)   
						{1,  7}, //��_������            DATE           
						{1,  8}, //�����                VARCHAR2(40)   
						{1,  9}, //�������              VARCHAR2(200)  
						{1,  10}, //�������              VARCHAR2(200)  
						{1,  11}, //���_������           VARCHAR2(40)   
						{1,  12}, //���_����             VARCHAR2(1000) 
						{1,  13}, //����_��_���          VARCHAR2(16)

						
				};
			} catch (SQLException e) {
				e.printStackTrace();
			}
		}
		
		@Override
		protected String getSQL() {
			return "Select * from ��������� join ������ using (���_������)"; 
		}

	}

