package ch.picard.ppimapbuilder.ui.util.field;

import ch.picard.ppimapbuilder.ui.util.PMBUIStyle;

import javax.swing.*;
import javax.swing.border.EmptyBorder;
import java.awt.*;
import java.awt.event.ActionListener;

public class ListDeletableItem {

	private final JPanel listPanel;
	private final JScrollPane scrollPane;

	public ListDeletableItem() {
		scrollPane = new JScrollPane(listPanel = new JPanel());
		scrollPane.setViewportBorder(PMBUIStyle.emptyBorder);
		scrollPane.setBorder(PMBUIStyle.fancyPanelBorder);

		listPanel.setBackground(Color.white);
		listPanel.setBorder(PMBUIStyle.emptyBorder);
		listPanel.setLayout(new BoxLayout(listPanel, BoxLayout.Y_AXIS));
	}

	public Component getComponent() {
		return scrollPane;
	}

	public void removeAllRow() {
		listPanel.removeAll();
	}

	public void addRow(ListRow row) {
		listPanel.add(row);
	}

	public void repaint() {
		scrollPane.repaint();
	}

	public static class ListRow extends JPanel {
		public ListRow(String text, String tooltip) {
			setLayout(new BoxLayout(this, BoxLayout.LINE_AXIS));
			setOpaque(false);
			setBorder(new EmptyBorder(1, 5, 1, 5));
			if (tooltip != null) setToolTipText(tooltip);

			add(new JLabel(text));
			add(Box.createHorizontalGlue());
		}

		public ListRow(String text) {
			this(text, null);
		}

		public ListRow addButton(JButton button) {
			add(button);
			return this;
		}

		public ListRow addDeleteButton(ActionListener deleteListener) {
			JButton deleteOrga = new DeleteButton();
			deleteOrga.addActionListener(deleteListener);
			return addButton(deleteOrga);
		}


	}
}
