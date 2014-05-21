package components;

import java.sql.SQLException;

import dao.DAO;
import dao.Item;

public class DetaledModel extends ItemsModel {
	private Object fKValue;
	private String fKName;
	private int fKPos;
	public DetaledModel(DAO dao, Object fKVavue, String fKName) {
		super(dao);
		this.fKValue =  fKVavue;
		this.fKName =fKName;
		fKPos = dao.getFieldIndex(fKName);
		setWhereCond(" where " + fKName + " = " + fKVavue);
	}
	

	@Override
	protected Item getEmptyRow() {
		 
		Item item = super.getEmptyRow();
	    item.setVal(fKPos, fKValue);
		return item;
	}
}
