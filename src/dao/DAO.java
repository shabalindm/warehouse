package dao;


import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;


public class DAO {
	private Connection conn;
	private String tableName;
	private String tablePK; // ��� ���������� �����
	private String[] columnNames; // ����� ���� �������� � �������
	private Class[] columnClassNames; 

	private String sqlInsert;
	private String sqlUpdate;
	private String sqlSearchById;
	private String sqlDelete;

	private PreparedStatement pstmInsert = null;
	private PreparedStatement pstmUpdate = null;
	private PreparedStatement pstmSearchById = null;
	private PreparedStatement pstmDelete = null;
	 
	public DAO(Connection conn, String tableName, String tablePK) throws SQLException {
		
		this.conn = conn;
		this.tableName = tableName;
		this.tablePK = tablePK;
		
		// ������ ����� ��������, �������� ������� ������
		Statement stmt = conn.createStatement(); 
		ResultSet rs = stmt.executeQuery("select * from " +tableName + " where 1<>1");
		try{
			stmt = conn.createStatement(); 
			rs = stmt.executeQuery("select * from " + tableName + " where 1<>1");
			columnNames = new String [rs.getMetaData().getColumnCount()];
			columnNames[0] = tablePK;
			
			for(int i = 0, j = 1; i < columnNames.length; i++){
				String column = rs.getMetaData().getColumnName(i+1);
				if (!column.equals(tablePK)){
					columnNames[j] = column;
					j++;
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

		//���������� sql pstmSearchById
		sqlSearchById = "SELECT ";
		for(int i = 0; i<colls.length; i++)
			sqlSearchById += colls[i]+ ", ";
		sqlSearchById = sqlSearchById.substring(0, sqlSearchById.length()-2); // ������� ������ ������� 
		sqlSearchById += " FROM " + tableName + " WHERE " + colls[0] + " = ?";

		//���������� sql pstmDelete
		sqlDelete = "DELETE FROM " + tableName + " WHERE " + colls[0] + " = ?";

		// �������������� �������
		pstmInsert = conn.prepareStatement(sqlInsert);
		pstmUpdate = conn.prepareStatement(sqlUpdate);
		pstmSearchById = conn.prepareStatement(sqlSearchById);
		pstmDelete = conn.prepareStatement(sqlDelete);		
		
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

		item.setId(rs.getString(columnNames[0]));
		for (int i = 1; i<columnNames.length; i ++)
			item.setField(i, rs.getString(columnNames[i]));		

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
		pstmSearchById.setString(1, item.getId());
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
		pstmDelete.setString(1, item.getId());
		if (pstmDelete.executeUpdate() != 0)
			return true;
		return false;
		
	}
	
	
	/** ����� �������� ����� �� Item  ��������� �� � ������ (��������������) � pstmInsert ��� pstmUpdate. �����, ����� ����� ������������� � pstm 
	 * ��������� ���������� ����� � item, � �� ������� �������� � �������� �������� � defaultColumnNames
	 * @throws SQLException */
	void bindFields( Item item, PreparedStatement pstm) throws SQLException {
		
		for (int i = 1; i < columnNames.length ; i++)	{		
			pstm.setString(i, item.getField(i) != null ? item.getField(i).toString() : null);
			System.out.println(item.getField(i) != null ? item.getField(i).toString() : null);
		}
		pstm.setString(columnNames.length , item.getId());
	}
		
	/**���������� �������� ����� item � ���� ������ � ���� ����� ������� */
	public int storeNew(Item item )   throws SQLException
	{	
		bindFields(item, pstmInsert);
		return pstmInsert.executeUpdate();
	}
	
	/**������� �� �������� iD ������ � ���� ������ � ������������ � ��� �������� ����� ��  item  */
	public int store(Item  item)   throws SQLException
	{	
		bindFields(item, pstmUpdate);
		System.out.println(sqlUpdate);
		return pstmUpdate.executeUpdate();
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
