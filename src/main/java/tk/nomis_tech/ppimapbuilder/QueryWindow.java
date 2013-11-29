package tk.nomis_tech.ppimapbuilder;

import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;

import javax.swing.JButton;
import javax.swing.JFrame;

/**
 * PPiMapBuilder interaction query window
 */
public class QueryWindow extends JFrame {

	private static final long serialVersionUID = 1L;

	public QueryWindow() {
		setTitle("Interaction Query");
		
		JButton startQuery = new JButton("Start");
		startQuery.addActionListener(new ActionListener() {
			
			public void actionPerformed(ActionEvent e) {
				
			}
			
		});
		
		getContentPane().add(startQuery);
		getRootPane().setDefaultButton(startQuery);
		
		setLocationRelativeTo(null);
	}
}
