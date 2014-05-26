package components;

import java.awt.FlowLayout;
import java.sql.Connection;
import java.sql.ResultSet;
import java.sql.Statement;

import javax.swing.JButton;
import javax.swing.JPanel;
import javax.swing.JTable;

import components.TableEditPanel.ClearBtnListener;
import components.TableEditPanel.DeleteBtnListener;
import components.TableEditPanel.InsertBtnListener;
import components.TableEditPanel.UnDeleteBtnListener;
import components.TableEditPanel.WhereBtnListener;
import components.TableEditPanel.WriteBtnListener;
import dao.Item;

public class WiewNotEditPanel extends TableEditPanel<Item>{
	public WiewNotEditPanel(AbstractItemsTableModel<Item> model) {
		super(model);
		// TODO Auto-generated constructor stub
	}

	@Override
	protected void createBottons() {
		 whereBtn = makeButton("Условия", null, new WhereBtnListener());
		 clearBtn = makeButton("Обновить", null, new ClearBtnListener()) ;
	}

	@Override
	protected void setupControlPanel(JPanel controlPanel) {
		 controlPanel.setLayout(new FlowLayout(FlowLayout.LEFT));
		 controlPanel.add(clearBtn);
		 controlPanel.add(whereBtn);
	}	
	
	
	 

}

