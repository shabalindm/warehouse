package components;

import java.sql.SQLException;

import dao.DAO;
import dao.Item;

public class DetaledModel extends ItemsModel {
	private Object fKValue;
	private String fKName;
	private int fKPos;
	public DetaledModel(DAO dao, Object fKValue, String fKName) {
		super(dao);
		this.fKValue =  fKValue;
	
		this.fKName =fKName;
		fKPos = dao.getFieldIndex(fKName);
		if ( fKName instanceof String)
			setWhereCond(" where " + fKName + " = '" + fKValue + "'");
		else
			setWhereCond(" where " + fKName + " = " + fKValue);
			
	}
	

	@Override
	protected Item getEmptyRow() {
		 
		Item item = super.getEmptyRow();
	    item.setVal(fKPos, fKValue);
		return item;
	}
}
