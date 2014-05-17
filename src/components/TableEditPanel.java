package components;

import gui.MainFrame;

import java.awt.BorderLayout;
import java.awt.FlowLayout;
import java.awt.Toolkit;
import java.awt.datatransfer.Clipboard;
import java.awt.datatransfer.DataFlavor;
import java.awt.datatransfer.Transferable;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.event.AdjustmentEvent;
import java.awt.event.AdjustmentListener;
import java.awt.event.KeyEvent;
import java.awt.event.KeyListener;
import java.text.DateFormat;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

import javax.swing.Icon;
import javax.swing.JButton;
import javax.swing.JFrame;
import javax.swing.JLabel;
import javax.swing.JPanel;
import javax.swing.JScrollPane;
import javax.swing.JSplitPane;
import javax.swing.JTable;
import javax.swing.JTextArea;
import javax.swing.ScrollPaneConstants;
import javax.swing.event.ChangeEvent;
import javax.swing.event.ListSelectionEvent;
import javax.swing.event.TableColumnModelEvent;
import javax.swing.event.TableColumnModelListener;
import javax.swing.event.TableModelEvent;
import javax.swing.event.TableModelListener;
import javax.swing.table.TableModel;

public  class TableEditPanel<T> extends JPanel {
	
	AbstractItemsTableModel<T> model;
	JTable table;
	JPanel btnPanel;

	JButton insertBtn; 
	JButton deleteBtn;
	JButton unDeleteBtn;
	JButton writeBtn;
	JButton clearBtn;
	JButton whereBtn;	
	private final boolean COMMITED = true;
	
	private StateListener stateListener;	
	/**   ������������� ��������� ��������� ���������� (commited)*/
	public void setStateListener(StateListener stateListener){
		this.stateListener = stateListener;
	}
	
/**   �������� ���������  (���� �� ����������) ���������*/
	protected void setState(boolean state){
		if (stateListener != null)
			stateListener.setState(state);		
	}

	/**�����������*/ 
	 public TableEditPanel(AbstractItemsTableModel<T> model) {
		this.model = model;
		table = new EJTable(model);
		table.setAutoCreateRowSorter(true);
		table.setCellSelectionEnabled(true);	
		table.setAutoResizeMode(JTable.AUTO_RESIZE_OFF);
		JScrollPane scrollPane = new JScrollPane(table);
		scrollPane.setVerticalScrollBarPolicy(JScrollPane.VERTICAL_SCROLLBAR_AS_NEEDED);
		scrollPane.setHorizontalScrollBarPolicy(JScrollPane.HORIZONTAL_SCROLLBAR_AS_NEEDED);
		
		JPanel btnPanel = new JPanel();
		initBtnPanel(btnPanel);			
		
		setLayout(new BorderLayout());
		add(btnPanel, BorderLayout.NORTH);
		add(scrollPane, BorderLayout.CENTER);
	}

	 
	 /**
		 * @param btnPanel 
		 * 
		 */
	 protected void initBtnPanel(JPanel btnPanel) {
		 insertBtn  = makeButton("��������", null, new InsertBtnListener());
		 insertBtn.setToolTipText("����� Shift, ����� �������� ����� ������ ����� ���������� �������");
		 deleteBtn = makeButton("�������", null, new DeleteBtnListener());
		 unDeleteBtn = makeButton("�������", null, new UnDeleteBtnListener()) ;
		 writeBtn = makeButton("��������", null, new WriteBtnListener()) ;
		 clearBtn = makeButton("��������", null, new ClearBtnListener()) ;
		 whereBtn = makeButton("�������", null, new WhereBtnListener());
		 btnPanel.setLayout(new FlowLayout(FlowLayout.LEFT));

		 btnPanel.add(insertBtn); 
		 btnPanel.add(deleteBtn);
		 btnPanel.add(unDeleteBtn);
		 btnPanel.add(writeBtn);
		 btnPanel.add(clearBtn);
		 btnPanel.add(whereBtn);
	 }
	 
	
	/** ��������� ������ �������� �����*/
	class DeleteBtnListener implements ActionListener{

