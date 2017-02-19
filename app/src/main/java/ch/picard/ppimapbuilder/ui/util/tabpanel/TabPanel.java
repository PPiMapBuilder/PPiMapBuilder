package ch.picard.ppimapbuilder.ui.util.tabpanel;

import ch.picard.ppimapbuilder.ui.util.FocusPropagator;

import javax.swing.*;
import java.awt.*;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.event.FocusEvent;
import java.awt.event.FocusListener;
import java.util.ArrayList;
import java.util.Collection;

public class TabPanel<T extends TabContent> extends JPanel implements FocusListener {

	private final JPanel viewportPanel;
	private T activePanel;
	private final Collection<T> tabPanels;
	private final Collection<TabButton> tabButtons;

	public TabPanel(final T... panels) {
		setLayout(new BorderLayout());

		// Top tab bar
		final JPanel topPanel = new JPanel(new GridLayout(1, panels.length));
		tabPanels = new ArrayList<T>();
		tabButtons = new ArrayList<TabButton>();
		boolean first = true;
		for (final T panel : panels) {
			TabButton<T> button = new TabButton<T>(panel, first);
			button.addActionListener(new ActionListener() {
				@Override
				public void actionPerformed(ActionEvent e) {
					setActivePanel(panel);
				}
			});
			tabButtons.add(button);
			topPanel.add(button);
			tabPanels.add(panel);
			first = false;
		}
		topPanel.setPreferredSize(new Dimension(0, 28));
		add(topPanel, BorderLayout.NORTH);

		// Viewport panel
		viewportPanel = new JPanel(new GridLayout(1, 1));
		activePanel = panels[0];
		viewportPanel.add(activePanel.getComponent());
		add(viewportPanel, BorderLayout.CENTER);

		updateButtons();
	}

	private void updateButtons() {
		for (TabButton tabButton : tabButtons)
			tabButton.setActive(tabButton.getText().equals(activePanel.getComponent().getName()));
	}

	public void setActivePanel(T panel) {
		activePanel.setActive(false);
		viewportPanel.remove(activePanel.getComponent());

		activePanel = panel;
		viewportPanel.add(activePanel.getComponent(), BorderLayout.CENTER);
		activePanel.setActive(true);

		updateButtons();
		validate();
		repaint();
	}

	public JPanel getViewportPanel() {
		return viewportPanel;
	}

	public T getActivePanel() {
		return activePanel;
	}

	public Collection<T> getTabPanels() {
		return tabPanels;
	}

	@Override
	public void focusGained(FocusEvent e) {
		for(TabButton button: tabButtons) {
			button.focusGained(e);
		}
	}

	@Override
	public void focusLost(FocusEvent e) {
		for(TabButton button: tabButtons) {
			button.focusLost(e);
		}
	}
}
