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
	private String tablePK; // Имя первичного ключа
	private String[] columnNames; // Имена всех столбцов в таблице
	private Class[] columnClasses; 
	
	

	public String getTablePK() {
		return tablePK;
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
	private Statement stmt;

	 
	public DAO(Connection conn, String tableName, String tablePK) throws SQLException {
		
		this.conn = conn;
		this.tableName = tableName;
		this.tablePK = tablePK;
		
		// Узнаем имена столбцов и типы столбцов, выполнив пробный запрос
		stmt = conn.createStatement(); 
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
		finally {	rs.close();}
		
		//Собираем строчки для sql-запросов		
		String [] colls = columnNames; 
		//Подготовка sqlInsert;
		sqlInsert = "INSERT INTO " + tableName  + " (";
		for(int i = 1; i < colls.length; i ++)
			sqlInsert += colls[i]+ ", ";
		sqlInsert += colls[0] + ") ";		   
		sqlInsert += " VALUES ("; 
		for(int i = 1; i < colls.length; i ++)
			sqlInsert += "?, ";
		sqlInsert+="?)";		

		// подготовка sqlUpdate
		sqlUpdate = "UPDATE " + tableName + " SET ";
		for(int i = 1; i<colls.length; i++)
			sqlUpdate += colls[i] + " = ?, ";
		sqlUpdate = sqlUpdate.substring(0, sqlUpdate.length()-2); // удаляем лишнюю запятую 
		sqlUpdate += " WHERE " + colls[0] + " = ?";

		//подготовка sqlSearchById
		sqlSearchById = "SELECT ";
		for(int i = 0; i<colls.length; i++)
			sqlSearchById += colls[i]+ ", ";
		sqlSearchById = sqlSearchById.substring(0, sqlSearchById.length()-2); // удаляем лишнюю запятую 
		sqlSearchById += " FROM " + tableName + " WHERE " + colls[0] + " = ?";

		//подготовка sqlDelete
		sqlDelete = "DELETE FROM " + tableName + " WHERE " + colls[0] + " = ?";
		
		// //подготовка sqlChangeID
		sqlChangeID = "UPDATE " + tableName + " SET " + tablePK +" = ? " + " WHERE " + tablePK +" = ? ";

		// подготавливаем запросы
		pstmInsert = conn.prepareStatement(sqlInsert);
		pstmUpdate = conn.prepareStatement(sqlUpdate);
		pstmSearchById = conn.prepareStatement(sqlSearchById);
		pstmDelete = conn.prepareStatement(sqlDelete);		
		pstmChangeID = conn.prepareStatement(sqlChangeID);	
		
		stmt = conn.createStatement();
	}
	 
	

	/** Ищет по значению ID поле запись в базе данных и выдает новый объект item, поля которого заполнены значениями из этой записи */
	public Item searchById(Object id) throws SQLException {		
		Item result = null;		
		pstmSearchById.setObject(1, id);
		ResultSet rs = null;
		try{
			rs = pstmSearchById.executeQuery();
			if (rs.next()){
				result = new Item(null, columnNames.length);
				result.pollFieldsFromResultSet(rs, columnNames);
			}
			return result;
		}
		finally{rs.close();}		
	
	}

		/** Обновляет значения полей объекта item, беря их из базы данных. Возвращает true если обновление прошло успешно */
	public boolean refresh(Item item) throws SQLException {		
		pstmSearchById.setObject(1, item.getId());
		ResultSet rs = null;
		try{
			rs = pstmSearchById.executeQuery();
			if (rs.next()){
				item.pollFieldsFromResultSet(rs, columnNames);
				return true;
			}
			return false;
		}
		finally{rs.close();}
		
	}

	/** Находит по ID запись в базе удалеет ее. Если не удается - возвращает false*/
	public boolean  delete(Item item) throws SQLException{
		pstmDelete.setObject(1, item.getId());
		if (pstmDelete.executeUpdate() != 0)
			return true;
		return false;
		
	}
	
	
		
	/**Записывает значения полей item в базу данных в виде новой строчки */
	public void storeNew(Item item )   throws SQLException{			
		for (int i = 1; i < columnNames.length ; i++)	{			
			pstmInsert.setObject(i, item.getVal(i));			
		}
		pstmInsert.setObject(columnNames.length , item.getVal(0));
		
		pstmInsert.executeUpdate();
		 
	}
	
	/**Записывает значения полей item в базу данных в виде новой строчки, но при этом prepared statemet не использует */
	public void storeNew2(Item item )   throws SQLException{
		StringBuilder sql = new StringBuilder();
		sql.append("INSERT INTO " + tableName  + " (");
		for(int i = 0; i < columnNames.length; i ++)
			if(item.getVal(i) != null){
				sql.append(columnNames[i]);
				sql.append(", ");
			}
		sql.delete(sql.length()-2 , sql.length());
		
		sql.append( ") VALUES ( ");		   
		for(int i = 0; i < columnNames.length; i ++)
			if(item.getVal(i) != null){
				sql.append("'");
				sql.append(item.getVal(i).toString().replaceAll("'", "''"));
				sql.append("', ");
			}	
		
		sql.delete(sql.length()-2 , sql.length());
		sql.append(")");
		stmt.executeUpdate(sql.toString());		
	}
	
	/**Находит по значению iD запись в базе данных и переписывает в нее значения полей из  item  */
	public int store(Item  item)   throws SQLException
	{	
		for (int i = 1; i < columnNames.length ; i++)	{			
			pstmUpdate.setObject(i, item.getVal(i));			
		}
		pstmUpdate.setObject(columnNames.length , item.getVal(0));
		return  pstmUpdate.executeUpdate();
	}
	
	/**Меняет Id записи в базе */
	public int changeID(Object oldID , Object newID )   throws SQLException
	{	
		pstmChangeID.setObject(1, newID );
		pstmChangeID.setObject(2, oldID );
		return pstmChangeID.executeUpdate();
	}
	
	
	 public void close() {
		   try
		   {			   
			   if (pstmInsert!= null)
				   pstmInsert.close();

			   if (pstmUpdate != null)
				   pstmUpdate.close();

			   if (pstmSearchById != null)
				   pstmSearchById.close();
			   
			   if(pstmDelete != null)
				   pstmDelete.close();
			   
			   if(pstmChangeID != null)
				   pstmChangeID.close();
			   if( stmt != null)
				   stmt.close();
			   
		   }
		   catch (SQLException e)
		   {
			   e.printStackTrace();
		   }


	 }
	
}
