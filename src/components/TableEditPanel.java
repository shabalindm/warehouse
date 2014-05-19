package components;

import java.awt.BorderLayout;
import java.awt.FlowLayout;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.util.Arrays;

import javax.swing.Icon;
import javax.swing.JButton;
import javax.swing.JFrame;
import javax.swing.JLabel;
import javax.swing.JPanel;
import javax.swing.JScrollPane;
import javax.swing.JTable;
import javax.swing.JTextArea;

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
		model.updateData();
		
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
			 int [] selectedRows = table.getSelectedRows();
			 int[] rowsToDelete = new int[selectedRows.length];
			 for(int i = 0 ; i < selectedRows.length; i ++ ){				 
				 rowsToDelete[i] =table.convertRowIndexToModel(selectedRows[i]);				 
				 }
			 
			 Arrays.sort(rowsToDelete);
			 for (int i = rowsToDelete.length - 1; i>=0; i--){// ������� ����� ������� � ��������� �������� - ����� � �������� �������� ������ ������� �� ����� ��������
				 model.deleteRow(rowsToDelete[i]);	
			 }			
			 table.repaint();
		 }
	 }

	
	/** ��������� ������ ������ �������� �����*/
	class UnDeleteBtnListener implements ActionListener{
		@Override
		public void actionPerformed(ActionEvent e) {
			for (int rowIndex : table.getSelectedRows())			
				model.unDeleteRow(table.convertRowIndexToModel(rowIndex));
			table.repaint();
		}}
	
	/** ��������� ������ ������ ���������� ������� � ���� */
	public class WriteBtnListener implements ActionListener {

		@Override
		public void actionPerformed(ActionEvent e) {
			writeData();

		}

	}
	
	/** ��������� ������ ������� ������*/
	class InsertBtnListener implements ActionListener{
		@Override
		public void actionPerformed(ActionEvent e) {			
			int viewRow = table.getSelectedRow();
			int modelRow;
			if(viewRow == -1){//������� �� �������
				modelRow = -1; // ��������� ����� ������
			}else if(e.getModifiers() == 17 ){ // - ������ ������� shift
				viewRow--;
				if(viewRow == -1)
					modelRow = -1;
				else
					modelRow = table.convertRowIndexToModel(viewRow);		
			} else {
				modelRow = table.convertRowIndexToModel(viewRow);
			}				
			model.addRow(modelRow);
			int newViewRow = table.convertRowIndexToView(modelRow+1);
			table.setRowSelectionInterval(newViewRow, newViewRow);
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
	protected JButton makeButton(String name, Icon icon,
			ActionListener actionListener) {
		JButton btn = new JButton(name, icon);
		btn.addActionListener(actionListener);
		
		return btn;
	}
	
	
	
	public void close(){
		model.close();
	}

	/**
	 * 
	 */
	public void writeData() {
		int errorRow = model.writeToDB();
		if (errorRow != -1){
			errorRow = table.convertRowIndexToView(errorRow);
			table.setRowSelectionInterval(errorRow, errorRow);
			table.setColumnSelectionInterval(0, model.getColumnCount()-1);				
		} else {
			setState(!COMMITED);
}
	}

}
