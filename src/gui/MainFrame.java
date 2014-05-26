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
	
	
	/** Этот слушатель будет передаваться каждому редактору таблиц. В случае, когда редактор выдаст сообщение (об ошибке) он поместит его в инфо - поле*/
	MessageListener msgListener = new MessageListener(){
		@Override
		public void setText(String text) {
			info.setText(text);
		}};	
		
	/** Слушатель состояния соединения.*/
	 public	static StateListener stateListener = new StateListener() {		
		@Override
		public void setState(boolean state) {
			commited = state;			
		}
	};

	
	/**Конструктор*/
	public MainFrame(){		
		// Инициализируем соединение, читаем метаинформацию о таблицах, загружаем ее в списки JList
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
		
		// Добавляем слушатели к спискам таблиц и представлений. 
		tables.addMouseListener(new MouseDoubleClickLstn());
		views.addMouseListener(new MouseDoubleClickLstn());
		
		initMenu();   
		
	// компонуем окно
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
		info.setForeground(Color.RED);
		info.setWrapStyleWord(true);
		
		westpanel.add(navi, BorderLayout.CENTER);
		westpanel.add(infopane, BorderLayout.SOUTH);
		add(westpanel, BorderLayout.WEST);
		
		tabbedPane.setTabLayoutPolicy(JTabbedPane.SCROLL_TAB_LAYOUT);	
		add(tabbedPane, BorderLayout.CENTER);
		addWindowListener(new CloseOperationListener());
		setDefaultCloseOperation(DO_NOTHING_ON_CLOSE);
	
	}// Конструктор

	

