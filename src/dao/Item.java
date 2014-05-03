package dao;
public class Item {
	String id;
	Item [] otherCols;
	
	public String getId() {
		return id;
	}
	
	public void setId(String id) {
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
	public void setField(int i , String value) {
		Item item = new Item();
		item.setId(value);		
		otherCols[i-1] = item;
	}
	public void setField(int i , Item item) {		
		otherCols[i-1] = item;
	}
	
	
	@Override
	public String toString() {
		// TODO Auto-generated method stub
		return id;
	}
	
	
}