package components;

import java.awt.BorderLayout;
import java.awt.FlowLayout;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;

import javax.swing.JLabel;
import javax.swing.JPanel;
import javax.swing.JTable;
import javax.swing.table.AbstractTableModel;

import dao.Item;

public class DetaledPanel extends TableEditPanel<Item> {

	Item mainItem ; // Это запись из главной таблицы, которая будет отображаться в заголовке
	JTable header ;
	
	public DetaledPanel(DetaledModel model, final Item mainItem) {
		super(model);
		this.mainItem = mainItem;
		Object [] row = new Object[mainItem.size()];
		AbstractTableModel aModel = new AbstractTableModel(){

			@Override
			public int getRowCount() {
				// TODO Auto-generated method stub
				return 1;
			}

			@Override
			public int getColumnCount() {
				// TODO Auto-generated method stub
				return mainItem.size();
			}

			@Override
			public Object getValueAt(int rowIndex, int columnIndex) {
				// TODO Auto-generated method stub
				return mainItem.getVal(columnIndex);
			}};
		
		header= new JTable(aModel);// mainItem.dao.getColumnNames()); 
		header.setCellSelectionEnabled(false);
		
		controlPanel.add(header, BorderLayout.CENTER);
	}

	@Override
	protected void setupControlPanel(JPanel controlPanel) {
		JPanel btmPanel = new JPanel();		
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
