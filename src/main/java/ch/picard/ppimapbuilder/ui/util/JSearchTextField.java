package ch.picard.ppimapbuilder.ui.util;

/*
 * Copyright 2010 Georgios Migdos <cyberpython@gmail.com>.
 * 
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 * 
 * http://www.apache.org/licenses/LICENSE-2.0
 * 
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 * under the License.
 */

import ch.picard.ppimapbuilder.ui.settingwindow.SettingWindow;

import javax.swing.*;
import java.awt.*;
import java.awt.event.*;
import java.util.*;
import java.util.List;

/**
 * @authorGeorgios Migdos <cyberpython@gmail.com>
 */
public class JSearchTextField extends JIconTextField implements FocusListener {

	private String textWhenNotFocused;

	/**
	 * Dialog used as the drop-down list.
	 */
	private JDialog d;

	/**
	 * Location of said drop-down list.
	 */
	private Point location;

	/**
	 * List contained in the drop-down dialog.
	 */
	private JList list;

	/**
	 * Vectors containing the original data and the filtered data for the
	 * suggestions.
	 */
	private List<String> data, suggestions;

	/**
	 * Separate matcher-thread, prevents the text-field from hanging while the
	 * suggestions are beeing prepared.
	 */
	private InterruptableMatcher matcher;

	/**
	 * Fonts used to indicate that the text-field is processing the request,
	 * i.e. looking for matches
	 */
	private Font busy, regular;

	/**
	 * Needed for the new narrowing search, so we know when to reset the list
	 */
	private String lastWord = "";

	/**
	 * The last chosen variable which exists. Needed if user
	 * continued to type but didn't press the enter key
	 */
	private String lastChosenExistingVariable;

	/**
	 * Hint that will be displayed if the field is empty
	 */
	private String hint;

	/**
	 * Listeners, fire event when a selection as occured
	 */
	private LinkedList<ActionListener> listeners;

	private SuggestMatcher suggestMatcher = new ContainsMatcher();

	private boolean caseSensitive = false;


