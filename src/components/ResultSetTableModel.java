package components;

import java.sql.ResultSet;
import java.sql.SQLException;

import javax.swing.table.AbstractTableModel;

public class ResultSetTableModel extends AbstractTableModel{
	ResultSet rs;
	//select * from комплектующие
	
	int rowCount;
	public ResultSetTableModel(ResultSet rs) throws SQLException {
		super();
		this.rs = rs;
		if (!rs.last())
			rowCount = 0;
		else
			rowCount =  rs.getRow();
		rs.beforeFirst();
	}

	@Override
	public int getRowCount() {
		// TODO Auto-generated method stub
		return rowCount;
	}

	@Override
	public int getColumnCount() {
		// TODO Auto-generated method stub
		try {
			return rs.getMetaData().getColumnCount();
		} catch (SQLException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
		return 0;
	}

	@Override
	public Object getValueAt(int rowIndex, int columnIndex) {
		// TODO Auto-generated method stub
		try {
			rs.absolute(rowIndex+1);
			return rs.getObject(columnIndex+1);
		} catch (SQLException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
		return null;
		
	}

	@Override
	public String getColumnName(int column) {
		try {
			
			return rs.getMetaData().getColumnName(column+1);
		} catch (SQLException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
		return null;
		
	}
	
}