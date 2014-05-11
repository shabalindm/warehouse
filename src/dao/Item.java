package dao;

import java.sql.ResultSet;
import java.sql.SQLException;

/**Содержит объектное представления записи из базы. Эта запись состоит из подзаписей, 
 * которые могут содержать как полноценую запись из другой таблицы, так и только одно значение ID. 
 * Нумерация полей начинается с нуля (0-е поле  - это id)*/
public class Item {
	private String rowId;
	private Object id; 
	private Item [] otherFields;
	
	public Item(Object id) {
		super();
		this.id = id;
	}
	
	public Item(Object id, int lenght) {
		this.id = id;
		if (lenght > 1){
			setSize(lenght);
		}
	}
	
	/**i = 1, 2 ...*/
	public Item getItem(int i){
		return otherFields[i-1];
	}
	
	 /** i = 1, 2 */
	public void setItem(int i, Item item){
		otherFields[i-1]= item;
	}
	
	/** i = 0, 1 ... */
	public Object getVal(int i){
		if (i == 0)
			return id;
		else if (getItem(i) == null)
			return null;
		else return getItem(i).id;
		
	}
	
	/** i = 0, 1 .. */
	public void setVal(int i, Object val){
		if (i == 0)
			 id = val;
		else  if (val != null){
			Item item = new Item(val);			
			setItem(i, item);
		}	
		else {
			setItem(i, null);			
		}
	}
	
	public Object getId() {
		return id;
	}
	
	public void setId(Object id) {
		this.id = id;
	}
	public String getRowId() {
		return rowId;
	}
	
	   /**В соответсвие с набором имен столбцов columnNames берет  значения из текущей позиции курсора rs
	    *  и заполняет ими все поля в объекте item.  */
	public void pollFieldsFromResultSet(ResultSet rs, String[] columnNames) throws SQLException{
		if ( otherFields == null)
			setSize(columnNames.length);
		
		else if (size() !=  columnNames.length ) 
			throw new RuntimeException("Число элементов в item не совпадает с числом назаваний колонок");
			
		for (int i = 0; i<columnNames.length; i ++)
			setVal(i, rs.getObject(columnNames[i]));		
	}
	
	 /**В соответсвие с набором имен столбцов columnNames берет  значения из текущей позиции курсора rs
	    *  и заполняет ими все поля в объекте item. Включает "ROWID"  */
	public void pollFieldsFromRSRowID(ResultSet rs, String[] columnNames) throws SQLException{
		if ( otherFields == null)
			setSize(columnNames.length);
		
		else if (size() !=  columnNames.length ) 
			throw new RuntimeException("Число элементов в item не совпадает с числом назаваний колонок");
			
		for (int i = 0; i<columnNames.length; i ++)
			setVal(i, rs.getObject(columnNames[i]));
		rowId = rs.getString("ROWID");		
	}
	
	private int size() {
		if (otherFields == null)
			return 1;
		return otherFields.length+1;
		
	}
	
	/***/
	 public void setSize(int lenght){
		 otherFields = new Item[lenght-1];
	 }
	
	
	@Override
	public String toString() {
		if (id == null)
			return  "";
		return id.toString();
	}	
}