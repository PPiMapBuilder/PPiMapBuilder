package tk.nomis_tech.ppimapbuilder;

import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;

import javax.swing.JButton;
import javax.swing.JFrame;

public class QueryWindow extends JFrame {

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
