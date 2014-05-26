package gui;

import java.awt.BorderLayout;
import java.awt.Color;
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
import java.sql.Connection;
import java.sql.DatabaseMetaData;
import java.sql.DriverManager;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.Locale;
import java.util.Properties;
import java.util.Vector;

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

import components.AbstractItemsTableModel;
import components.ButtonTabComponent;
import components.ItemNotEditingModel;
import components.ItemsModel;
import components.MessageListener;
import components.Panel1;
import components.Panel2;
import components.Panel3;
import components.Panel4;
import components.Panel5;
import components.Panel6;
import components.Panel7;
import components.QueryEditor;
import components.StateListener;
import components.TableEditPanel;
import components.UserCancelledOperationException;
import components.WiewNotEditPanel;
import components.WriteDataToDBException;
import dao.DAO;


public class MainFrame extends JFrame {
	public static  boolean commited = true; 
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
		
	/** ��������� ��������� ����������.*/
	 public	static StateListener stateListener = new StateListener() {		
		@Override
		public void setState(boolean state) {
			commited = state;			
		}
	};

	
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
		info.setForeground(Color.RED);
		info.setWrapStyleWord(true);
		
		westpanel.add(navi, BorderLayout.CENTER);
		westpanel.add(infopane, BorderLayout.SOUTH);
		add(westpanel, BorderLayout.WEST);
		
		tabbedPane.setTabLayoutPolicy(JTabbedPane.SCROLL_TAB_LAYOUT);	
		add(tabbedPane, BorderLayout.CENTER);
		addWindowListener(new CloseOperationListener());
		setDefaultCloseOperation(DO_NOTHING_ON_CLOSE);
	
	}// �����������

	

