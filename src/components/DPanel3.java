package components;

import gui.MainFrame;

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


/**Панель с детализацией к заявке, и еще с кнопочкой для детализации накладных*/
public class DPanel3 extends   DetaledPanel<Item>  {

	public DPanel3(Connection conn, Item mainItem,  Object fKValue, String fKName) {
		super(new DModel3(conn, fKValue,  fKName), mainItem);
	}

	JButton detNaclBtn;	
	List <TableWindow> childWindows = new ArrayList<>();
	

	
	@Override
	protected void createBottons() {
		super.createBottons();
		detNaclBtn = makeButton("детализация", null, new ActionListener() {
				
				@Override
				public void actionPerformed(ActionEvent e) { 
					// Выбор значения ячейки
					int selectedRow = table.getSelectedRow();
					if (selectedRow ==-1) // Ничего не выделено
						return;
					Item selectedItem = model.getRow(table.convertRowIndexToModel(selectedRow));
					Object fKValue = selectedItem.getId();					
				 	DPanel4 panel = new DPanel4 (selectedItem.dao.getConnection(), selectedItem, fKValue, "НАКЛ_ID");
					panel.model.setMessageListener(model.getMessageListener());
					panel.setStateListener(MainFrame.stateListener);
					TableWindow window = new TableWindow (panel, "детализация накладных" + selectedItem.getId()); // Размещаем в новом окне
					childWindows.add(window);
					
					
				}
			});
	}
		
	@Override
	protected void setupControlPanel(JPanel controlPanel) {
		super.setupControlPanel(controlPanel);
		btmPanel.add(detNaclBtn); 
		
		
		
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



class DModel3 extends ItemsModel {
	private Object fKValue;
	private String fKName;
	private int fKPos;
	
	public DModel3(Connection conn, Object fKValue, String fKName) {
		this(conn);
		this.fKValue =  fKValue;
		
		this.fKName =fKName;
		fKPos = dao.getFieldIndex(fKName);
		if ( fKName instanceof String)
			setWhereCond(" where " + fKName + " = '" + fKValue + "'");
		else
			setWhereCond(" where " + fKName + " = " + fKValue);
		
	}

	

	private static DAO mkDAO(Connection conn){
		DAO dao = null;
		try {
			dao = new DAO(conn, "НАКЛАДНЫЕ", "НАКЛ_ID");
		} catch (SQLException e) {
			e.printStackTrace();
		}
		return dao;
	}
	
	public DModel3(Connection conn){
		super(mkDAO( conn));
		
	}
	
	

	
	
	

	@Override
	protected Item getEmptyRow() {
		
		Item item = super.getEmptyRow();
	    item.setVal(fKPos, fKValue);
		return item;
	}

}