	public JSearchTextField(SettingWindow owner) {
		super();
		this.textWhenNotFocused = "Search...";
		this.addFocusListener(this);

		data = new ArrayList<String>();
		suggestions = new ArrayList<String>();
		listeners = new LinkedList<ActionListener>();

		owner.addComponentListener(new ComponentListener() {
			@Override
			public void componentShown(ComponentEvent e) {
				relocate();
			}

			@Override
			public void componentResized(ComponentEvent e) {
				relocate();
			}

			@Override
			public void componentMoved(ComponentEvent e) {
				relocate();
			}

			@Override
			public void componentHidden(ComponentEvent e) {
				relocate();
			}
		});

		owner.addWindowListener(new WindowListener() {

			@Override
			public void windowIconified(WindowEvent e) {
				d.setVisible(false);
			}

			@Override
			public void windowClosing(WindowEvent e) {
				d.dispose();
			}

			@Override
			public void windowClosed(WindowEvent e) {
				d.dispose();
			}

			@Override
			public void windowActivated(WindowEvent e) {}
			@Override
			public void windowDeiconified(WindowEvent e) {}
			@Override
			public void windowDeactivated(WindowEvent e) {}
			@Override
			public void windowOpened(WindowEvent e) {}
		});

		addFocusListener(new FocusListener() {
			@Override
			public void focusLost(FocusEvent e) {
				d.setVisible(false);

				if (getText().equals("") && e.getOppositeComponent() != null && e.getOppositeComponent().getName() != null) {
					if (!e.getOppositeComponent().getName().equals("suggestFieldDropdownButton")) {
						setText(hint);
					}
				} else if (getText().equals("")) {
					setText(hint);
				}
			}

			@Override
			public void focusGained(FocusEvent e) {
				if (getText().equals(hint)) {
					setText("");
				}

				if (getText().length() >= 3)
					showSuggest();
				else
					hideSuggest();
			}
		});


		d = new JDialog(owner);
		d.setUndecorated(true);
		d.setFocusableWindowState(false);
		d.setFocusable(false);
		list = new JList();
		list.setOpaque(false);
		list.addMouseListener(new MouseListener() {
			private int selected;

			@Override
			public void mouseReleased(MouseEvent e) {
				if (selected == list.getSelectedIndex()) {
					// provide double-click for selecting a suggestion
					setText((String) list.getSelectedValue());
					lastChosenExistingVariable = list.getSelectedValue().toString();
					fireActionEvent();
					d.setVisible(false);
				}
				selected = list.getSelectedIndex();
			}

			@Override
			public void mousePressed(MouseEvent e) {}
			@Override
			public void mouseExited(MouseEvent e) {}
			@Override
			public void mouseEntered(MouseEvent e) {}
			@Override
			public void mouseClicked(MouseEvent e) {}
		});
		list.setBorder(PMBUIStyle.emptyBorder);
		JScrollPane scrollPane = new JScrollPane(list, JScrollPane.VERTICAL_SCROLLBAR_AS_NEEDED,
				JScrollPane.HORIZONTAL_SCROLLBAR_NEVER);
		scrollPane.setViewportBorder(PMBUIStyle.emptyBorder);
		scrollPane.setBorder(PMBUIStyle.defaultComponentBorder);
		d.add(scrollPane);
		d.pack();
		addKeyListener(new KeyListener() {

			@Override
			public void keyPressed(KeyEvent e) {
				relocate();
			}

			@Override
			public void keyReleased(KeyEvent e) {
				if (e.getKeyCode() == KeyEvent.VK_ESCAPE) {
					d.setVisible(false);
					return;
				} else if (e.getKeyCode() == KeyEvent.VK_DOWN) {
					if (d.isVisible()) {
						list.setSelectedIndex(list.getSelectedIndex() + 1);
						list.ensureIndexIsVisible(list.getSelectedIndex() + 1);
						return;
					} else {
						if (getText().length() >= 3)
							showSuggest();
						else
							hideSuggest();
					}
				} else if (e.getKeyCode() == KeyEvent.VK_UP) {
					list.setSelectedIndex(list.getSelectedIndex() - 1);
					list.ensureIndexIsVisible(list.getSelectedIndex() - 1);
					return;
				} else if (e.getKeyCode() == KeyEvent.VK_ENTER & list.getSelectedIndex() != -1 & suggestions.size() > 0) {
					fireEnterPressed();
					return;
				}
				if (getText().length() >= 3)
					showSuggest();
				else
					hideSuggest();
			}

			@Override
			public void keyTyped(KeyEvent e) {}
		});
		regular = getFont();
		busy = new Font(getFont().getName(), Font.ITALIC, getFont().getSize());

		try {
			location = getLocationOnScreen();
			location.y += 30;
			d.setLocation(location);
		} catch (IllegalComponentStateException e) {}
	}

	public void fireEnterPressed() {
		if (list.getSelectedValue() != null) {
			setText((String) list.getSelectedValue());
			lastChosenExistingVariable = list.getSelectedValue().toString();
		}
		fireActionEvent();
		d.setVisible(false);
	}

	/**
	 * Use ActionListener to notify on changes
	 * so we don't have to create an extra event
	 */
	private void fireActionEvent() {
		ActionEvent event = new ActionEvent(this, 0, getText());
		for (ActionListener listener : listeners) {
			listener.actionPerformed(event);
		}
	}

	/**
	 * Set preferred size for the drop-down that will appear.
	 *
	 * @param size Preferred size of the drop-down list
	 */
	public void setPreferredSuggestSize(Dimension size) {
		d.setPreferredSize(size);
	}

	public void setSuggestWidth(int width) {
		d.setPreferredSize(new Dimension(width, d.getHeight()));
		d.setMinimumSize(new Dimension(width, d.getHeight()));
	}

	/**
	 * Set minimum size for the drop-down that will appear.
	 *
	 * @param size Minimum size of the drop-down list
	 */
	public void setMinimumSuggestSize(Dimension size) {
		d.setMinimumSize(size);
	}

	/**
	 * Set maximum size for the drop-down that will appear.
	 *
	 * @param size Maximum size of the drop-down list
	 */
	public void setMaximumSuggestSize(Dimension size) {
		d.setMaximumSize(size);
	}

	/**
	 * Sets new data used to suggest similar words.
	 *
	 * @param data Vector containing available words
	 * @return success, true unless the data-vector was null
	 */
	public boolean setSuggestData(List<String> data) {
		if (data == null) {
			return false;
		}
		Collections.sort(data);
		this.data = data;
		list.setListData(data.toArray());
		return true;
	}

