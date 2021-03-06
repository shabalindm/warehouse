package components;

import java.awt.BorderLayout;
import java.awt.event.WindowAdapter;
import java.awt.event.WindowEvent;
import java.io.ObjectOutputStream.PutField;

import javax.swing.JFrame;

public class TableWindow extends JFrame {
	private TableEditPanel<?> tEPanel;

	public TableWindow(TableEditPanel<?> tEPanel, String name){
		super(name);
		this.tEPanel = tEPanel;
		setLocationByPlatform(true);
		//setLayout(new BorderLayout());
		add(tEPanel);
		setDefaultCloseOperation(JFrame.DO_NOTHING_ON_CLOSE);
		addWindowListener(new CloseOperationListener());
		
		pack();		
		setVisible(true);
	}
	
	public void close() throws WriteDataToDBException, UserCancelledOperationException {
			toFront();
			tEPanel.close();
			dispose();
		}	
	
	class CloseOperationListener extends WindowAdapter{
		public void windowClosing(WindowEvent e){
			try {
				close();
			} catch (WriteDataToDBException | UserCancelledOperationException e1) {
				return;
			}
		}	
		
	}

}
