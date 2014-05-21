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
	//private String tablePK; // Имя первичного ключа
	private Integer PKPlace = null; // позиция первичного ключа c массиве с именами строк
	private String[] columnNames; // Имена всех столбцов в таблице
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
		
		// Узнаем имена столбцов и типы столбцов, выполнив пробный запрос
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
		
		//Собираем строчки для sql-запросов и подготавливем их		
		String [] colls = columnNames; 
		//1. Подготовка sqlInsert; 
		sqlInsert = "INSERT INTO " + tableName  + " (";
		for(int i = 1; i < colls.length; i ++)
			sqlInsert += colls[i]+ ", ";
		sqlInsert += colls[0] + ") ";		   
		sqlInsert += " VALUES ("; 
		for(int i = 1; i < colls.length; i ++)
			sqlInsert += "?, ";
		sqlInsert+="?)";
		pstmInsert = conn.prepareStatement(sqlInsert);

		//2 подготовка sqlUpdate
		if (PKPlace != null){
			sqlUpdate = "UPDATE " + tableName + " SET ";
			for(int i = 0; i<colls.length; i++){
				if (i == PKPlace.intValue())
					continue;			
				sqlUpdate += colls[i] + " = ?, ";
				
			}
			sqlUpdate = sqlUpdate.substring(0, sqlUpdate.length()-2); // удаляем лишнюю запятую 
			sqlUpdate += " WHERE " + colls[PKPlace] + " = ?";
			pstmUpdate = conn.prepareStatement(sqlUpdate);
		}
		
		//3 подготовка sqlUpdateByRowId
		sqlUpdateByRowId = "UPDATE " + tableName + " SET ";
		for(int i = 0; i<colls.length; i++)
			sqlUpdateByRowId += colls[i] + " = ?, ";
		sqlUpdateByRowId = sqlUpdateByRowId.substring(0, sqlUpdateByRowId.length()-2); // удаляем лишнюю запятую 
		sqlUpdateByRowId += " WHERE ROWID = ?";
		pstmUpdateByRowId = conn.prepareStatement(sqlUpdateByRowId);

		//4 подготовка sqlSearchById
		if (PKPlace != null){
			sqlSearchById = "SELECT ";
			for(int i = 0; i<colls.length; i++)
				sqlSearchById += colls[i]+ ", ";
			sqlSearchById +=  " ROWID "; 
			sqlSearchById += " FROM " + tableName + " WHERE " + colls[PKPlace] + " = ?";
			pstmSearchById = conn.prepareStatement(sqlSearchById);
		}
		
		//5 подготовка sqlSearchByRowId
		sqlSearchByRowId = "SELECT ";
		for(int i = 0; i<colls.length; i++)
			sqlSearchByRowId += colls[i]+ ", ";
		sqlSearchByRowId +=  " ROWID "; 
		sqlSearchByRowId += " FROM " + tableName + " WHERE ROWID = ?";
		pstmSearchByRowId = conn.prepareStatement(sqlSearchByRowId);


		//6 подготовка sqlDelete
		if (PKPlace != null){
			sqlDelete = "DELETE FROM " + tableName + " WHERE " + colls[PKPlace] + " = ?";
			pstmDelete = conn.prepareStatement(sqlDelete);	 
		}
		
		//7 подготовка sqlDeleteByRowID
		sqlDeleteByRowId = "DELETE FROM " + tableName + " WHERE ROWID = ?";
		pstmDeleteByRowId = conn.prepareStatement(sqlDeleteByRowId);
		
		// 8 подготовка sqlChangeID
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
	 
	

	/** Ищет по значению ID поле запись в базе данных и выдает новый объект item, поля которого заполнены значениями из этой записи */
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
	
	/** Ищет по значению ROWID поле запись в базе данных и выдает новый объект item, поля которого заполнены значениями из этой записи */
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

		/** Обновляет значения полей объекта item, беря их из базы данных. Возвращает true если обновление прошло успешно */
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
	
	/** Обновляет значения полей объекта item, беря их из базы данных по rowId. Возвращает true, если обновление прошло успешно */
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

	/** Находит по ID запись в базе удалеет ее. Если не удается - возвращает false*/
	public boolean  delete(Item item) throws SQLException{
		pstmDelete.setObject(1, item.getId());
		if (pstmDelete.executeUpdate() != 0)
			return true;
		return false;
	}
	
	/**Находит по RowID запись в базе и удаляет ее. Если не удается - возвращает false*/
	public boolean  deleteByRowId(Item item) throws SQLException{
		pstmDeleteByRowId.setObject(1, item.getRowId());
		if (pstmDeleteByRowId.executeUpdate() != 0)
			return true;
		return false;
	}
	
	
		
	/**Записывает значения полей item в базу данных в виде новой строчки. Сохраняет значения из Item как есть в т.ч и null-значения, 
	 *Вообще-то не нужен, метод storeNew2 лучше работает с null -значениями, хоть медленнее*/
	public void storeNew(Item item) throws SQLException{			
		for (int i = 0; i < columnNames.length ; i++)	{			
			pstmInsert.setObject(i, item.getVal(i));			
		}
		pstmInsert.setObject(columnNames.length, item.getVal(0));		
		pstmInsert.executeUpdate();
		 
	}
	
	/**Записывает значения полей item в базу данных в виде новой строчки, но при этом те поля, которые равны null пропускает.
	 * Для этого каждый раз собирается  PreparedStatement*/
	public void storeNew2(Item item )   throws SQLException{
		// Собираем номера тех столбцов, которые не пусты
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
	
	/**Находит по значению iD запись в базе данных и переписывает в нее значения полей из  item  */
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
	
	/**Находит по значению rowiD запись в базе данных и переписывает в нее значения полей из  item  */
	public int storebyRowId(Item  item)   throws SQLException
	{	
		for (int i = 0; i < columnNames.length ; i++)	{			
			pstmUpdateByRowId.setObject(i+1, item.getVal(i));			
		}
		pstmUpdateByRowId.setString(columnNames.length+1, item.getRowId());
		return  pstmUpdateByRowId.executeUpdate();
	}
	
	
	/**Меняет Id записи в базе */
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
		throw new RuntimeException(" Не найден столбец " + name);	
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
