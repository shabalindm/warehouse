package recycle;

import java.sql.Connection;


public class Panel1 extends TableEditPanel{
	
	public Panel1( Connection conn) {
		super(new Model1(conn));
	}
		
	

}
