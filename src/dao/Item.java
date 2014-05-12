package dao;

import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.Arrays;

/**�������� ��������� ������������� ������ �� ����.  
 * ��������� ����� ���������� � ���� (0-� ����  - ��� id)*/
public class Item {
	private String rowId;
	private Integer idPlace = null; 
	private Object [] fields;
	
	
	public Item(int lenght, Integer idPlace) {
		this.idPlace = idPlace;
		fields = new Object [lenght];
	}
	
	
	/** i = 0, 1 ... */
	public Object getVal(int i){
			return fields[i];		
	}
	
	/** i = 0, 1 .. */
	public void setVal(int i, Object val){
		fields[i] = val;
	}
	
	public Object getId() {
		if (idPlace == null)
			throw new RuntimeException("ID �� ���������");
		return fields[idPlace];
	}
	
	public void setId(Object id) {
		if (idPlace == null)
			throw new RuntimeException("ID �� ���������");
		fields[idPlace] = id;
	}
	public String getRowId() {
		return rowId;
	}
	
	   /**� ����������� � ������� ���� �������� columnNames �����  �������� �� ������� ������� ������� rs
	    *  � ��������� ��� ��� ���� � ������� item.  */
	public void pollFieldsFromResultSet(ResultSet rs, String[] columnNames) throws SQLException{		
		if (size() !=  columnNames.length ) 
			throw new RuntimeException("����� ��������� � item �� ��������� � ������ ��������� �������");
			
		for (int i = 0; i<columnNames.length; i ++)
			setVal(i, rs.getObject(columnNames[i]));		
	}
	
	 /**� ����������� � ������� ���� �������� columnNames �����  �������� �� ������� ������� ������� rs
	    *  � ��������� ��� ��� ���� � ������� item. �������� "ROWID"  */
	public void pollFieldsFromRSRowID(ResultSet rs, String[] columnNames, String rowIdAleas) throws SQLException{
		pollFieldsFromResultSet( rs , columnNames);
		rowId = rs.getString(rowIdAleas);		
	}
	
	public int size() {
		return fields.length;
		
	}
	
	@Override
	public String toString() {
		return Arrays.deepToString(fields);
	}	
}