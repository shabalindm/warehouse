package gui;

import java.awt.BorderLayout;
import java.awt.Component;
import java.awt.EventQueue;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.event.InputEvent;
import java.awt.event.KeyEvent;
import java.awt.event.MouseAdapter;
import java.awt.event.MouseEvent;
import java.awt.event.WindowAdapter;
import java.awt.event.WindowEvent;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.sql.Connection;
import java.sql.DatabaseMetaData;
import java.sql.DriverManager;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.Locale;
import java.util.Properties;
import java.util.Vector;

import javax.swing.JCheckBoxMenuItem;
import javax.swing.JFrame;
import javax.swing.JList;
import javax.swing.JMenu;
import javax.swing.JMenuBar;
import javax.swing.JMenuItem;
import javax.swing.JOptionPane;
import javax.swing.JPanel;
import javax.swing.JScrollPane;
import javax.swing.JTabbedPane;
import javax.swing.JTextArea;
import javax.swing.KeyStroke;

import components.ButtonTabComponent;
import dao.DAO;
import dao.MessageListener;
import dao.TableEditPanel;
import dao.TableViewer;

public class MainFrame extends JFrame {
	public boolean [] commitedEnclosure = new boolean[]{true};	//������ � ����� �������, � ������� ����� ��������, ������� �� ��������������� ���������
	Connection conn;
	