	public String getTextWhenNotFocused() {
		return this.textWhenNotFocused;
	}

	public void setTextWhenNotFocused(String newText) {
		this.textWhenNotFocused = newText;
	}

	@Override
	protected void paintComponent(Graphics g) {
		super.paintComponent(g);

		if (!this.hasFocus() && this.getText().equals("")) {
			int width = this.getWidth();
			int height = this.getHeight();
			Font prev = g.getFont();
			Font italic = prev.deriveFont(Font.ITALIC);
			Color prevColor = g.getColor();
			g.setFont(italic);
			g.setColor(UIManager.getColor("textInactiveText"));
			int h = g.getFontMetrics().getHeight();
			int textBottom = (height - h) / 2 + h - 4;
			int x = this.getInsets().left;
			Graphics2D g2d = (Graphics2D) g;
			RenderingHints hints = g2d.getRenderingHints();
			g2d.setRenderingHint(RenderingHints.KEY_TEXT_ANTIALIASING, RenderingHints.VALUE_TEXT_ANTIALIAS_ON);
			g2d.drawString(textWhenNotFocused, x, textBottom);
			g2d.setRenderingHints(hints);
			g.setFont(prev);
			g.setColor(prevColor);
		}

	}

	//FocusListener implementation:
	public void focusGained(FocusEvent e) {
		this.repaint();
	}

	public void focusLost(FocusEvent e) {
		this.repaint();
	}

	/**
	 * Force the suggestions to be displayed (Useful for buttons
	 * e.g. for using JSuggestionField like a ComboBox)
	 */
	public void showSuggest() {
		if (!getText().toLowerCase().contains(lastWord.toLowerCase())) {
			suggestions.clear();
		}
		if (suggestions.isEmpty()) {
			suggestions.addAll(data);
		}
		if (matcher != null) {
			matcher.stop = true;
		}
		matcher = new InterruptableMatcher();
		//matcher.start();
		SwingUtilities.invokeLater(matcher);
		lastWord = getText();
		relocate();
	}

	/**
	 * Force the suggestions to be hidden (Useful for buttons, e.g. to use
	 * JSuggestionField like a ComboBox)
	 */
	public void hideSuggest() {
		d.setVisible(false);
	}


	/**
	 * Place the suggestion window under the JTextField.
	 */
	private void relocate() {
		try {
			location = getLocationOnScreen();
			location.y += getHeight();
			d.setLocation(location);
		} catch (IllegalComponentStateException e) {
		}
	}


	// MATCHER CLASSES

	/**
	 * Inner class providing the independent matcher-thread. This thread can be
	 * interrupted, so it won't process older requests while there's already a
	 * new one.
	 */
	private class InterruptableMatcher extends Thread {
		/**
		 * flag used to stop the thread
		 */
		private volatile boolean stop;

		/**
		 * Standard run method used in threads
		 * responsible for the actual search
		 */
		@Override
		public void run() {
			try {
				setFont(busy);
				Iterator<String> it = suggestions.iterator();
				String word = getText();
				while (it.hasNext()) {
					if (stop) {
						return;
					}
					// rather than using the entire list, let's rather remove
					// the words that don't match, thus narrowing
					// the search and making it faster
					if (caseSensitive) {
						if (!suggestMatcher.matches(it.next(), word)) it.remove();
					} else {
						if (!suggestMatcher.matches(it.next().toLowerCase(), word.toLowerCase())) it.remove();
					}
				}
				setFont(regular);
				if (suggestions.size() > 0) {
					list.setListData(suggestions.toArray());
					list.setSelectedIndex(0);
					list.ensureIndexIsVisible(0);
					d.setVisible(true);
				} else {
					d.setVisible(false);
				}
			} catch (Exception e) {
				// Despite all precautions, external changes have occurred.
				// Let the new thread handle it...
			}
		}
	}

	public interface SuggestMatcher {
		public boolean matches(String dataWord, String searchWord);
	}

	public class ContainsMatcher implements SuggestMatcher {
		@Override
		public boolean matches(String dataWord, String searchWord) {
			return dataWord.contains(searchWord);
		}
	}

}