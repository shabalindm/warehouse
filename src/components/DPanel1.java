package components;

import java.math.BigDecimal;
import java.sql.Connection;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;

import dao.DAO;
import dao.Item;


/**Панель с требованиями, фильтрованная по одной заявке*/
public class DPanel1 extends   DetaledPanel<Item[]>  {

	public DPanel1(Connection conn, Item mainItem,  Object fKValue, String fKName) {
		super(new DModel1(conn, fKValue,  fKName), mainItem);
		for (int i = 0; i< model.getColumnCount(); i++){
			if(((DModel1) model).index1(i) != 0)
				table.getColumnModel().getColumn(i).setCellRenderer(new GreyCellRenderer());
				
		}
	}

	

}


class DModel1 extends Model5 { // берем за основу уже готовую табличку для редактирования требований
	private Object fKValue;
	private String fKName;
	private int fKPos;
	
	public DModel1(Connection conn, Object fKValue, String fKName) {
		super(conn);
		this.fKValue =  fKValue;
		
		this.fKName =fKName;
		fKPos = daos[0].getFieldIndex(fKName);
		if ( fKName instanceof String)
			setWhereCond(" where " + fKName + " = '" + fKValue + "'");
		else
			setWhereCond(" where " + fKName + " = " + fKValue);
		
	}

	@Override
	protected Item[] getEmptyRow() {
		 
		Item[] items = super.getEmptyRow();
	    items[0].setVal(fKPos, fKValue);
		return items;
	}

}


