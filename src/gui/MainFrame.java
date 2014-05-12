package gui;

import java.awt.BorderLayout;
import java.awt.Component;
import java.awt.EventQueue;
import java.awt.event.MouseAdapter;
import java.awt.event.MouseEvent;
import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.sql.Connection;
import java.sql.DatabaseMetaData;
import java.sql.DriverManager;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.Locale;
import java.util.Properties;
import java.util.Vector;

import javax.swing.DefaultListCellRenderer;
import javax.swing.JFrame;
import javax.swing.JLabel;
import javax.swing.JList;
import javax.swing.JPanel;
import javax.swing.JScrollPane;
import javax.swing.JTabbedPane;
import javax.swing.JTextArea;
import javax.swing.ListCellRenderer;
import javax.swing.ScrollPaneConstants;
import javax.swing.table.DefaultTableCellRenderer;

import components.ButtonTabComponent;
import dao.TableViewer;

public class MainFrame extends JFrame {
	String url = null;
	private String user;
	private String passwd;
	
	
	JList<String> tables;
	JList<String> views ;
	JTextArea info;
	JTabbedPane tabbedPane = new JTabbedPane();
	

	
	{ tabbedPane.setTabLayoutPolicy(JTabbedPane.SCROLL_TAB_LAYOUT);	}

	
	public MainFrame(){
		initJDBC();
		
		Connection conn = null;
		try {	
			conn = makeConnection();		
			DatabaseMetaData dbmd = conn.getMetaData();
			ResultSet rs = dbmd.getTables(null, user.toUpperCase(), null, new String[]{"TABLE"});
			Vector<String> tableNames = new Vector<>();
			while (rs.next()){
				tableNames.add((String) rs.getObject("TABLE_NAME"));
			}
			tables = new JList<>(tableNames);
			rs.close();
			rs =   dbmd.getTables(null, user.toUpperCase(), null, new String[]{"VIEW"});
			Vector<String> viewNames = new Vector<>();
			while (rs.next()){
				viewNames.add((String) rs.getObject("TABLE_NAME"));
			}
			views = new JList<>(viewNames);
			rs.close();
			
		} catch (SQLException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
		finally{if (conn != null)
			try {
				conn.close();
			} catch (SQLException e) {
				e.printStackTrace();
			}}
		
		tables.addMouseListener(new MouseDoubleClickLstn());
		views.addMouseListener(new MouseDoubleClickLstn());
	
		setLayout(new BorderLayout());
		JPanel westpanel = new JPanel();
		westpanel.setLayout(new BorderLayout());
		
		JTabbedPane navi = new JTabbedPane();
		JScrollPane tablesPane = new JScrollPane(tables);
		tablesPane.setVerticalScrollBarPolicy(JScrollPane.VERTICAL_SCROLLBAR_ALWAYS);
		
		JScrollPane viewsPane = new JScrollPane(views);
		tablesPane.setVerticalScrollBarPolicy(JScrollPane.VERTICAL_SCROLLBAR_ALWAYS);
		navi.addTab("       таблицы             "   , null, tablesPane);
		navi.addTab("     представления        "    , null, viewsPane);
		
		info = new JTextArea(10, 14);
		JScrollPane infopane =  new JScrollPane(info);
		info.setLineWrap(true);
		
		westpanel.add(navi, BorderLayout.CENTER);
		westpanel.add(infopane, BorderLayout.SOUTH);
		add(westpanel, BorderLayout.WEST);
		add(tabbedPane, BorderLayout.CENTER);
		
	
	}



	private Connection makeConnection() throws SQLException {
		Connection conn;
		Locale l = Locale.getDefault();
		Locale.setDefault(Locale.ENGLISH);
		conn = DriverManager.getConnection(url,user,passwd);
		Locale.setDefault(l);
		conn.setAutoCommit(false);
		return conn;
	}
	
	
	
	private void initJDBC() {
		try
	      {
	         // create and load Properties object
	         // the properties file is jdbc_build.properties
	         Properties dbProps = new Properties();
	         dbProps.load(new FileInputStream("jdbc_build.properties"));

	         // read the properties (db.driver and db.url)
	         String driver = dbProps.getProperty("db.driver");
	         url = dbProps.getProperty("db.url");
	         
	         // load the JDBC driver class
	         Class.forName(driver);
	         
	          user = dbProps.getProperty("db.user");
	          passwd = dbProps.getProperty("db.passwd"); 
	          
	         }
		catch (Exception e){
			e.printStackTrace();
		}
		
	}

	private class MouseDoubleClickLstn extends MouseAdapter {
	    public void mouseClicked(MouseEvent evt) {
	        JList list = (JList)evt.getSource();
	        if (evt.getClickCount() == 2) {
	            int index = list.locationToIndex(evt.getPoint());
	            System.out.println(list.getModel().getElementAt(index));
	            String title = (String) list.getModel().getElementAt(index);
	            for (Component c :tabbedPane.getComponents())
	            	if (title.equals(c.getName())){
	            		tabbedPane.setSelectedComponent(c);
	            		return;
	            		}
	            int i = tabbedPane.getTabCount();
	            Component c = tabbedPane.add(title, new JLabel(title));
	            c.setName(title);
	            tabbedPane.setTabComponentAt(i, new ButtonTabComponent(tabbedPane));
	            tabbedPane.setSelectedComponent(c); 
	            
	            
	        } else if (evt.getClickCount() == 3) {   // Triple-click
	            int index = list.locationToIndex(evt.getPoint());

	        }
	    }
	}
	









	public static void main(String[] args) {
		
		EventQueue.invokeLater(new Runnable()
        {
           public void run()
           {           
              JFrame frame = new MainFrame();
              frame.setTitle("*****");
              frame.setLocationByPlatform(true);
              frame.setSize(400, 400);
              //frame.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
              frame.setVisible(true);
           }
        });

	}

}
