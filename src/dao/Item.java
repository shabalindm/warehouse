package dao;

import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.Arrays;

/**Содержит объектное представления записи из базы.  
 * Нумерация полей начинается с нуля (0-е поле  - это id)*/
public class Item {
	private String rowId;
	private Integer idPlace = null; 
	private Object [] fields;
	public DAO dao;
	
	
	Item(int lenght, Integer idPlace) {
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
			throw new RuntimeException("ID не определен");
		return fields[idPlace];
	}
	
	public void setId(Object id) {
		if (idPlace == null)
			throw new RuntimeException("ID не определен");
		fields[idPlace] = id;
	}
	public String getRowId() {
		return rowId;
	}
	
	   /**В соответсвие с набором имен столбцов columnNames берет  значения из текущей позиции курсора rs
	    *  и заполняет ими все поля в объекте item.  */
	public void pollFieldsFromResultSet(ResultSet rs, String[] columnNames) throws SQLException{		
		if (size() !=  columnNames.length ) 
			throw new RuntimeException("Число элементов в item не совпадает с числом назаваний колонок");
			
		for (int i = 0; i<columnNames.length; i ++)
			setVal(i, rs.getObject(columnNames[i]));		
	}
	
	 /**В соответсвие с набором имен столбцов columnNames берет  значения из текущей позиции курсора rs
	    *  и заполняет ими все поля в объекте item. Включает "ROWID"  */
	public void pollFieldsFromRSRowID(ResultSet rs, String[] columnNames, String rowIdAleas) throws SQLException{
		pollFieldsFromResultSet( rs , columnNames);
		rowId = rs.getString(rowIdAleas);		
	}
	
	public int size() {
		return fields.length;
		
	}
	
	public void changeID(Object newID) throws SQLException{
		dao.changeID(getId(), newID);
		setId(newID);
		}
	
	public void delete() throws SQLException{
		dao.delete(this);
		}
	
	public void deleteByRowId() throws SQLException{
		dao.deleteByRowId(this);
		}
		
	public void refresh() throws SQLException{
		dao.refresh(this);
		}
	
	public void refreshByRowID() throws SQLException{
		dao.refreshByRowID(this);
		}
	
	public void store() throws SQLException{
		dao.store(this);
		}
	public void storebyRowId() throws SQLException{
		dao.storebyRowId(this);
		}
	public void storeNew2() throws SQLException{
		dao.storeNew2(this);
		}
	
	
	@Override
	public String toString() {
		return Arrays.deepToString(fields);
	}	
}