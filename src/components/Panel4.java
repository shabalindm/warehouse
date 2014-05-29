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
/**Панель для просмотра накладных с кнопкой для просмотра детализации*/
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
		detNaclBtn = makeButton("детализация", null, new ActionListener() {
				
				@Override
				public void actionPerformed(ActionEvent e) { 
					// Выбор значения ячейки
					int selectedRow = table.getSelectedRow();
					if (selectedRow ==-1) // Ничего не выделено
						return;
					Item selectedItem = model.getRow(table.convertRowIndexToModel(selectedRow))[0];
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
				daos[0] = new DAO(conn, "НАКЛАДНЫЕ", "НАКЛ_ID");
				daos[1] = new DAO(conn, "ЗАЯВКИ", "НОМ_ЗАЯВКИ");
				
				columnsMap = new int[][]{
						
						{0,  0},//НАКЛ_ID      NOT NULL NUMBER 						
						{0,  2},//НОМ_ТОВ_НАКЛ          VARCHAR2(100) 
						{0,  3},//ДТ_ТОВ_НАКЛ           DATE          
						{0,  4},//НОМ_СФ                VARCHAR2(100) 
						{0,  1},//НОМ_ЗАЯВКИ   NOT NULL VARCHAR2(40)
						
						//{1,  0}, //НОМ_ЗАЯВКИ  NOT NULL VARCHAR2(40)   
						{1,  1}, //ДАТА_ЗАЯВКИ          DATE           
						{1,  2}, //НОМ_СЧЕТА            VARCHAR2(40)   
						{1,  3}, //ДТ_СЧЕТА             DATE           
						{1,  4}, //ПОСТАВЩИК            VARCHAR2(200)  
						{1,  5}, //СУММА_СЧТ            NUMBER(9,2)    
						{1,  6}, //СТАТУС      NOT NULL VARCHAR2(40)   
						{1,  7}, //ДТ_ОПЛАТЫ            DATE           
						{1,  8}, //СЛУЖЗ                VARCHAR2(40)   
						{1,  9}, //СИСТЕМА              VARCHAR2(200)  
						{1,  10}, //ИЗДЕЛИЕ              VARCHAR2(200)  
						{1,  11}, //НОМ_ЗАКАЗА           VARCHAR2(40)   
						{1,  12}, //ЗАЯ_ИНФО             VARCHAR2(1000) 
						{1,  13}, //ЗАПР_НА_ИЗМ          VARCHAR2(16)

						
				};
			} catch (SQLException e) {
				e.printStackTrace();
			}
		}
		
		@Override
		protected String getSQL() {
			return "Select * from НАКЛАДНЫЕ join ЗАЯВКИ using (НОМ_ЗАЯВКИ)"; 
		}

	}

