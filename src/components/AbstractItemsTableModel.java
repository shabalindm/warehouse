package components;

import java.awt.Toolkit;
import java.awt.datatransfer.Clipboard;
import java.awt.datatransfer.DataFlavor;
import java.awt.datatransfer.Transferable;
import java.sql.Connection;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Savepoint;
import java.sql.Statement;
import java.text.DateFormat;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Formatter;
import java.util.Iterator;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;
import java.util.NavigableMap;
import java.util.Set;
import java.util.TreeMap;
import java.util.TreeSet;

import javax.swing.table.AbstractTableModel;

public abstract class AbstractItemsTableModel<T> extends AbstractTableModel {	
	private Map<Integer, List<T>>  newRows = new TreeMap<>();
	private Set<Integer> deletedRows = new TreeSet<>();
	private Map<Integer, Set<Integer>> modifiedSells = new TreeMap<>(); 
	private NavigableMap<Integer, Integer>  shiftMap = new TreeMap<>();	
	private Map<Integer, T> cache  = new TreeMap<>();
	
	protected int rowCount;
	private String whereCond = "";
	protected Connection conn;
	private MessageListener listener;
	private StateListener stateListener;	
	protected ResultSet rs;
	
/**   ������������� ��������� ���������, ������� ��������� ��� ������ � �������*/
	public void setMessageListener(MessageListener listener){
		this.listener = listener;
	}
	
	public MessageListener getMessageListener(){
		 return listener;
	}
	
/**   �������� ���������  (���� �� ����������) ���������*/
	protected void announce(String msg){
		if (listener != null)
			listener.setText(msg);		
	}
	
	/**   ������������� ��������� ��������� ���������� (commited)*/
	public void setStateListener(StateListener stateListener){
		this.stateListener = stateListener;
	}
	
/**   �������� ���������  (���� �� ����������) ���������*/
	protected void setState(boolean state){
		if (stateListener != null)
			stateListener.setState(state);		
	}
	
	public String getWhereCond() {
		return whereCond;
	}

	public void setWhereCond(String whereCond) {
		this.whereCond = whereCond;
	}

	/**������������ SQL ������, �� �������� ������ ����� ����� ������ �� ����*/
	protected abstract String getSQL();
	
	/** ���������� �������� � ����, ��������� ������ ������, ������� �����, ����������
	 *  ����� ����� � ��� � ��������� �������� ���� rowCount*/
	public void updateData() {
			Statement stmt = null;
			String sql = getSQL()+ " " + whereCond; 
			System.out.println(sql);
			try{
				stmt = conn.createStatement(ResultSet.TYPE_SCROLL_INSENSITIVE, ResultSet.CONCUR_READ_ONLY);
				ResultSet newrs = stmt.executeQuery(sql);
				try {rs.close();} 
				catch (Exception e){} // ��������� ����������
				rs = newrs;
				// ������ ���������� �����
				if (!rs.last())
					rowCount = 0;
				else
					rowCount =  rs.getRow();
				rs.beforeFirst();				
				// ������� ������ ������
				cache.clear();
				newRows.clear();
				deletedRows.clear();
				shiftMap.clear();
				modifiedSells.clear();
				shiftMap.put(-1, 0);
				announce("���������");
			} catch (SQLException e) {
				// TODO Auto-generated catch block
				announce(e.getMessage());
				e.printStackTrace();
			}		

		finally{ fireTableDataChanged();}
	}
	
/**��������� �� ��������������� ������ ������ �����������*/
	abstract T getRowFromResultSet(int rowNumber); 
	
/** �������� ������� �� ���� �� ������� ����, ���, ���� �� ��� ���, ���������� � ��� � ������ */
	private T getRowFromCache(int rowIndex){
		T row = cache.get(rowIndex);
		if (row == null){
			row = getRowFromResultSet(rowIndex);
			cache.put(rowIndex, row);
			}
		return row;
			
	}
	
/**������� ������� (����� ������ � �����) �� ������� ������*/ 
	public T getRow(int rowIndex){
		Entry<Integer, Integer> floor = shiftMap.floorEntry(rowIndex);		
		if (floor.getValue() <= 0) // ������������e ����� - ������� ����, ������� ����� ������ ����� ������
			return getRowFromCache(rowIndex + floor.getValue());
		Entry<Integer, Integer> higher = shiftMap.higherEntry(rowIndex);
		int baseRowNum = higher.getKey() + higher.getValue() -1;
		int localIndex = floor.getValue() - 10 - baseRowNum + rowIndex - floor.getKey();
		return newRows.get(baseRowNum).get(localIndex);			
	}

	public boolean isNewRow(int rowIndex){
		return shiftMap.floorEntry(rowIndex).getValue() > 0;
	}
	
	public boolean isUpdatedSell(int rowIndex, int columnIndex ){
		int shift = shiftMap.floorEntry(rowIndex).getValue(); // shift > 0 -  ������� ����, ��� ������� ��������� ����� ����� �����
		if (shift > 0)
			return false;
		Set<Integer> row = modifiedSells.get(rowIndex + shift);
		if (row !=null)
			return row.contains(columnIndex);
		return false;	
	}
	