/** Слушатель, который по двойному щелчку мыши на таблице из списка открывает ее в новой вкладке*/
	private class MouseDoubleClickLstn extends MouseAdapter {
	    public void mouseClicked(MouseEvent evt) {
	        JList list = (JList)evt.getSource();
	        if (evt.getClickCount() == 2) {
	        	int index = list.locationToIndex(evt.getPoint());
	        	String tableName = (String) list.getModel().getElementAt(index);
	        	// Если редактор этой таблицы (представления) уже открыт, то нужно только перевести на него фокус
	        	for (Component c : tabbedPane.getComponents())
	        		if (tableName.equals(c.getName())){ 
	        			tabbedPane.setSelectedComponent(c);
	        			return;
	        		}
	        	
	        	//методом тыка, узанаем, обновляемый ли вид
	        	 boolean rowIdSupported;
	        	try {
					conn.createStatement().executeQuery("select rowid from " + tableName+ " where 1<>1" );
					  rowIdSupported = true;
				} catch (SQLException e1) {
					rowIdSupported = false;
				}
	        	
	        		//Создаем панельку с таблицами и кнопками
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
	        			
	        			//размещаем панель в новой вкладке
	        			putInTab(tableName, tEpanel, model);

	        		} catch (SQLException e) {
	        			e.printStackTrace();
	        		}
	        	
	        } 
	    }		
	}
	

	/**Размещает созданную панель в новой закрывающейся вкладке
	 * @param tableName
	 * @param tEpanel
	 * @param model 
	 */
	private void putInTab(String tabName, final TableEditPanel tEpanel, final AbstractItemsTableModel<?>  model) {
		int i = tabbedPane.getTabCount();
		Component c = tabbedPane.add(tabName, tEpanel);
		c.setName(tabName);
		tabbedPane.setSelectedComponent(c); 
		
		// Добавляем ко вкладке крестик, переопределяем операцию закрытия
		tabbedPane.setTabComponentAt(i, new ButtonTabComponent(tabbedPane){
			@Override
			public void removeTab(int i) throws Exception {				
					tEpanel.close();
					super.removeTab(i);
				}
		});
	}
	
	
	
	
	/** Слушатель, проверяющий, зафиксированы ли изменения в базе и выводящий диалог с предложеним зафиксировать*/
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
				int result = JOptionPane.showConfirmDialog((Component) null, "Зафиксировать все изменения?",
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
	
	

	/**Создает менюшки*/
	private void initMenu() {
        JMenuBar menuBar = new JMenuBar();
        //create Options menu
       JMenuItem commit = new JMenuItem("Сохранить изменения");
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
      
       JMenuItem rollback = new JMenuItem("Откатить");
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
        JMenu connectMenu = new JMenu("Соединение");
        connectMenu.add(commit);
        connectMenu.add(rollback);
        menuBar.add(connectMenu);
        
 /*------------------------------------------------------------------------*/
        JMenu insertMenu = new JMenu("Специальные таблицы");
        
        JMenuItem insertItem1 = new JMenuItem("Ввод спецификаций");        
        
        insertItem1.addActionListener(new ActionListener() {
        	public void actionPerformed(ActionEvent e) {       		
        			Panel1 panel = new Panel1(conn);
        			panel.model.setMessageListener(msgListener);
        			panel.setStateListener(stateListener);
        			putInTab("Cпецификации с комплектующими", panel, panel.model);        			
        	}           
        });
        JMenuItem insertItem2 = new JMenuItem("Ввод требований");        
        
        insertItem2.addActionListener(new ActionListener() {
        	public void actionPerformed(ActionEvent e) { 
        			      		
        			Panel2 panel = new Panel2(conn);
        			panel.model.setMessageListener(msgListener);
        			panel.setStateListener(stateListener);
        			putInTab("Ввод требований", panel, panel.model);        			
        	}           
        });
        
 JMenuItem insertItem3 = new JMenuItem("Заявки");        
        
        insertItem3.addActionListener(new ActionListener() {
        	public void actionPerformed(ActionEvent e) {      		
        			Panel3 panel = new Panel3(conn);
        			panel.model.setMessageListener(msgListener);
        			panel.setStateListener(stateListener);
        			putInTab("Заявки", panel, panel.model);      			
        	}           
        });
        
JMenuItem insertItem4 = new JMenuItem("Накладные");        
        
        insertItem4.addActionListener(new ActionListener() {
        	public void actionPerformed(ActionEvent e) {      		
        			Panel4 panel = new Panel4(conn);
        			panel.model.setMessageListener(msgListener);
        			panel.setStateListener(stateListener);
        			putInTab("Накладные", panel, panel.model);      			
        	}           
        });
        
JMenuItem insertItem5 = new JMenuItem("Требования");        
        
        insertItem5.addActionListener(new ActionListener() {
        	public void actionPerformed(ActionEvent e) {      		
        			Panel5 panel = new Panel5(conn);
        			panel.model.setMessageListener(msgListener);
        			panel.setStateListener(stateListener);
        			putInTab("требования", panel, panel.model);      			
        	}           
        });    
JMenuItem insertItem6 = new JMenuItem("детализация заявок");        
        
        insertItem6.addActionListener(new ActionListener() {
        	public void actionPerformed(ActionEvent e) {      		
        			Panel6 panel = new Panel6(conn);
        			panel.model.setMessageListener(msgListener);
        			panel.setStateListener(stateListener);
        			putInTab("детал_заявок", panel, panel.model);      			
        	}           
        });  
JMenuItem insertItem7 = new JMenuItem("детализация_накл");        
        
        insertItem7.addActionListener(new ActionListener() {
        	public void actionPerformed(ActionEvent e) {      		
        			Panel7 panel = new Panel7(conn);
        			panel.model.setMessageListener(msgListener);
        			panel.setStateListener(stateListener);
        			putInTab("детализация_накл", panel, panel.model);      			
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
        
 JMenu querryMenu = new JMenu("Редактор запросов");
        
        JMenuItem querryItem = new JMenuItem("Редактор запросов");        
        
        querryItem.addActionListener(new ActionListener() {
        	public void actionPerformed(ActionEvent e) { 
        		QueryEditor panel = new QueryEditor(conn);
        		int i = tabbedPane.getTabCount();
        		Component c = tabbedPane.add("редактор", panel);
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

