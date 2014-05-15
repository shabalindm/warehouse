package dao;

import java.sql.Connection;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;
import java.text.DateFormat;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.TreeMap;

import javax.swing.table.AbstractTableModel;

public abstract class AbstractItemsTableModel<T> extends AbstractTableModel {
	
	Map<Integer, T> cache  = new TreeMap<>();
	int rowCount;
	String whereCond = "";
	Connection conn;
	MessageListener listener;
	DateFormat f = new SimpleDateFormat("MM/dd/yy");
	ResultSet rs;
	
/**   Устанавливает слушателя сообщений, которые воздикают при работе с моделью*/
	public void setMessageListener(MessageListener listener){
		this.listener = listener;
	}
	
/**   Передает слушателю  (если он установлен) сообщение*/
	void announce(String msg){
		if (listener != null)
			listener.setText(msg);		
	}
	
		
	public String getWhereCond() {
		return whereCond;
	}

	public void setWhereCond(String whereCond) {
		this.whereCond = whereCond;
	}

	/**Генеририрует SQL запрос, по которому модель будет брать данные из базы*/
	abstract String getSQL();
	
	/** Сбрасывает значения в кеше, закрывает старый курсор, создает новый, определяет
	 *  число строк в нем и обновляет значение поля rowCount*/
	public void updateCache() {
			Statement stmt = null;
			String sql = getSQL(); 
			try{
				stmt = conn.createStatement(ResultSet.TYPE_SCROLL_INSENSITIVE, ResultSet.CONCUR_READ_ONLY);
				ResultSet newrs = stmt.executeQuery(sql);
				try {rs.close();} 
				catch (Exception e){} // Закрываем предыдущий
				rs = newrs;
				// очищаем кэш
				cache.clear();
				// узнаем количество строк
				if (!rs.last())
					rowCount = 0;
				else
					rowCount =  rs.getRow();
				rs.beforeFirst();	
				System.out.println(rowCount);
			} catch (SQLException e) {
				// TODO Auto-generated catch block
				announce(e.getMessage());
				e.printStackTrace();
			}		

		finally{ fireTableDataChanged();}
	}
	
/*Считывает из результирующего набора порцию результатов в кэш*/
	abstract void pollToCache(int row);


	@Override
	public int getRowCount() {
		return rowCount;
	}

	
	/** Берет строчку, создает из нее item(s) и записывает его (их) в базу.
	 *  Внимание - просто записывает, значение в кеше автоматически не появляется*/
	public abstract void addRows(List<String[]> records); 
	
		
	/** Удаляет из таблицы и из базы строки по заданному набору индексов строк  */
	public void deleteRows(int [] deleted) {
		try{
			for(int rowNum : deleted ){
				T item = cache.get(rowNum);
				deleteFromDB(item);
				announce(null);
			}
		} catch (SQLException e) {
			announce(e.getMessage());
			e.printStackTrace();
		}
		finally{
			updateCache();
		}
	} 
	
	abstract void deleteFromDB(T item) throws SQLException  ;
	
	public  void close(){
		try {
			rs.close();
		} catch (SQLException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
	};
	
}
