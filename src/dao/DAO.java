package dao;


import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.ResultSetMetaData;
import java.sql.RowId;
import java.sql.SQLException;
import java.sql.Statement;
import java.util.ArrayList;
import java.util.List;


public class DAO {
	private Connection conn;
	private String tableName;
	//private String tablePK; // ��� ���������� �����
	private Integer PKPlace = null; // ������� ���������� ����� c ������� � ������� �����
	private String[] columnNames; // ����� ���� �������� � �������
	private Class[] columnClasses; 
	
	

	public String getTablePK() {
		return columnNames[PKPlace];
	}

	public Class[] getColumnClasses() {
		return columnClasses;
	}

	public String[] getColumnNames() {
		return columnNames;
	}
	
	public String getTableName() {
		return tableName;
	}
	
	public Connection getConnection() {
		return conn;
	}

	private String sqlInsert; //1
	private String sqlUpdate; //2
	private String sqlUpdateByRowId; //3
	private String sqlSearchById; //4
	private String sqlSearchByRowId;//5
	private String sqlDelete; //6
	private String sqlDeleteByRowId;//7
	private String sqlChangeID; //8

	private PreparedStatement pstmInsert = null;
	private PreparedStatement pstmUpdate = null;
	private PreparedStatement pstmUpdateByRowId = null;
	private PreparedStatement pstmSearchById = null;
	private PreparedStatement pstmSearchByRowId = null;
	private PreparedStatement pstmDelete = null;
	private PreparedStatement pstmDeleteByRowId = null;
	private PreparedStatement pstmChangeID = null;
	private Statement stmt;

	 
	public DAO(Connection conn, String tableName, String PKName) throws SQLException {
		
		this.conn = conn;
		this.tableName = tableName;
		
		// ������ ����� �������� � ���� ��������, �������� ������� ������
		stmt = conn.createStatement(); 
		ResultSet rs = stmt.executeQuery("select * from " +tableName + " where 1<>1");
		try{
			stmt = conn.createStatement(); 
			rs = stmt.executeQuery("select * from " + tableName + " where 1<>1");
			ResultSetMetaData metaData = rs.getMetaData();
			columnNames = new String [metaData.getColumnCount()];
			columnClasses = new Class[metaData.getColumnCount()];
						
			for(int i = 0; i < columnNames.length; i++){
				columnNames[i] = metaData.getColumnName(i+1);
				if (columnNames[i].equalsIgnoreCase(PKName))
					PKPlace = i;
				try {
					columnClasses[i] = Class.forName(metaData.getColumnClassName(i+1));
				} catch (ClassNotFoundException e) {
					e.printStackTrace();
				}
			}
			
		}
		finally {	rs.close();}
		
		//�������� ������� ��� sql-�������� � ������������� ��		
		String [] colls = columnNames; 
		//1. ���������� sqlInsert; 
		sqlInsert = "INSERT INTO " + tableName  + " (";
		for(int i = 1; i < colls.length; i ++)
			sqlInsert += colls[i]+ ", ";
		sqlInsert += colls[0] + ") ";		   
		sqlInsert += " VALUES ("; 
		for(int i = 1; i < colls.length; i ++)
			sqlInsert += "?, ";
		sqlInsert+="?)";
		pstmInsert = conn.prepareStatement(sqlInsert);

		//2 ���������� sqlUpdate
		if (PKPlace != null){
			sqlUpdate = "UPDATE " + tableName + " SET ";
			for(int i = 0; i<colls.length; i++){
				if (i == PKPlace.intValue())
					continue;			
				sqlUpdate += colls[i] + " = ?, ";
				
			}
			sqlUpdate = sqlUpdate.substring(0, sqlUpdate.length()-2); // ������� ������ ������� 
			sqlUpdate += " WHERE " + colls[PKPlace] + " = ?";
			pstmUpdate = conn.prepareStatement(sqlUpdate);
		}
		
		//3 ���������� sqlUpdateByRowId
		sqlUpdateByRowId = "UPDATE " + tableName + " SET ";
		for(int i = 0; i<colls.length; i++)
			sqlUpdateByRowId += colls[i] + " = ?, ";
		sqlUpdateByRowId = sqlUpdateByRowId.substring(0, sqlUpdateByRowId.length()-2); // ������� ������ ������� 
		sqlUpdateByRowId += " WHERE ROWID = ?";
		pstmUpdateByRowId = conn.prepareStatement(sqlUpdateByRowId);

		//4 ���������� sqlSearchById
		if (PKPlace != null){
			sqlSearchById = "SELECT ";
			for(int i = 0; i<colls.length; i++)
				sqlSearchById += colls[i]+ ", ";
			sqlSearchById +=  " ROWID "; 
			sqlSearchById += " FROM " + tableName + " WHERE " + colls[PKPlace] + " = ?";
			pstmSearchById = conn.prepareStatement(sqlSearchById);
		}
		
		//5 ���������� sqlSearchByRowId
		sqlSearchByRowId = "SELECT ";
		for(int i = 0; i<colls.length; i++)
			sqlSearchByRowId += colls[i]+ ", ";
		sqlSearchByRowId +=  " ROWID "; 
		sqlSearchByRowId += " FROM " + tableName + " WHERE ROWID = ?";
		pstmSearchByRowId = conn.prepareStatement(sqlSearchByRowId);


		//6 ���������� sqlDelete
		if (PKPlace != null){
			sqlDelete = "DELETE FROM " + tableName + " WHERE " + colls[PKPlace] + " = ?";
			pstmDelete = conn.prepareStatement(sqlDelete);	 
		}
		
		//7 ���������� sqlDeleteByRowID
		sqlDeleteByRowId = "DELETE FROM " + tableName + " WHERE ROWID = ?";
		pstmDeleteByRowId = conn.prepareStatement(sqlDeleteByRowId);
		
		// 8 ���������� sqlChangeID
		if (PKPlace != null){
			sqlChangeID = "UPDATE " + tableName + " SET " + colls[PKPlace]  +" = ? " + " WHERE " + colls[PKPlace] + " = ? ";
			pstmChangeID = conn.prepareStatement(sqlChangeID);
		}
		
		stmt = conn.createStatement();
		
		System.out.println("sqlInsert " + sqlInsert);
		System.out.println("sqlUpdate " + sqlUpdate);
		System.out.println("sqlUpdateByRowId " +sqlUpdateByRowId);
		System.out.println("sqlSearchById " + sqlSearchById);
		System.out.println("sqlSearchByRowId " + sqlSearchByRowId);
		System.out.println("sqlDelete " + sqlDelete);
		System.out.println("sqlDeleteByRowId " + sqlDeleteByRowId);
		System.out.println("sqlChangeID " + sqlChangeID);

		
	}
	 
	

