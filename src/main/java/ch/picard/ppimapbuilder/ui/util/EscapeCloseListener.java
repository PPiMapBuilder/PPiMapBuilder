package ch.picard.ppimapbuilder.ui.util;

import java.awt.KeyEventDispatcher;
import java.awt.event.KeyEvent;
import javax.swing.JFrame;

/**
 *
 * @author CORNUT, CRESSANT, DUPUIS, GRAVOUIL
 *
 */
public class EscapeCloseListener implements KeyEventDispatcher {

    private JFrame window;

    public EscapeCloseListener(JFrame window) {
        this.window = window;
    }

    @Override
    public boolean dispatchKeyEvent(KeyEvent e) {
        if (e.getKeyCode() == KeyEvent.VK_ESCAPE) {
            window.setVisible(false);
        }
        return false;
    }
}