	JList<String> tables;
	JList<String> views ;
	JTextArea info;
	JTabbedPane tabbedPane = new JTabbedPane();
	
	
	/** ���� ��������� ����� ������������ ������� ��������� ������. � ������, ����� �������� ������ ��������� (�� ������) �� �������� ��� � ���� - ����*/
	MessageListener msgListener = new MessageListener(){
		@Override
		public void setText(String text) {
			info.setText(text);
			
		}};
	

	
	/**�����������*/
	public MainFrame(){		
		// �������������� ����������, ������ �������������� � ��������, ��������� �� � ������ JList
		try
		{
			// create and load Properties object
			// the properties file is jdbc_build.properties
			Properties dbProps = new Properties();
			dbProps.load(new FileInputStream("jdbc_build.properties"));

			// read the properties (db.driver and db.url)
			String driver = dbProps.getProperty("db.driver");
			String url = dbProps.getProperty("db.url");

			// load the JDBC driver class
			Class.forName(driver);

			String user = dbProps.getProperty("db.user");
			String passwd = dbProps.getProperty("db.passwd"); 

			Locale l = Locale.getDefault();
			Locale.setDefault(Locale.ENGLISH);
			conn = DriverManager.getConnection(url,user,passwd);
			Locale.setDefault(l);
			conn.setAutoCommit(false);


			DatabaseMetaData dbmd = conn.getMetaData();
			ResultSet rs = dbmd.getTables(null, user.toUpperCase(), null, new String[]{"TABLE"});
			Vector<String> tableNames = new Vector<>();
			
			while (rs.next()){
				tableNames.add((String) rs.getObject("TABLE_NAME"));
			}
			
			tables = new JList<>(tableNames);
			rs.close();
			rs =  dbmd.getTables(null, user.toUpperCase(), null, new String[]{"VIEW"});
			Vector<String> viewNames = new Vector<>();
			
			while (rs.next()){
				viewNames.add((String) rs.getObject("TABLE_NAME"));
			}
			
			views = new JList<>(viewNames);
			rs.close();
			
		} catch (Exception e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
		
		// ��������� ��������� � ������� ������ � �������������. 
		tables.addMouseListener(new MouseDoubleClickLstn());
		views.addMouseListener(new MouseDoubleClickLstn());
		
		initMenu();   
		
	// ��������� ����
		setLayout(new BorderLayout());
		JPanel westpanel = new JPanel();
		westpanel.setLayout(new BorderLayout());
		
		JTabbedPane navi = new JTabbedPane();
		JScrollPane tablesPane = new JScrollPane(tables);
		tablesPane.setVerticalScrollBarPolicy(JScrollPane.VERTICAL_SCROLLBAR_ALWAYS);
		
		JScrollPane viewsPane = new JScrollPane(views);
		tablesPane.setVerticalScrollBarPolicy(JScrollPane.VERTICAL_SCROLLBAR_ALWAYS);
		navi.addTab("       �������             "   , null, tablesPane);
		navi.addTab("     �������������        "    , null, viewsPane);
		
		info = new JTextArea(10, 14);
		JScrollPane infopane =  new JScrollPane(info);
		info.setLineWrap(true);
		
		westpanel.add(navi, BorderLayout.CENTER);
		westpanel.add(infopane, BorderLayout.SOUTH);
		add(westpanel, BorderLayout.WEST);
		
		tabbedPane.setTabLayoutPolicy(JTabbedPane.SCROLL_TAB_LAYOUT);	
		add(tabbedPane, BorderLayout.CENTER);
		addWindowListener(new CloseOperationListener());
		setDefaultCloseOperation(DO_NOTHING_ON_CLOSE);
	
	}// �����������

	


	private class MouseDoubleClickLstn extends MouseAdapter {
	    public void mouseClicked(MouseEvent evt) {
	        JList list = (JList)evt.getSource();
	        if (evt.getClickCount() == 2) {
	        	int index = list.locationToIndex(evt.getPoint());
	        	System.out.println(list.getModel().getElementAt(index));
	        	String tableName = (String) list.getModel().getElementAt(index);
	        	for (Component c : tabbedPane.getComponents())
	        		if (tableName.equals(c.getName())){ // �������� ���� ������� (�������������) ��� ������
	        			tabbedPane.setSelectedComponent(c);
	        			return;
	        		}
	        	
	        	//������� �������� � ��������� � ��������
	        	try {
	        		final DAO dao = new DAO(conn, tableName, null);				
	        		final TableEditPanel  tEpanel = new TableEditPanel(dao, commitedEnclosure);
	        		tEpanel.commitedEnclosure = commitedEnclosure;
	        		tEpanel.setMessageListener(msgListener);
	        		int i = tabbedPane.getTabCount();
	        		Component c = tabbedPane.add(tableName, tEpanel);
	        		c.setName(tableName);
	        		tabbedPane.setSelectedComponent(c); 
	        		
	        		// ��������� �� ������� �������, �������������� �������� ��������
	        		tabbedPane.setTabComponentAt(i, new ButtonTabComponent(tabbedPane){
	        			@Override
	        			public void removeTab(int i) {
	 	        				dao.close();
        						super.removeTab(i);
        						}
	        		});
 		
	        	} catch (SQLException e) {
	        		e.printStackTrace();
	        	}



	        } 
	    }
	}
	
	/** ���������, �����������, ������������� �� ��������� � ���� � ��������� ������ � ����������� �������������*/
	class CloseOperationListener extends WindowAdapter{
		public void windowClosing(WindowEvent e){
			if (!commitedEnclosure[0]){ 
				int result = JOptionPane.showConfirmDialog((Component) null, "��������� ���������� ������?",
						"alert", JOptionPane.YES_NO_CANCEL_OPTION);
				if (result == 0){
					try {	conn.commit();	conn.close();}
					catch (SQLException e1) {e1.printStackTrace();}
					MainFrame.this.dispose();
				}
				else if (result == 1){
					try {	conn.rollback(); conn.close();}
					catch (SQLException e1) {e1.printStackTrace();}
					MainFrame.this.dispose();
				}
				else //result == 2
					return;
					 	
			}
			MainFrame.this.dispose();

		}
	}
	/**��������� ���������� �������� ������*/
	private void refreshTabs() {
		new Thread(){
			public void run(){
				for(Component c :tabbedPane.getComponents())
					if (c instanceof TableEditPanel)
						((TableEditPanel)c).tableViewModel.updateCache();				
		}}.start();
	}

	/**������� �������*/
	private void initMenu() {
        JMenuBar menuBar = new JMenuBar();
        //create Options menu
        JMenuItem tabComponentsItem = new JMenuItem("��������� ���������");
       tabComponentsItem.setAccelerator(KeyStroke.getKeyStroke(KeyEvent.VK_S, InputEvent.CTRL_MASK));
        tabComponentsItem.addActionListener(new ActionListener() {
            public void actionPerformed(ActionEvent e) {
                try {
					conn.commit();
					refreshTabs();
					commitedEnclosure[0] = true;
				} catch (SQLException e1) {
					e1.printStackTrace();
				}
                }

			
            
        });
        JMenuItem scrollLayoutItem = new JMenuItem("��������");
       // scrollLayoutItem.setAccelerator(KeyStroke.getKeyStroke(KeyEvent.VK_S, InputEvent.ALT_MASK));
        scrollLayoutItem.addActionListener(new ActionListener() {
        	public void actionPerformed(ActionEvent e) {
        		try {
					conn.rollback();
					commitedEnclosure[0] = true;
					refreshTabs();
				} catch (SQLException e1) {
					e1.printStackTrace();
				}
        	}
        });
        JMenu optionsMenu = new JMenu("����������");
        optionsMenu.add(tabComponentsItem);
        optionsMenu.add(scrollLayoutItem);
        menuBar.add(optionsMenu);
        setJMenuBar(menuBar);
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
