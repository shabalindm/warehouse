package components;

import java.awt.BorderLayout;
import java.awt.FlowLayout;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;

import javax.swing.JLabel;
import javax.swing.JPanel;
import javax.swing.JScrollPane;
import javax.swing.JTable;
import javax.swing.table.AbstractTableModel;
import javax.swing.table.TableModel;

import dao.Item;
/**Базовый класс для панелей, открывающихся в качестеве детализации*/
public class DetaledPanel<T> extends TableEditPanel<T> {

	Item mainItem ; // Это запись из главной таблицы, которая будет отображаться в заголовке
	JPanel btmPanel;
	
	
	public DetaledPanel(AbstractItemsTableModel model,  Item mainItem) {
		super(model);
		this.mainItem = mainItem;

		// Собираем табличку из одной записи, которая показывает mainItem
		Object[] fiedls	= new Object[mainItem.size()];
		for (int i = 0; i < fiedls.length; i++)
			fiedls[i] = mainItem.getVal(i);
		JTable tbl =  new JTable(new Object [][]{fiedls}, mainItem.dao.getColumnNames());
		tbl.setCellSelectionEnabled(false);
		tbl.setEnabled(false);
		JPanel header= new JPanel();// mainItem.dao.getColumnNames()); 
		header.setLayout(new BorderLayout());
		
		header.add(tbl.getTableHeader(), BorderLayout.NORTH);
		header.add(tbl, BorderLayout.CENTER);
		
		//header.setCellSelectionEnabled(false);
		
		controlPanel.add(header, BorderLayout.CENTER);
			}

	@Override
	protected void setupControlPanel(JPanel controlPanel) {
		btmPanel = new JPanel();		
		btmPanel.setLayout(new FlowLayout(FlowLayout.LEFT));
		btmPanel.add(insertBtn); 
		btmPanel.add(deleteBtn);
		btmPanel.add(unDeleteBtn);
		btmPanel.add(writeBtn);
		btmPanel.add(clearBtn);		
		controlPanel.setLayout(new BorderLayout());
		controlPanel.add(btmPanel, BorderLayout.NORTH);	
		

	}
	
	
	@Override
	protected void createBottons() {		
		super.createBottons();
	}


	

}
