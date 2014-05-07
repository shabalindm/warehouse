package dao;


import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.ResultSetMetaData;
import java.sql.SQLException;
import java.sql.Statement;


public class DAO {
	private Connection conn;
	private String tableName;
	private String tablePK; // ��� ���������� �����
	private String[] columnNames; // ����� ���� �������� � �������
	private Class[] columnClasses; 

	private String sqlInsert;
	private String sqlUpdate;
	private String sqlSearchById;
	private String sqlDelete;
	private String sqlChangeID;

	private PreparedStatement pstmInsert = null;
	private PreparedStatement pstmUpdate = null;
	private PreparedStatement pstmSearchById = null;
	private PreparedStatement pstmDelete = null;
	private PreparedStatement pstmChangeID = null;
	
	 
	public DAO(Connection conn, String tableName, String tablePK) throws SQLException {
		
		this.conn = conn;
		this.tableName = tableName;
		this.tablePK = tablePK;
		
		// ������ ����� �������� � ���� ��������, �������� ������� ������
		Statement stmt = conn.createStatement(); 
		ResultSet rs = stmt.executeQuery("select * from " +tableName + " where 1<>1");
		try{
			stmt = conn.createStatement(); 
			rs = stmt.executeQuery("select * from " + tableName + " where 1<>1");
			ResultSetMetaData metaData = rs.getMetaData();
			columnNames = new String [metaData.getColumnCount()];
			columnClasses = new Class[metaData.getColumnCount()];
			
			columnNames[0] = tablePK;
						
			for(int i = 0, j = 1; i < columnNames.length; i++){
				String column = metaData.getColumnName(i+1);
				Class colClass= null;
				try {
					 colClass = Class.forName(metaData.getColumnClassName(i+1));
				} catch (ClassNotFoundException e) {
					e.printStackTrace();
				}
				if (!column.equals(tablePK)){
					columnNames[j] = column;
					columnClasses[j] = colClass;
					j++;
				}
				else{
					columnNames[0] = column;
					columnClasses[0] = colClass;
				}
			}
			
		}
		finally {	rs.close();		stmt.close();}
		
		//�������� ������� ��� sql-��������		
		String [] colls = columnNames; 
		//���������� sqlInsert;
		sqlInsert = "INSERT INTO " + tableName  + " (";
		for(int i = 1; i < colls.length; i ++)
			sqlInsert += colls[i]+ ", ";
		sqlInsert += colls[0] + ") ";		   
		sqlInsert += " VALUES ("; 
		for(int i = 1; i < colls.length; i ++)
			sqlInsert += "?, ";
		sqlInsert+="?)";		

		// ���������� sqlUpdate
		sqlUpdate = "UPDATE " + tableName + " SET ";
		for(int i = 1; i<colls.length; i++)
			sqlUpdate += colls[i] + " = ?, ";
		sqlUpdate = sqlUpdate.substring(0, sqlUpdate.length()-2); // ������� ������ ������� 
		sqlUpdate += " WHERE " + colls[0] + " = ?";

		//���������� sqlSearchById
		sqlSearchById = "SELECT ";
		for(int i = 0; i<colls.length; i++)
			sqlSearchById += colls[i]+ ", ";
		sqlSearchById = sqlSearchById.substring(0, sqlSearchById.length()-2); // ������� ������ ������� 
		sqlSearchById += " FROM " + tableName + " WHERE " + colls[0] + " = ?";

		//���������� sqlDelete
		sqlDelete = "DELETE FROM " + tableName + " WHERE " + colls[0] + " = ?";
		
		// //���������� sqlChangeID
		sqlChangeID = "UPDATE " + tableName + " SET " + tablePK +" = ? " + " WHERE " + tablePK +" = ? ";

		// �������������� �������
		pstmInsert = conn.prepareStatement(sqlInsert);
		pstmUpdate = conn.prepareStatement(sqlUpdate);
		pstmSearchById = conn.prepareStatement(sqlSearchById);
		pstmDelete = conn.prepareStatement(sqlDelete);		
		pstmChangeID = conn.prepareStatement(sqlChangeID);	
	}
	 
