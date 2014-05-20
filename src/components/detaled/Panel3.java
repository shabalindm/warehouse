package components.detaled;

import java.awt.BorderLayout;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.sql.Connection;
import java.sql.SQLException;

import javax.swing.JButton;
import javax.swing.JFrame;
import javax.swing.JPanel;

import components.AbstractItemsTableModel;
import components.ItemsModel;
import components.TableEditPanel;
import dao.DAO;
import dao.Item;

 public class Panel3 extends TableEditPanel<Item> {
	 JButton trbBtn;
	 Connection conn;
	 
	 private static ItemsModel makeModel(Connection conn){
		 DAO dao = null;
		 try {
			dao = new DAO(conn, "ЗАЯВКИ", "НОМ_ЗАЯВКИ");
		} catch (SQLException e) {
			e.printStackTrace();
		} 
		 return new ItemsModel(dao);
	 }

	public Panel3(Connection conn) {
		super(makeModel( conn));
		this.conn = conn;
		
			}


	
	@Override
	protected void createBottons() {
		super.createBottons();
		 trbBtn = makeButton("Требования", null, new ActionListener() {
				
				@Override
				public void actionPerformed(ActionEvent e) { 
					// Выбор значения ячейки
					Object fKValue = 
					model.getRow(table.convertRowIndexToModel(table.getSelectedRow())).getId();
					
				//	DAO dao = new  DAO(conn, "ТРЕБОВАНИЯ", "ТРЕБ_ID");
					//DetaledModel detaled = new DetaledModel(dao, fKValue, 3);
					
					// TableWindow window = new TableWindow();
				}
			});
	
	}

	@Override
	protected void setupControlPanel(JPanel controlPanel) {
		super.setupControlPanel(controlPanel);
		 trbBtn = makeButton("Требования", null, new ActionListener() {
			
			@Override
			public void actionPerformed(ActionEvent e) { 
				 
			}
		});
		
	}
	
	
	
	
	
}
