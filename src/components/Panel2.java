package components;

import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;

import javax.swing.JButton;
import javax.swing.JPanel;

import dao.Item;

public class Panel2 extends TableEditPanel<Item[]> {

	private class btnListener implements ActionListener {

		@Override
		public void actionPerformed(ActionEvent e) {
			for (int veiwRow : table.getSelectedRows()){
				((Model2)model).find(table.convertRowIndexToModel(veiwRow));
			}

		}
	}

	public Panel2(Model2 model) {
		super(model);		
	}

	@Override
	protected void initBtnPanel(JPanel btnPanel) {
		super.initBtnPanel(btnPanel);
		JButton btntn = makeButton("Проверить заявки", null, new btnListener());
		btntn.setToolTipText("Ищет среди имеющихся или создает новую заявку. Выдели нужные строчки");
		btnPanel.add(btntn);
	}

	
}
