package components;

public class WriteDataToDBException extends Exception {
	int rowIndex;

	public WriteDataToDBException(int rowIndex) {
		super();
		this.rowIndex = rowIndex;
	}
	

}