/** ���������, ������� �� �������� ������ ���� �� ������� �� ������ ��������� �� � ����� �������*/
	private class MouseDoubleClickLstn extends MouseAdapter {
	    public void mouseClicked(MouseEvent evt) {
	        JList list = (JList)evt.getSource();
	        if (evt.getClickCount() == 2) {
	        	int index = list.locationToIndex(evt.getPoint());
	        	String tableName = (String) list.getModel().getElementAt(index);
	        	// ���� �������� ���� ������� (�������������) ��� ������, �� ����� ������ ��������� �� ���� �����
	        	for (Component c : tabbedPane.getComponents())
	        		if (tableName.equals(c.getName())){ 
	        			tabbedPane.setSelectedComponent(c);
	        			return;
	        		}
	        	
	        	//������� ����, �������, ����������� �� ���
	        	 boolean rowIdSupported;
	        	try {
					conn.createStatement().executeQuery("select rowid from " + tableName+ " where 1<>1" );
					  rowIdSupported = true;
				} catch (SQLException e1) {
					rowIdSupported = false;
				}
	        	
	        		//������� �������� � ��������� � ��������
	        		try {
	        			DAO dao = new DAO(conn, tableName, null);	

	        			
	        			ItemsModel model;
	        			TableEditPanel  tEpanel;
	        			if (rowIdSupported){
	        				 model = new ItemsModel(dao);
	        				 tEpanel = new TableEditPanel(model);
	        			}
	        			else  {
	        				model = new ItemNotEditingModel(dao);
	        				tEpanel = new WiewNotEditPanel(model);
	        			}
	        			
	        			model.setMessageListener(msgListener);	        			
	        			tEpanel.setStateListener(stateListener);
	        			
	        			//��������� ������ � ����� �������
	        			putInTab(tableName, tEpanel, model);

	        		} catch (SQLException e) {
	        			e.printStackTrace();
	        		}
	        	
	        } 
	    }		
	}
	

	/**��������� ��������� ������ � ����� ������������� �������
	 * @param tableName
	 * @param tEpanel
	 * @param model 
	 */
	private void putInTab(String tabName, final TableEditPanel tEpanel, final AbstractItemsTableModel<?>  model) {
		int i = tabbedPane.getTabCount();
		Component c = tabbedPane.add(tabName, tEpanel);
		c.setName(tabName);
		tabbedPane.setSelectedComponent(c); 
		
		// ��������� �� ������� �������, �������������� �������� ��������
		tabbedPane.setTabComponentAt(i, new ButtonTabComponent(tabbedPane){
			@Override
			public void removeTab(int i) throws Exception {				
					tEpanel.close();
					super.removeTab(i);
				}
		});
	}
	
	
	
	
	/** ���������, �����������, ������������� �� ��������� � ���� � ��������� ������ � ����������� �������������*/
	class CloseOperationListener extends WindowAdapter{
		public void windowClosing(WindowEvent e){
			int tabCount = tabbedPane.getTabCount();
			while(tabbedPane.getTabCount()>0){
				try {
					tabbedPane.setSelectedIndex(0);
					((ButtonTabComponent)tabbedPane.getTabComponentAt(0)).removeTab(0);
				} catch (Exception e1) {
					return;
				} 
			}
			if (!commited){ 
				int result = JOptionPane.showConfirmDialog((Component) null, "������������� ��� ���������?",
						"", JOptionPane.YES_NO_CANCEL_OPTION);
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
	
	

	/**������� �������*/
	private void initMenu() {
        JMenuBar menuBar = new JMenuBar();
        //create Options menu
       JMenuItem commit = new JMenuItem("��������� ���������");
       commit.addActionListener(new ActionListener() {
       	public void actionPerformed(ActionEvent e) {
       		try {
					conn.commit();
					commited = true;
					//refreshTabs();
				} catch (SQLException e1) {
					e1.printStackTrace();
				}
       	}
       });
      
       JMenuItem rollback = new JMenuItem("��������");
        rollback.addActionListener(new ActionListener() {
        	public void actionPerformed(ActionEvent e) {
        		try {
					conn.rollback();
					commited = true;
					//refreshTabs();
				} catch (SQLException e1) {
					e1.printStackTrace();
				}
        	}
        });
        JMenu connectMenu = new JMenu("����������");
        connectMenu.add(commit);
        connectMenu.add(rollback);
        menuBar.add(connectMenu);
        
 /*------------------------------------------------------------------------*/
        JMenu insertMenu = new JMenu("����������� �������");
        
        JMenuItem insertItem1 = new JMenuItem("���� ������������");        
        
        insertItem1.addActionListener(new ActionListener() {
        	public void actionPerformed(ActionEvent e) {       		
        			Panel1 panel = new Panel1(conn);
        			panel.model.setMessageListener(msgListener);
        			panel.setStateListener(stateListener);
        			putInTab("C����������� � ��������������", panel, panel.model);        			
        	}           
        });
        JMenuItem insertItem2 = new JMenuItem("���� ����������");        
        
        insertItem2.addActionListener(new ActionListener() {
        	public void actionPerformed(ActionEvent e) { 
        			      		
        			Panel2 panel = new Panel2(conn);
        			panel.model.setMessageListener(msgListener);
        			panel.setStateListener(stateListener);
        			putInTab("���� ����������", panel, panel.model);        			
        	}           
        });
        
 JMenuItem insertItem3 = new JMenuItem("������");        
        
        insertItem3.addActionListener(new ActionListener() {
        	public void actionPerformed(ActionEvent e) {      		
        			Panel3 panel = new Panel3(conn);
        			panel.model.setMessageListener(msgListener);
        			panel.setStateListener(stateListener);
        			putInTab("������", panel, panel.model);      			
        	}           
        });
        
JMenuItem insertItem4 = new JMenuItem("���������");        
        
        insertItem4.addActionListener(new ActionListener() {
        	public void actionPerformed(ActionEvent e) {      		
        			Panel4 panel = new Panel4(conn);
        			panel.model.setMessageListener(msgListener);
        			panel.setStateListener(stateListener);
        			putInTab("���������", panel, panel.model);      			
        	}           
        });
        
JMenuItem insertItem5 = new JMenuItem("����������");        
        
        insertItem5.addActionListener(new ActionListener() {
        	public void actionPerformed(ActionEvent e) {      		
        			Panel5 panel = new Panel5(conn);
        			panel.model.setMessageListener(msgListener);
        			panel.setStateListener(stateListener);
        			putInTab("����������", panel, panel.model);      			
        	}           
        });    
JMenuItem insertItem6 = new JMenuItem("����������� ������");        
        
        insertItem6.addActionListener(new ActionListener() {
        	public void actionPerformed(ActionEvent e) {      		
        			Panel6 panel = new Panel6(conn);
        			panel.model.setMessageListener(msgListener);
        			panel.setStateListener(stateListener);
        			putInTab("�����_������", panel, panel.model);      			
        	}           
        });  
JMenuItem insertItem7 = new JMenuItem("�����������_����");        
        
        insertItem7.addActionListener(new ActionListener() {
        	public void actionPerformed(ActionEvent e) {      		
        			Panel7 panel = new Panel7(conn);
        			panel.model.setMessageListener(msgListener);
        			panel.setStateListener(stateListener);
        			putInTab("�����������_����", panel, panel.model);      			
        	}           
        });  
        insertMenu.add(insertItem1);
        insertMenu.add(insertItem2);
        insertMenu.add(insertItem3);
        insertMenu.add(insertItem4);
        insertMenu.add(insertItem5);
        insertMenu.add(insertItem6);
        insertMenu.add(insertItem7);
        menuBar.add(insertMenu);  
        
 JMenu querryMenu = new JMenu("�������� ��������");
        
        JMenuItem querryItem = new JMenuItem("�������� ��������");        
        
        querryItem.addActionListener(new ActionListener() {
        	public void actionPerformed(ActionEvent e) { 
        		QueryEditor panel = new QueryEditor(conn);
        		int i = tabbedPane.getTabCount();
        		Component c = tabbedPane.add("��������", panel);
        		tabbedPane.setSelectedComponent(c);
        		tabbedPane.setTabComponentAt(i, new ButtonTabComponent(tabbedPane));     			
        	}           
        });
        
        querryMenu.add(querryItem);
        menuBar.add(querryMenu); 
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
              frame.setSize(1000, 400);
              frame.setVisible(true);
           }
        });

	}

}

