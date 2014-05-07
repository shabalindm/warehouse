package dao;


import java.awt.Component;
import java.awt.EventQueue;
import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.ResultSet;
import java.sql.Statement;
import java.util.Locale;

import javax.swing.JFrame;
import javax.swing.JOptionPane;

import oracle.jdbc.OracleDriver;

public class Main {
  public static void main(String[] argv) throws Exception {
	  
	  Locale l = Locale.getDefault();
		Locale.setDefault(Locale.ENGLISH);
		DriverManager.registerDriver(new OracleDriver());
		final Connection conn = DriverManager.getConnection("jdbc:oracle:thin:@localhost:1521:xe", "anya", "123");
		Locale.setDefault(l);
		conn.setAutoCommit(false);
		Statement stmt = conn.createStatement();
		ResultSet rs = stmt.executeQuery("Select * from комплектующие");
		final DAO dao = new DAO(conn, "КОМПЛЕКТУЮЩИЕ", "КОМПЛ_ID");
		EventQueue.invokeLater(new Runnable()
         {
            public void run()
            {           
               JFrame frame = new TableViewer(dao);
               frame.setTitle("Тест Таблицы");
               frame.setLocationByPlatform(true);
               //frame.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
               frame.setVisible(true);
            }
         });
  }
}