	// ������������
	public static void main(String ... args){
		AbstractItemsTableModel<String> model = new AbstractItemsTableModel<String>() {
			

			@Override
			public int getColumnCount() {
				// TODO Auto-generated method stub
				return 0;
			}

			@Override
			public Object getValueAt(int rowIndex, int columnIndex) {
				// TODO Auto-generated method stub
				return null;
			}

			@Override
			protected String getSQL() {
				// TODO Auto-generated method stub
				return null;
			}

			
			@Override
			void deleteFromDB(String item) throws SQLException {
				// TODO Auto-generated method stub
				
			}

			@Override
			protected
			String getEmptyRow() {
				// TODO Auto-generated method stub
				return "New";
			}

			@Override
			String getRowFromResultSet(int rsRowNumber) {
				// TODO Auto-generated method stub
				return null;
			}

			@Override
			public void setValueAt(Object aValue, int rowIndex, int columnIndex) {
				// TODO Auto-generated method stub
				
			}

			@Override
			void insertIntoDB(String modelRow) throws SQLException {
				// TODO Auto-generated method stub
				
			}

			@Override
			void updateInDB(String modelRow) throws SQLException {
				// TODO Auto-generated method stub
				
			}
		};
		
		model.cache.put(0, "A");
		model.cache.put(1, "B");
		model.cache.put(2, "C");
		model.cache.put(3, "D");
		model.cache.put(4, "E");
		model.cache.put(5, "F");		
		model.rowCount = 6;
		model.updateShiftMap();
		
		System.out.println(model);
		System.out.println(model.getModelIndex(0));
		System.out.println(model.newRows);
		System.out.println(model.shiftMap);
		model.addRow(5);		
		System.out.println(model);
		model.addRow(5);		
		System.out.println(model);
		model.addRow(-1);		
		System.out.println(model);
		model.addRow(-1);		
		System.out.println(model);
		model.addRow(0);		
		System.out.println(model);
		model.addRow(-1);		
		System.out.println(model);
		model.addRow(5);	
		System.out.println(model);
		model.addRow(5);	
		System.out.println(model);
		System.out.println(model.getModelIndex(0));
		System.out.println(model.getModelIndex(1));
		System.out.println(model.getModelIndex(2));
		System.out.println(model.getModelIndex(3));
		System.out.println(model.getModelIndex(4));
		System.out.println(model.getModelIndex(5));
		model.deleteRow(0);	
		System.out.println(model);
		model.deleteRow(0);	
		System.out.println(model);
		model.deleteRow(4);	
		System.out.println(model);
		model.deleteRow(10);	
		System.out.println(model);
		System.out.println(model.deletedRows);
		model.deleteRow(8);	
		System.out.println(model);
		System.out.println(model.deletedRows);
		model.deleteRow(7);	
		System.out.println(model);
		System.out.println(model.deletedRows);
		model.unDeleteRow(8);	
		System.out.println(model);
		System.out.println(model.deletedRows);
		
		
	}
	

	/** ��������� ����� ������ ������� �����  rowIndex */
	public void addRow(int rowIndex){
		// ������� � ����� �������� ��� ����� �������� higher � floor, ������� ������������� ������ �� ���� �������: 
		// 
		Entry<Integer, Integer> floor = shiftMap.floorEntry(rowIndex);
		Entry<Integer, Integer> higher = shiftMap.higherEntry(rowIndex);
		if(floor.getValue()<=0 ){
			if(higher == null || rowIndex < higher.getKey()-1){
				//������� ������ �����
				int baseRowNum = rowIndex + floor.getValue();
				List<T> list =  new ArrayList<>();
				list.add(getEmptyRow());
				newRows.put(baseRowNum, list);
				updateShiftMap();
				rowCount++;
				fireTableRowsInserted(rowIndex+1, rowIndex+1);
				return;
				
			}else{//rowIndex == higher.getKey()-1
				floor = higher; 
				higher = shiftMap.higherEntry(higher.getKey());
			}
		}
		// ������� � ��� ��������� ����
		int baseRowNum = higher.getKey() + higher.getValue() -1;
		int localNum = floor.getValue() - 10 - baseRowNum + rowIndex - floor.getKey() +1;
		newRows.get(baseRowNum).add(localNum, getEmptyRow());			
		updateShiftMap();
		rowCount++;
		fireTableRowsInserted(rowIndex+1, rowIndex+1);
		
	}
	
	private void updateShiftMap() {
		shiftMap.clear();
		int currShift= 0;
		shiftMap.put(-1, 0);
		for ( Entry<Integer, List<T>> entry : newRows.entrySet()){
			int size = entry.getValue().size();
			int baseRowNum = entry.getKey();
			int index = baseRowNum + 1 - currShift;
			shiftMap.put(index, baseRowNum + 10);
			currShift -= size;
			shiftMap.put(index + size, currShift);
		}

	}

