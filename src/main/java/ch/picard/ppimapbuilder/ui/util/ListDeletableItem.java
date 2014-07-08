package ch.picard.ppimapbuilder.ui.util;

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
		public ListRow(String text, ActionListener deleteListener) {
			ImageIcon icon = new ImageIcon(ListDeletableItem.class.getResource("delete.png"));

			JButton deleteOrga = new JButton(icon);
			Dimension iconDim = new Dimension(icon.getIconWidth()+2, icon.getIconHeight()+2);
			deleteOrga.setMinimumSize(iconDim);
			deleteOrga.setMaximumSize(iconDim);
			deleteOrga.setPreferredSize(iconDim);
			deleteOrga.setContentAreaFilled(false);
			deleteOrga.setBorder(BorderFactory.createEmptyBorder());
			deleteOrga.addActionListener(deleteListener);

			setLayout(new BoxLayout(this, BoxLayout.LINE_AXIS));
			setOpaque(false);
			setBorder(new EmptyBorder(1, 5, 1, 5));

			add(new JLabel(text));
			add(Box.createHorizontalGlue());
			add(deleteOrga);
		}
	}
}
