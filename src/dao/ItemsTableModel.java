package dao;

import java.sql.Connection;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;
import java.sql.Timestamp;
import java.text.DateFormat;
import java.text.ParseException;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;

import javax.swing.ScrollPaneConstants;
import javax.swing.table.AbstractTableModel;

public class ItemsTableModel extends AbstractTableModel{
	private List<Item> cache  = new ArrayList<>();
	private String whereCond = "";
	private DAO dao;
	private MessageListener listener;
	DateFormat f;
	
	public ItemsTableModel(DAO dao)  {
		this.dao = dao;
	}
	
	public void setMessageListener(MessageListener listener){
		this.listener = listener;
	}
	
	private void announce(String msg){
		if (listener != null)
			listener.setText(msg);		
	}
	
	public DAO getDAO() {
		return dao;
	}
	
		
	public String getWhereCond() {
		return whereCond;
	}

	public void setWhereCond(String whereCond) {
		this.whereCond = whereCond;
	}

	public void updateCache()  {
			Statement stmt = null;
			ResultSet rs = null;
			String sql = "select * from " + dao.getTableName() +" "  + whereCond;
			System.out.println(sql);
			try{			
				stmt = dao.getConnection().createStatement();
				rs = stmt.executeQuery(sql);
				// ������� ���
				cache.clear();
				//�������� ������ �� ��������������� ������ � ���
				while (rs.next()){
					Item item = new Item(null);
					item.pollFieldsFromResultSet(rs, dao.getColumnNames());
					cache.add(item);
				}	
			} catch (SQLException e) {
				// TODO Auto-generated catch block
				announce(e.getMessage());
				e.printStackTrace();
			}		

		finally{ fireTableDataChanged();
			try {stmt.close();
		} catch (SQLException e) {	
			e.printStackTrace();
		}}
	}
	

	@Override
	public String getColumnName(int column) {
		// TODO Auto-generated method stub
		return dao.getColumnNames()[column];
	}


	@Override
	public int getRowCount() {
		return cache.size();
	}

	@Override
	public int getColumnCount() {
		return dao.getColumnNames().length;
	}

	@Override
	public Object getValueAt(int rowIndex, int columnIndex) {
		 Object val = cache.get(rowIndex).getVal(columnIndex);
		if (java.util.Date.class.isAssignableFrom(dao.getColumnClasses()[columnIndex]) && val != null )
			val = f.format(val);			
		return val;
	}
	
	/** ������� ����� item � ���������� ��� � ����. �������� - ������ ����������, �������� � ���� ������������� �� ����������*/
	public void addRows(List<String[]> records) {
		try {
			while (records.size() > 0){
				Item item = new Item(null);
				String[] record = records.get(0);
				item.setSize(record.length);
				for (int i = 0; i<record.length; i++ ){					
					if (java.util.Date.class.isAssignableFrom(dao.getColumnClasses()[i]) && 
							record[i] != null && !record[i].matches("\\s*") ){
						try {
							item.setVal(i, new Timestamp((((Date)f.parseObject(record[i])).getTime())));
						} catch (ParseException e) {
							announce("������������ ������ ����");
							return;
						}
					}		
					else{ 				
						item.setVal(i, record[i]);}
				}

				// ��������� � ����
				dao.storeNew2(item);
				// ������� �� ���������
				records.remove(record);
			}
			announce(null);
			} catch (SQLException e) {
				announce(e.getMessage());
				e.printStackTrace(); 
			}
		
		}

	@Override
	public void setValueAt(Object aValue, int rowIndex, int columnIndex) {
		Item item = cache.get(rowIndex); 
		if (java.util.Date.class.isAssignableFrom(dao.getColumnClasses()[columnIndex])&& aValue != null )
			try {
				aValue = new Timestamp((((Date)f.parseObject((String) aValue)).getTime()));
			} catch (ParseException e1) {
					announce("������������ ������ ����");
					return;
			}
		
		try{				
			if (columnIndex == 0){
				 dao.changeID(item.getId(), aValue);
				 // ��������� item � ����������� ID
				 item.setId(aValue);
				 announce(null);
			}
			else{
				item.setVal(columnIndex, aValue);			
				dao.store(item);
				announce(null);
			}

		} catch (SQLException e){	
			announce(e.getMessage());
			e.printStackTrace();
		}	
		finally {
			try {
				dao.refresh(item);
			} catch (SQLException e) {
				e.printStackTrace();
			}
		}
	}
	
	@Override
	public boolean isCellEditable(int rowIndex, int columnIndex) {			
		return true;
	}	
	
	public void deleteRows(int [] deleted) {		
		ArrayList<Item> list = new ArrayList<>();
		try{
			for(int rowNum : deleted ){
				Item item = cache.get(rowNum);
				dao.delete(item);
				list.add(item); // ����� �������� ��������� � ������ ������� ��������� �� ��
				announce(null);
			}
		} catch (SQLException e) {
			announce(e.getMessage());
			e.printStackTrace();
		}
		finally{
			cache.removeAll(list);
		}
	}
}
