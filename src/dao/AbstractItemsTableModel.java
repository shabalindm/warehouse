package dao;

import java.sql.Connection;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;
import java.text.DateFormat;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.List;

import javax.swing.table.AbstractTableModel;

public abstract class AbstractItemsTableModel<T> extends AbstractTableModel {
	
	List<T> cache  = new ArrayList<>();
	int rowCount;
	String whereCond = "";
	Connection conn;
	MessageListener listener;
	DateFormat f = new SimpleDateFormat("MM/dd/yy");
	ResultSet rs;
	
/**   ������������� ��������� ���������, ������� ��������� ��� ������ � �������*/
	public void setMessageListener(MessageListener listener){
		this.listener = listener;
	}
	
/**   �������� ���������  (���� �� ����������) ���������*/
	void announce(String msg){
		if (listener != null)
			listener.setText(msg);		
	}
	
		
	public String getWhereCond() {
		return whereCond;
	}

	public void setWhereCond(String whereCond) {
		this.whereCond = whereCond;
	}

	/**������������ SQL ������, �� �������� ������ ����� ����� ������ �� ����*/
	abstract String getSQL();
	
	/** ���������� �������� � ����, ��������� ������ ������, ������� �����, ����������
	 *  ����� ����� � ��� � ��������� �������� ���� rowCount*/
	public void updateCache() {
			Statement stmt = null;
			String sql = getSQL(); 
			try{
				stmt = conn.createStatement(ResultSet.TYPE_SCROLL_INSENSITIVE, ResultSet.CONCUR_READ_ONLY);
				ResultSet newrs = stmt.executeQuery(sql);
				try {rs.close();} 
				catch (Exception e){} // ��������� ����������
				rs = newrs;
				// ������� ���
				cache.clear();
				// ������ ���������� �����
				if (!rs.last())
					rowCount = 0;
				else
					rowCount =  rs.getRow();
				rs.beforeFirst();				
			} catch (SQLException e) {
				// TODO Auto-generated catch block
				announce(e.getMessage());
				e.printStackTrace();
			}		

		finally{ fireTableDataChanged();}
	}
	
/*��������� �� ��������������� ������ ������ ����������� � ���*/
	abstract void readMore();


	@Override
	public int getRowCount() {
		return rowCount;
	}

	
	/** ������� ����� item � ���������� ��� � ����. �������� - ������ ����������, �������� � ���� ������������� �� ����������*/
	public abstract void addRows(List<String[]> records); 
	
		
	/** ������� �� ������� � �� ���� ������ �� ��������� ������ �������� ����  */
	public void deleteRows(int [] deleted) {		
		ArrayList<T> list = new ArrayList<>();
		try{
			for(int rowNum : deleted ){
				T item = cache.get(rowNum);
				deleteFromDBAndAddToList(item, list);
				announce(null);
			}
		} catch (SQLException e) {
			announce(e.getMessage());
			e.printStackTrace();
		}
		finally{
			cache.removeAll(list);
			rowCount -= list.size(); // ���������� ����� ���� � �������
		}
	} 
	
	 abstract void deleteFromDBAndAddToList(T item, ArrayList<T> list) throws SQLException  ;
	
	public abstract void close();
	
}
