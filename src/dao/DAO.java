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
		
		// Узнаем имена столбцов и типы столбцов, выполнив пробный запрос
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
	 
	   /**Берет  значения из текущей позиции курсора rs и заполняет ими все поля в объекте item. 
	    * Третьим параметром идет массив с именами столбцов в результирующим наборе. Если он null, то берется ColumnNames из ДАО;
	    ** В если типы объектов item и ДАО не соответствуют друг другу, результат не определен */
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

	/** Ищет по значению ID поле запись в базе данных и выдает новый объект item, поля которого заполнены значениями из этой записи */
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

		/** Обновляет значения полей объекта item, беря их из базы данных. Возвращает true если обновление прошло успешно */
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

	/** Находит по ID запись в базе удалеет ее. Если не удается - возвращает false*/
	public boolean  delete(Item item) throws SQLException{
		pstmDelete.setObject(1, item.getId());
		if (pstmDelete.executeUpdate() != 0)
			return true;
		return false;
		
	}
	
	
	/** Берет значения полей из Item  связывает их с полями (плейсхолдерами) в pstmInsert или pstmUpdate. Важно, чтобы число плейсхолдеров в pstm 
	 * равнялось количеству полей в item, а их порядок совапдал с порядком столбцов в defaultColumnNames
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
		
	/**Записывает значения полей item в базу данных в виде новой строчки */
	public void storeNew(Item item )   throws SQLException
	{	
		bindFields(item, pstmInsert);	
		pstmInsert.executeUpdate();
		 
	}
	
	/**Находит по значению iD запись в базе данных и переписывает в нее значения полей из  item  */
	public int store(Item  item)   throws SQLException
	{	
		bindFields(item, pstmUpdate);
		return pstmUpdate.executeUpdate();
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
