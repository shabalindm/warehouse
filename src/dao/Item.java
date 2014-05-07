package dao;
public class Item {
	Object id;
	Item [] otherCols;
	
	public Object getId() {
		return id;
	}
	
	public void setId(Object id) {
		this.id = id;
	}
	
	
	public Item[] getOtherCols() {
		return otherCols;
	}

	public void setOtherCols(Item[] otherCols) {
		this.otherCols = otherCols;
	}

	public Item getField(int i) {		
		return otherCols[i-1];
	}
	
	public Object getValue(int i){
		if (i==0)
			return id;
		else if (otherCols[i-1] == null)
			return null;
		else 
			return otherCols[i-1].getId();
	}
	
	public void setField(int i , Object value) {
		if (value == null)
			return;			
		Item item = new Item();
		item.setId(value);		
		otherCols[i-1] = item;
	}
	public void setField(int i , Item item) {		
		otherCols[i-1] = item;
	}

	@Override
	public String toString() {
		if (id == null)
			return  null;
		return id.toString();
	}
	
	
	
	
}