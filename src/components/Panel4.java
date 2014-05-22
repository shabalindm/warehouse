package components;

import java.awt.BorderLayout;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.sql.Connection;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.List;

import javax.swing.JButton;
import javax.swing.JFrame;
import javax.swing.JPanel;

import dao.DAO;
import dao.Item;

 public class Panel4 extends TableEditPanel<Item> {
	 JButton detNaclBtn;
	 
	 Connection conn;
	 List <TableWindow> childWindows = new ArrayList<>();
	 
	 private static ItemsModel makeModel(Connection conn){
		 DAO dao = null;
		 try {
			dao = new DAO(conn, "НАКЛАДНЫЕ", "НАКЛ_ID");
		} catch (SQLException e) {
			e.printStackTrace();
		} 
		 return new ItemsModel(dao);
	 }

	public Panel4(Connection conn) {
		super(makeModel( conn));
		this.conn = conn;		
			}


	
	@Override
	protected void createBottons() {
		super.createBottons();
		detNaclBtn = makeButton("детазизация", null, new ActionListener() {
				
				@Override
				public void actionPerformed(ActionEvent e) { 
					// Выбор значения ячейки
					int selectedRow = table.getSelectedRow();
					if (selectedRow ==-1) // Ничего не выделено
						return;
					Item selectedItem = model.getRow(table.convertRowIndexToModel(selectedRow));
					Object fKValue = selectedItem.getId();					
				 	try {
						DAO dao = new  DAO(selectedItem.dao.getConnection(), "ДЕТАЛ_НАКЛАДНЫХ", "Д_НАКЛ_ID");
						DetaledModel dmodel = new DetaledModel(dao, fKValue, "НАКЛ_ID");
						dmodel.setMessageListener(model.getMessageListener());
						dmodel.setStateListener(model.getStateListener());
						DetaledPanel panel = new DetaledPanel(dmodel, selectedItem);
						TableWindow window = new TableWindow (panel, "детализация накладных" + selectedItem.getId()); // Размещаем в новом окне
						childWindows.add(window);
					} catch (SQLException e1) {
						// TODO Auto-generated catch block
						e1.printStackTrace();
					}
					//DetaledModel detaled = new DetaledModel(dao, fKValue, 3);
					
					// TableWindow window = new TableWindow();
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
