package components.detaled;

import java.awt.BorderLayout;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;

import javax.swing.JLabel;
import javax.swing.JPanel;

import components.TableEditPanel;
import dao.Item;

public class DetaledPanel extends TableEditPanel<Item> {

	Item mainItem ; // Это запись из главной таблицы, которая будет отображаться в заголовке
	JLabel header ;
	
	public DetaledPanel(DetaledModel model, Item mainItem) {
		super(model);
		this.mainItem = mainItem;
		this.header.setText(mainItem.toString()); 
	}

	@Override
	protected void setupControlPanel(JPanel controlPanel) {
		JPanel btmPanel = new JPanel();		
		super.setupControlPanel(btmPanel); // Тут небольшая хитрость:)
		controlPanel.setLayout(new BorderLayout());
		controlPanel.add(btmPanel, BorderLayout.NORTH);
		header = new JLabel();
		controlPanel.add(header, BorderLayout.SOUTH);		
	}
	
	
	@Override
	protected void createBottons() {		
		super.createBottons();
	}


	

}
