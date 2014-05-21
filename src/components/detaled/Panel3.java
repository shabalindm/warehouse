package components.detaled;

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

import components.AbstractItemsTableModel;
import components.ItemsModel;
import components.TableEditPanel;
import components.UserCancelledOperationException;
import components.WriteDataToDBException;
import dao.DAO;
import dao.Item;

 public class Panel3 extends TableEditPanel<Item> {
	 JButton trbBtn;
	 Connection conn;
	 List <TableWindow> childWindows = new ArrayList<>();
	 
	 private static ItemsModel makeModel(Connection conn){
		 DAO dao = null;
		 try {
			dao = new DAO(conn, "ÇÀßÂÊÈ", "ÍÎÌ_ÇÀßÂÊÈ");
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
		 trbBtn = makeButton("Òðåáîâàíèÿ", null, new ActionListener() {
				
				@Override
				public void actionPerformed(ActionEvent e) { 
					// Âûáîð çíà÷åíèÿ ÿ÷åéêè
					int selectedRow = table.getSelectedRow();
					if (selectedRow ==-1) // Íè÷åãî íå âûäåëåíî
						return;
					Item selectedItem = model.getRow(table.convertRowIndexToModel(selectedRow));
					Object fKValue = selectedItem.getId();					
				 	try {
						DAO dao = new  DAO(selectedItem.dao.getConnection(), "ÒÐÅÁÎÂÀÍÈß", "ÒÐÅÁ_ID");
						DetaledModel model = new DetaledModel(dao, fKValue, "ÍÎÌ_ÇÀßÂÊÈ");
						DetaledPanel panel = new DetaledPanel(model, selectedItem);
						TableWindow window = new TableWindow (panel, "Òðåáîâàíèÿ ïî çàÿâêå " + selectedItem.getId()); // Ðàçìåùàåì â íîâîì îêíå
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
		controlPanel.add(trbBtn); 
		
		
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