	public String[] getColumnNames() {
		return columnNames;
	}
	public Class[] getColumnClassNames() {
		return columnClasses;
	}
	public String getTableName() {
		return tableName;
	}
	public Connection getConnection() {
		return conn;
	}
	 
	   /**�����  �������� �� ������� ������� ������� rs � ��������� ��� ��� ���� � ������� item. 
	    * ������� ���������� ���� ������ � ������� �������� � �������������� ������. ���� �� null, �� ������� ColumnNames �� ���;
	    ** � ���� ���� �������� item � ��� �� ������������� ���� �����, ��������� �� ��������� */
	public void pollFieldsFromResultSet(Item item, ResultSet rs, String[] columnNames)
			throws SQLException{

		if (columnNames == null)
			columnNames = this.columnNames;
		if (item.getOtherCols() == null)
			item.setOtherCols(new Item[columnNames.length - 1]);

		item.setId(rs.getObject(columnNames[0]));
		for (int i = 1; i<columnNames.length; i ++)
			item.setField(i, rs.getObject(columnNames[i]));	 	

	}

	/** ���� �� �������� ID ���� ������ � ���� ������ � ������ ����� ������ item, ���� �������� ��������� ���������� �� ���� ������ */
	public Item searchById(Long id) throws SQLException {		
		Item result = null;		
		pstmSearchById.setLong(1, id);
		ResultSet rs = null;
		try{
			rs = pstmSearchById.executeQuery();
			if (rs.next()){
				result = new Item();
				pollFieldsFromResultSet(result, rs, null);
			}
		}
		finally{rs.close();}		
		return result;
	}

		/** ��������� �������� ����� ������� item, ���� �� �� ���� ������. ���������� true ���� ���������� ������ ������� */
	public boolean refresh(Item item) throws SQLException {		
		pstmSearchById.setObject(1, item.getId());
		ResultSet rs = null;
		try{
			rs = pstmSearchById.executeQuery();
			if (rs.next()){
				pollFieldsFromResultSet(item, rs, null);
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
	
	
	/** ����� �������� ����� �� Item  ��������� �� � ������ (��������������) � pstmInsert ��� pstmUpdate. �����, ����� ����� ������������� � pstm 
	 * ��������� ���������� ����� � item, � �� ������� �������� � �������� �������� � defaultColumnNames
	 * @throws SQLException */
	void bindFields( Item item, PreparedStatement pstm) throws SQLException {
		
		for (int i = 1; i < columnNames.length ; i++)	{			
//			System.out.print(item.getField(i).getId());
//			try{ System.out.println(" " + item.getField(i).getId().getClass().getSimpleName());
			if (item.getField(i) == null)
				pstm.setObject(i, null);
			else
				pstm.setObject(i, item.getField(i).getId());
		
		pstm.setObject(columnNames.length, item.getId());}
	}
		
	/**���������� �������� ����� item � ���� ������ � ���� ����� ������� */
	public void storeNew(Item item )   throws SQLException
	{	
		bindFields(item, pstmInsert);	
		pstmInsert.executeUpdate();
		 
	}
	
	/**������� �� �������� iD ������ � ���� ������ � ������������ � ��� �������� ����� ��  item  */
	public int store(Item  item)   throws SQLException
	{	
		bindFields(item, pstmUpdate);
		return pstmUpdate.executeUpdate();
	}
	
	/**������ Id ������ � ���� */
	public int changeID(Object oldID , Object newID )   throws SQLException
	{	
		pstmChangeID.setObject(1, newID );
		pstmChangeID.setObject(2, oldID );
		return pstmChangeID.executeUpdate();
	}
	
	
	 public void close() {
		   try
		   {			   
			   if (pstmInsert != null)
				   pstmInsert.close();

			   if (pstmUpdate != null)
				   pstmUpdate.close();

			   if (pstmSearchById != null)
				   pstmSearchById.close();
			   
			   if(pstmDelete != null)
				   pstmDelete.close();
		   }
		   catch (SQLException e)
		   {
			   e.printStackTrace();
		   }


	 }
	
}
