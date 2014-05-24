package components;

import java.awt.BorderLayout;
import java.awt.Component;
import java.awt.FlowLayout;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.sql.Connection;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;

import javax.swing.JButton;
import javax.swing.JPanel;
import javax.swing.JScrollPane;
import javax.swing.JSplitPane;
import javax.swing.JTable;
import javax.swing.JTextArea;
import javax.swing.ScrollPaneConstants;
import javax.swing.table.AbstractTableModel;



public class QueryEditor extends JPanel {
	public class queryBtnListener implements ActionListener {

		@Override
		public void actionPerformed(ActionEvent e) {
			String sql = editor.getText();
			
			
			try {
				if (rs != null)
					rs.close();
				rs = stmt.executeQuery(sql); 
				
				ResultSetTableModel model = new ResultSetTableModel(rs);
				down.removeAll();
				JTable table = new JTable(model);
				down.add(table, BorderLayout.CENTER);
				down.add(table.getTableHeader(), BorderLayout.NORTH);
				revalidate();
			} catch (SQLException e1) {
				down.removeAll();
				down.add(new JTextArea(e1.getMessage()));
				revalidate();
				e1.printStackTrace();
			}

		}

	}
	
	Connection conn;
	Statement stmt;
	ResultSet rs;
	JTextArea editor = new JTextArea() ;
	JPanel down = new JPanel();
	
	//JTextArea textOutput = new JTextArea();
	JTable rezult;
	JButton queryBtn = new JButton("���������");
	private JSplitPane splitPane;
	
 public QueryEditor(Connection conn){
	 this.conn = conn;
	 try {
	 stmt = conn.createStatement(ResultSet.TYPE_SCROLL_INSENSITIVE, ResultSet.CONCUR_READ_ONLY);
	} catch (SQLException e) {
		// TODO Auto-generated catch block
		e.printStackTrace();
	}
	 setLayout(new BorderLayout());
	 
	
	 JPanel btmPanel = new JPanel();
	 btmPanel.setLayout(new FlowLayout());
	 add(btmPanel, BorderLayout.NORTH);
	 queryBtn.addActionListener(new queryBtnListener());
	 btmPanel.add(queryBtn);
	 
	JScrollPane downsplitPane = new JScrollPane(down);
	splitPane = new JSplitPane(JSplitPane.VERTICAL_SPLIT, editor , downsplitPane);
	
//	editor.setHorizontalScrollBarPolicy();		
	
	downsplitPane.setHorizontalScrollBarPolicy(ScrollPaneConstants.HORIZONTAL_SCROLLBAR_AS_NEEDED );
	 add(splitPane, BorderLayout.CENTER);
	 down.setLayout(new BorderLayout());
	 
 }
 
}

class ResultSetTableModel extends AbstractTableModel{
	ResultSet rs;
	//select * from �������������
	
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