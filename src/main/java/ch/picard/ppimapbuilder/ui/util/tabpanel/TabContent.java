package ch.picard.ppimapbuilder.ui.util.tabpanel;

import javax.swing.*;

public interface TabContent {

	public JComponent getComponent();

	public String getName();

	public void setActive(boolean active);

}
