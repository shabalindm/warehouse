package components;

import gui.MainFrame;

import java.awt.BorderLayout;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.sql.Connection;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.List;

import javax.swing.DefaultCellEditor;
import javax.swing.JButton;
import javax.swing.JCheckBox;
import javax.swing.JComboBox;
import javax.swing.JFrame;
import javax.swing.JPanel;

import dao.DAO;
import dao.Item;
// ������ � �������� 
 public class Panel3 extends TableEditPanel<Item> {
	 JButton trbBtn;
	 JButton detaledBtn;
	 JButton nakladBtn;
	 Connection conn;
	 List <TableWindow> childWindows = new ArrayList<>();
	 
	 private static ItemsModel makeModel(Connection conn){
		 DAO dao = null;
		 try {
			dao = new DAO(conn, "������", "���_������");
		} catch (SQLException e) {
			e.printStackTrace();
		} 
		 return new ItemsModel(dao);
	 }

	public Panel3(Connection conn) {
		super(makeModel( conn));
		this.conn = conn;	
		JComboBox<String> statusBox = new JComboBox<String>(new String[]{"�����", "��������","�����������","�������", "������"});
		 DefaultCellEditor statusEditor = new DefaultCellEditor(statusBox);
		table.getColumnModel().getColumn(6).setCellEditor(statusEditor);
		
		JComboBox<String> restrictedBox = new JComboBox<String>(new String[]{null,"�������" });
		 DefaultCellEditor restrictedEditor = new DefaultCellEditor(restrictedBox);
		table.getColumnModel().getColumn(13).setCellEditor(restrictedEditor);
	
		
			}


	
	@Override
	protected void createBottons() {
		super.createBottons();
		 trbBtn = makeButton("����������", null, new ActionListener() {
				
				@Override
				public void actionPerformed(ActionEvent e) { 
					// ����� �������� ������
					int selectedRow = table.getSelectedRow();
					if (selectedRow ==-1) // ������ �� ��������
						return;
					Item selectedItem = model.getRow(table.convertRowIndexToModel(selectedRow));
					Object fKValue = selectedItem.getId();					
				 	try {
						DAO dao = new  DAO(selectedItem.dao.getConnection(), "����������", "����_ID");
						DetaledModel dmodel = new DetaledModel(dao, fKValue, "���_������");
						dmodel.setMessageListener(model.getMessageListener());
						DetaledPanel panel = new DetaledPanel(dmodel, selectedItem);
						panel.setStateListener(MainFrame.stateListener);
						TableWindow window = new TableWindow (panel, "���������� �� ������ " + selectedItem.getId()); // ��������� � ����� ����
						childWindows.add(window);
					} catch (SQLException e1) {
						// TODO Auto-generated catch block
						e1.printStackTrace();
					}
					//DetaledModel detaled = new DetaledModel(dao, fKValue, 3);
					
					// TableWindow window = new TableWindow();
				}
			});
		 
		 detaledBtn = makeButton("���. ������", null, new ActionListener() {
				
				@Override
				public void actionPerformed(ActionEvent e) { 
					// ����� �������� ������
					int selectedRow = table.getSelectedRow();
					if (selectedRow ==-1) // ������ �� ��������
						return;
					Item selectedItem = model.getRow(table.convertRowIndexToModel(selectedRow));
					Object fKValue = selectedItem.getId();					
				 	try {
						DAO dao = new  DAO(selectedItem.dao.getConnection(), "�����_���", "�����_���_ID");
						DetaledModel dmodel = new DetaledModel(dao, fKValue, "���_������");
						dmodel.setMessageListener(model.getMessageListener());
						DetaledPanel panel = new DetaledPanel(dmodel, selectedItem);
						panel.setStateListener(MainFrame.stateListener);
						TableWindow window = new TableWindow (panel, "����������� �� ������ " + selectedItem.getId()); // ��������� � ����� ����
						childWindows.add(window);
					} catch (SQLException e1) {
						// TODO Auto-generated catch block
						e1.printStackTrace();
					}
					//DetaledModel detaled = new DetaledModel(dao, fKValue, 3);
					
					// TableWindow window = new TableWindow();
				}
			});
		 
		 nakladBtn = makeButton("���������", null, new ActionListener() {
				
				@Override
				public void actionPerformed(ActionEvent e) { 
					// ����� �������� ������
					int selectedRow = table.getSelectedRow();
					if (selectedRow ==-1) // ������ �� ��������
						return;
					Item selectedItem = model.getRow(table.convertRowIndexToModel(selectedRow));
					Object fKValue = selectedItem.getId();					
				 	try {
						DAO dao = new  DAO(selectedItem.dao.getConnection(), "���������", "����_ID");
						DetaledModel dmodel = new DetaledModel(dao, fKValue, "���_������");
						dmodel.setMessageListener(model.getMessageListener());
						DetaledPanel panel = new DetaledPanel4(dmodel, selectedItem);
						panel.setStateListener(MainFrame.stateListener);
						TableWindow window = new TableWindow (panel, "��������� �� ������ " + selectedItem.getId()); // ��������� � ����� ����
						childWindows.add(window);
					} catch (SQLException e1) {
						// TODO Auto-generated catch block
						e1.printStackTrace();
					}
				}
			});
	
	}

	@Override
	protected void setupControlPanel(JPanel controlPanel) {
		super.setupControlPanel(controlPanel);
		controlPanel.add(trbBtn); 
		controlPanel.add(detaledBtn); 
		controlPanel.add(nakladBtn); 
		
		
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