		@Override
		public void actionPerformed(ActionEvent e) {
			int[] rowsToDelete = table.getSelectedRows();
			int currentRow = table.getSelectedRow();			
			int count = table.getSelectedRowCount();
			for (int i = 0; i <count; i++){	
				if(model.isNewRow(currentRow)){					
					model.deleteRow(currentRow);
					System.out.println( " ������ " + currentRow + "(����� �������)");
				}else{
					model.deleteRow(currentRow);
					currentRow ++ ;
					System.out.println( " ������ " + currentRow + "(������ �������)");
				}
					
			}
			model.fireTableRowsDeleted(rowsToDelete[0], rowsToDelete[rowsToDelete.length-1]);
		}}
	
	/** ��������� ������ ������ �������� �����*/
	class UnDeleteBtnListener implements ActionListener{

		@Override
		public void actionPerformed(ActionEvent e) {
			for (int rowIndex : table.getSelectedRows())			
				model.unDeleteRow(rowIndex);		
		}}
	
	/** ��������� ������ ������ ���������� ������� � ���� */
	public class WriteBtnListener implements ActionListener {

		@Override
		public void actionPerformed(ActionEvent e) {
			int errorRow = model.writeToDB();
			if (errorRow != -1){
				table.setRowSelectionInterval(errorRow, errorRow);
			} else {
				setState(!COMMITED);
		}

		}

	}
	
	/** ��������� ������ ������� ������*/
	class InsertBtnListener implements ActionListener{
		@Override
		public void actionPerformed(ActionEvent e) {
			int row = table.getSelectedRow();
			System.out.println(e.getModifiers());
			if(e.getModifiers() == 17) //������ ������� shift
				row--;
			model.addRow(row);
			table.setRowSelectionInterval(row+1, row+1);
		}
	}
	
	/** ��������� ������ ������� ��������� ������*/
	class ClearBtnListener implements ActionListener{
		@Override
		public void actionPerformed(ActionEvent e) {			
			model.updateData();
		}
	}

	/** ��������� ������ "�������"*/
	 class WhereBtnListener implements ActionListener {
		
		@Override
		public void actionPerformed(ActionEvent e) {	
			 final JFrame dialog = new JFrame();
			 dialog.setLocationByPlatform(true);
			 dialog.setLayout(new BorderLayout());
			 final JTextArea input = new JTextArea(10, 50 );
			 dialog.add(input, BorderLayout.CENTER );
			 input.setText(model.whereCond);
			 input.selectAll();
			 input.setLineWrap(true);
			 JButton okBtn = makeButton("OK", null, new ActionListener(){

				@Override
				public void actionPerformed(ActionEvent e) {
					model.whereCond = input.getText();
					model.updateData();
					dialog.dispose();			
				}});
			 
			 JButton cancelBtn = makeButton("������", null, new ActionListener(){

					@Override
					public void actionPerformed(ActionEvent e) {
						dialog.dispose();			
					}});
			 JPanel btnpanel = new JPanel();
			 btnpanel.setLayout(new FlowLayout());
			 btnpanel.add(okBtn);
			 btnpanel.add(cancelBtn);
			 dialog.add(btnpanel, BorderLayout.SOUTH);
			 String example = "<html>������� ������� \"where\" � \"order by\" ��� sql �������. ������: <br> "
			 		+ "WHERE [��� �������]  &lt 10 <br> ORDER BY [��� �������] </html>";
			 dialog.add(new JLabel(example), BorderLayout.NORTH);
			 
			 dialog.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
			 dialog.pack();
			 dialog.setAlwaysOnTop(true);
			 dialog.setVisible(true);
		}

	}

	/** ������� ����� ������*/
	private JButton makeButton(String name, Icon icon,
			ActionListener actionListener) {
		JButton btn = new JButton(name, icon);
		btn.addActionListener(actionListener);
		
		return btn;
	}
		

	
	
	
	
	
	
	
	
	public void close(){
		model.close();
	}

}
