package components.detaled;

import java.awt.BorderLayout;
import java.awt.event.WindowAdapter;
import java.awt.event.WindowEvent;

import javax.swing.JFrame;

import components.TableEditPanel;
import components.UserCancelledOperationException;
import components.WriteDataToDBException;

public class TableWindow extends JFrame {
	private TableEditPanel<?> tEPanel;

	public TableWindow(TableEditPanel<?> tEPanel){
		super();
		this.tEPanel = tEPanel;
		setLocationByPlatform(true);
		//setLayout(new BorderLayout());
		add(tEPanel);
		setDefaultCloseOperation(JFrame.DO_NOTHING_ON_CLOSE);
		addWindowListener(new CloseOperationListener());
		pack();		
		setVisible(true);
	}
	
	
	class CloseOperationListener extends WindowAdapter{
		public void windowClosing(WindowEvent e){
			try {
				tEPanel.close()	;
			} catch (WriteDataToDBException | UserCancelledOperationException exc) {
				return;
			}
			TableWindow.this.dispose();

		}
	}

}
