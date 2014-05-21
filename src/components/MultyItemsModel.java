package components;

import java.sql.SQLException;

import dao.DAO;
import dao.Item;


public abstract class MultyItemsModel extends AbstractItemsTableModel<Item[]>{ 
	
	/* Каждая сточка этой модели представляет собой массив типа Item[]. 
	 * Поэтому в этой строчке получается двухуровневая адресация:
	 * 	
	  						  Item[0]  					      |            Item[1] 
	* row:     | _field0_ | _field1_ | _field2_ | _field3_     |     _field4_ | _field5_ |  _field6_ | 
	* index2:	    0          1          2           3                  0          1          2    
	* index1:                     -0-                                              -1-                              */
	
	protected DAO [] daos;
	protected int [][] columnsMap;
	
	/**По номеру столбца находит индекс Item[], который представляет строку модели */
	private int index1 (int columnIndex){
		return columnsMap[columnIndex][0];		
	}
	
	/**По номеру столбца находит номер Item из массива Item[], который представляет строку модели */
	private int index2 (int columnIndex){
		return columnsMap[columnIndex][1];		
	}

	
	@Override
	public int getColumnCount() {
		// TODO Auto-generated method stub
		return columnsMap.length;
	}
	
	@Override
	public Object getValueAt(int rowIndex, int columnIndex) {
		Item [] items = getRow(rowIndex);
		Item item = items[index1(columnIndex)];
		return  item.getVal(index2(columnIndex));
		
	}
	
	
	@Override
	Item[] getRowFromResultSet(int rowNumber) {
		Item[] row = getEmptyRow();		
		try {
			rs.absolute(rowNumber+1);
			for (int i = 0; i <row.length ; i++ ){
				row[i].pollFieldsFromResultSet(rs, daos[i].getColumnNames());
			}
		} catch (SQLException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
		return row;
	}
	@Override
	public void setValueAt(Object aValue, int rowIndex, int columnIndex) {
		Item [] items = getRow(rowIndex);
		Item item = items[index1(columnIndex)];
		
		if(aValue instanceof String && ((String)aValue).matches("\\s*"))
			aValue = null;
		
		Object oldValue = getValueAt(rowIndex, columnIndex);			
		if(aValue == null && aValue == oldValue ||  aValue != null && aValue.equals(oldValue) ){
		// считаем что значение на самом деле не изменилось 
		} else
				markCellModified(rowIndex, columnIndex); 
		
		item.setVal(index2(columnIndex), aValue);
		
	}
	@Override
	protected  Item[] getEmptyRow() {
		Item[] row = new Item[daos.length];
		for (int i = 0; i <row.length ; i++ ){
			row[i] = daos[i].getEmptyItem();
		}
		return row;
		
	}
	
	@Override
	public boolean isCellEditable(int rowIndex, int columnIndex) {			
		return true;
	}	
	
	
	@Override
	public String getColumnName(int column) {
		// TODO Auto-generated method stub
		return daos[index1(column)].getColumnNames()[index2(column)];
	}

	@Override
	public Class<?> getColumnClass(int columnIndex) {
		// TODO Auto-generated method stub
		return daos[index1(columnIndex)].getColumnClasses()[index2(columnIndex)];
	}
	
	public void close(){
		for(DAO dao:daos)
			dao.close();
		super.close();
	}

}