 /**��������, �������� �� ������� � ������� �� ��������*/
	public boolean isDeletedRow(int rowIndex){
		int shift = shiftMap.floorEntry(rowIndex).getValue(); // shift > 0 -  ������� ����, ��� ������� ��������� ����� ����� �����
		return shift <= 0 && deletedRows.contains(rowIndex + shift);		
	}
	
/** ������� ������� �� ����� �����������, ��� �������� �� ��������, ���� ��� ������� ��� ���� � ����*/
	public void deleteRow(int rowIndex){
		if(rowIndex>=rowCount)
			throw new IndexOutOfBoundsException("rowIndex = " + rowIndex + ", rowCount =  " +  rowCount);
		Entry<Integer, Integer> floor = shiftMap.floorEntry(rowIndex);		
		if (floor.getValue() <= 0){ // ������������e ����� - ������� ����, ������� ����� ������ ����� ������
			 deletedRows.add(rowIndex + floor.getValue());
			 return;
		}
		Entry<Integer, Integer> higher = shiftMap.higherEntry(rowIndex);
		int baseRowNum = higher.getKey() + higher.getValue() -1;
		int localIndex = floor.getValue() - 10 - baseRowNum + rowIndex - floor.getKey();
		List<T> list = newRows.get(baseRowNum);
		list.remove(localIndex);
		if(list.isEmpty())
			newRows.remove(baseRowNum);
		rowCount--;
		updateShiftMap();
		fireTableRowsDeleted(rowIndex, rowIndex);			
	}
	
	/**������� ������� �� ������� �� ��������*/
	public void unDeleteRow(int rowIndex){
		int shift = shiftMap.floorEntry(rowIndex).getValue();
		if (shift<=0)
			deletedRows.remove(rowIndex + shift);		
	}
	
	
	public void markCellModified(int rowIndex, int columnIndex){
		int shift = shiftMap.floorEntry(rowIndex).getValue();
		if (shift > 0)// ������� ��������� ����� �����
			return;
		rowIndex = rowIndex + shift;		
		Set<Integer> set = modifiedSells.get(rowIndex);
		if (set == null){
			set = new TreeSet<>();
			modifiedSells.put(rowIndex, set);
		}
		set.add(columnIndex);	
	}
	
	@Override
	public abstract  void setValueAt(Object aValue, int rowIndex, int columnIndex);
	

	@Override
	public String toString() {
		StringBuilder sb = new StringBuilder();
		Formatter f = new Formatter(sb);
		for (int i = 0 ; i < rowCount; i++ )
			f.format("(%2d%2s) ", i ,  getRow(i) );
		return sb.toString();
	}
	
	@Override
	public int getRowCount() {
		return rowCount;
	}

	protected abstract T getEmptyRow() ;

	abstract void deleteFromDB(T modelRow) throws SQLException ;
	abstract void insertIntoDB(T modelRow) throws SQLException ;
	abstract void updateInDB(T modelRow) throws SQLException ;
	
	/**���������� ���������� �  ����. ���������� ����� �������, �� ������� ��������� ������. ���� ������ �� ����, ���������� -1
	 * @throws WriteDataToDBException */
	public void writeToDB() throws WriteDataToDBException   {
		int rowIndex = -1;
		int localIndex = -1;
		
		Savepoint savepoint = null;
		try {
			 savepoint = conn.setSavepoint("S");
			 
			rowIndex = -1;
			for (int i: deletedRows){
				rowIndex = i;
				deleteFromDB(cache.get(rowIndex));
				modifiedSells.remove(rowIndex);
			}
			
			rowIndex = -1;
			for ( int i :modifiedSells.keySet()){
				rowIndex = i;
				updateInDB(cache.get(rowIndex));
				}
			
			rowIndex = -1;
			for (Entry<Integer,List<T>> entry :newRows.entrySet()){
				rowIndex = entry.getKey();
				localIndex = -1;
				for (int i = 0; i <entry.getValue().size(); i++){
					localIndex =i; 
					insertIntoDB(entry.getValue().get(localIndex));
					}
				
			}
			
			rowIndex = -1;
			localIndex = -1;
			updateData();
				
		} catch (SQLException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
			announce(e.getMessage());
			try {
				conn.rollback(savepoint);
			} catch (SQLException e1) {
				e1.printStackTrace();
			}
			throw new WriteDataToDBException(getModelIndex(rowIndex) + localIndex +1);
		}
		
	} 
	
	/**���� �� ������ ������ � ���� ����� ������, ������� � ��� ����� ������(��������, �������� getRow(int index) */
	private int getModelIndex( int chacheIndex){		
		Iterator<Entry<Integer, Integer>> it =shiftMap.entrySet().iterator();
				while(true){
					Entry<Integer, Integer> first = it.next();
					int upperBorger = rowCount;
					if (it.hasNext())
						upperBorger = it.next().getKey();					
					int modelIndex = chacheIndex -first.getValue();
					if (modelIndex < upperBorger)
						return modelIndex;
				}
	}

	/**���������, ���� �� �� ����������� ������*/
	public boolean isSaved(){
		return newRows.isEmpty() && deletedRows.isEmpty() && modifiedSells.isEmpty();
		
	};
	
	public  void close(){
		try {
			rs.close();
		} catch (SQLException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
	};
	
}
