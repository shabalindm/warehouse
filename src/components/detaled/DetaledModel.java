package components.detaled;

import components.ItemsModel;

import dao.DAO;
import dao.Item;

public class DetaledModel extends ItemsModel {
	Object fKeyValue;
	String fKeyName ;
	public DetaledModel(DAO dao, Object fKeyValue, String fKeyName ) {
		super(dao);
		this.fKeyValue =  fKeyValue;
		this.fKeyName =fKeyName;
	}

	@Override
	protected Item getEmptyRow() {
		 
		Item item = super.getEmptyRow();
	//	item.setVal(fKeyPos, fKeyValue);
		return item;
	}

	@Override
	protected String getSQL() {
		if (fKeyValue instanceof String)
			fKeyValue = "'" +  fKeyValue +  "'" ;
		return  null;// super.getSQL() + " where " + dao.getColumnNames()[fKeyPos] + " = " + fKeyValue;
	}
}
