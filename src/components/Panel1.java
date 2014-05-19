package components;

import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;

import javax.swing.JButton;
import javax.swing.JPanel;

import dao.Item;

public class Panel1 extends TableEditPanel<Item[]> {

	private class SearchBtnListener implements ActionListener {

		@Override
		public void actionPerformed(ActionEvent e) {
			for (int veiwRow : table.getSelectedRows()){
				((Model1)model).find(table.convertRowIndexToModel(veiwRow));
			}

		}
	}

	public Panel1(Model1 model) {
		super(model);		
	}

	@Override
	protected void initBtnPanel(JPanel btnPanel) {
		super.initBtnPanel(btnPanel);
		JButton searchBtn = makeButton("�����", null, new SearchBtnListener());
		searchBtn.setToolTipText("������ �������, ��� ������� ����� ���������� �����");
		btnPanel.add(searchBtn);
	}

	
}