	/** ���� �� �������� ID ���� ������ � ���� ������ � ������ ����� ������ item, ���� �������� ��������� ���������� �� ���� ������ */
	public Item searchById(Object id) throws SQLException {		
		Item result = null;		
		pstmSearchById.setObject(1, id);
		ResultSet rs = null;
		try{
			rs = pstmSearchById.executeQuery();
			if (rs.next()){
				result = new Item(columnNames.length, PKPlace);
				result.pollFieldsFromRSRowID(rs, columnNames, "ROWID" );
			}
			return result;
		}
		finally{rs.close();}		
	
	}
	
	/** ���� �� �������� ROWID ���� ������ � ���� ������ � ������ ����� ������ item, ���� �������� ��������� ���������� �� ���� ������ */
	public Item searchByRowId(String rowid) throws SQLException {		
		Item result = null;		
		pstmSearchByRowId.setObject(1, rowid);
		ResultSet rs = null;
		try{
			rs = pstmSearchByRowId.executeQuery();
			if (rs.next()){
				result = new Item(columnNames.length, PKPlace);
				result.pollFieldsFromRSRowID(rs, columnNames, "ROWID");
			}
			return result;
		}
		finally{rs.close();}		
	
	}

		/** ��������� �������� ����� ������� item, ���� �� �� ���� ������. ���������� true ���� ���������� ������ ������� */
	public boolean refresh(Item item) throws SQLException {		
		pstmSearchById.setObject(1, item.getId());
		ResultSet rs = null;
		try{
			rs = pstmSearchById.executeQuery();
			if (rs.next()){
				item.pollFieldsFromRSRowID(rs, columnNames, "ROWID");
				return true;
			}
			return false;
		}
		finally{rs.close();}
		
	}
	
	/** ��������� �������� ����� ������� item, ���� �� �� ���� ������ �� rowId. ���������� true, ���� ���������� ������ ������� */
	public boolean refreshByRowID(Item item) throws SQLException {		
		pstmSearchByRowId.setObject(1, item.getRowId());
		ResultSet rs = null;
		try{
			rs = pstmSearchByRowId.executeQuery();
			if (rs.next()){
				item.pollFieldsFromRSRowID(rs, columnNames, "ROWID");
				return true;
			}
			return false;
		}
		finally{rs.close();}
		
	}

	/** ������� �� ID ������ � ���� ������� ��. ���� �� ������� - ���������� false*/
	public boolean  delete(Item item) throws SQLException{
		pstmDelete.setObject(1, item.getId());
		if (pstmDelete.executeUpdate() != 0)
			return true;
		return false;
	}
	
	/**������� �� RowID ������ � ���� � ������� ��. ���� �� ������� - ���������� false*/
	public boolean  deleteByRowId(Item item) throws SQLException{
		pstmDeleteByRowId.setObject(1, item.getRowId());
		if (pstmDeleteByRowId.executeUpdate() != 0)
			return true;
		return false;
	}
	
	
		
	/**���������� �������� ����� item � ���� ������ � ���� ����� �������. ��������� �������� �� Item ��� ���� � �.� � null-��������, 
	 *������-�� �� �����, ����� storeNew2 ����� �������� � null -����������, ���� ���������*/
	public void storeNew(Item item) throws SQLException{			
		for (int i = 0; i < columnNames.length ; i++)	{			
			pstmInsert.setObject(i, item.getVal(i));			
		}
		pstmInsert.setObject(columnNames.length, item.getVal(0));		
		pstmInsert.executeUpdate();
		 
	}
	
	/**���������� �������� ����� item � ���� ������ � ���� ����� �������, �� ��� ���� �� ����, ������� ����� null ����������.
	 * ��� ����� ������ ��� ����������  PreparedStatement*/
	public void storeNew2(Item item )   throws SQLException{
		// �������� ������ ��� ��������, ������� �� �����
		List<Integer> notEmptycols = new ArrayList<>();
		for(int i = 0; i < columnNames.length; i ++){
			Object val = item.getVal(i);
			if(val != null && !(val instanceof String && ((String)val).matches("\\s*")))
				notEmptycols.add(i);
		}
		
		StringBuilder sql = new StringBuilder();
		sql.append("INSERT INTO " + tableName  + " (");
		for (int i : notEmptycols){
			sql.append(columnNames[i] );
			sql.append(", ");
		}
		sql.delete(sql.length()-2 , sql.length());
		
		sql.append( ") VALUES ( ");	
		
		for (int i : notEmptycols)
			sql.append("?, " );		
		sql.delete(sql.length()-2 , sql.length());
		sql.append(")");
		System.out.println(sql);
		PreparedStatement pstm = null;
		try{
			pstm  = conn.prepareStatement(sql.toString());
			int k =0;
			for (int i : notEmptycols){
				Object val = item.getVal(i);
				System.out.println( (k+1) + val.toString());
				pstm.setObject(k+1, val);
				k++;
			}

			pstm.executeUpdate();
		} finally {pstm.close();}
	}
	
	/**������� �� �������� iD ������ � ���� ������ � ������������ � ��� �������� ����� ��  item  */
	public int store(Item  item)   throws SQLException
	{	
		for (int i = 0, j = 0; i < columnNames.length ; i++){
			if(i == PKPlace.intValue())
				continue;
			pstmUpdate.setObject(j+1, item.getVal(i));
			j++;
		}
		pstmUpdate.setObject(columnNames.length , item.getId());
		return  pstmUpdate.executeUpdate();
	}
	
	/**������� �� �������� rowiD ������ � ���� ������ � ������������ � ��� �������� ����� ��  item  */
	public int storebyRowId(Item  item)   throws SQLException
	{	
		for (int i = 0; i < columnNames.length ; i++)	{			
			pstmUpdateByRowId.setObject(i+1, item.getVal(i));			
		}
		pstmUpdateByRowId.setString(columnNames.length+1, item.getRowId());
		return  pstmUpdateByRowId.executeUpdate();
	}
	
	
	/**������ Id ������ � ���� */
	public int changeID(Object oldID , Object newID )   throws SQLException
	{	
		pstmChangeID.setObject(1, newID );
		pstmChangeID.setObject(2, oldID );
		return pstmChangeID.executeUpdate();
	}
	
	public Item getEmptyItem(){
		Item item = new Item(columnNames.length, PKPlace);
		item.dao = this;		
		return item;
	}
	
	public int getFieldIndex(String name){
		for (int i = 0 ; i <  columnNames.length; i++){
			if (columnNames[i].equals(name))
			return i;
		}
		throw new RuntimeException(" �� ������ ������� " + name);	
	}
	
	 public void close() {
		   try
		   {			   
			   if (pstmInsert != null)			pstmInsert.close();
			   if (pstmUpdate != null)			pstmUpdate.close();
			   if (pstmUpdateByRowId != null)	pstmUpdateByRowId.close();
			   if (pstmSearchById != null)		pstmSearchById.close();
			   if (pstmSearchByRowId != null)	pstmSearchByRowId.close();
			   if (pstmDelete != null)			pstmDelete.close(); 
			   if (pstmDeleteByRowId != null)	pstmDeleteByRowId.close();
			   if (pstmChangeID != null)		pstmChangeID.close();				
			   if (stmt != null)				stmt.close();			   
		   }
		   catch (Exception e)
		   {
				e.printStackTrace();
				
		   }


	 }
	
}
