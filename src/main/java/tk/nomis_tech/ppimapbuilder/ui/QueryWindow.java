package tk.nomis_tech.ppimapbuilder.ui;

import java.awt.BorderLayout;
import java.awt.GridLayout;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.io.IOException;
import java.io.InputStream;
import java.util.Collection;
import java.util.List;

import javax.swing.JButton;
import javax.swing.JFrame;
import javax.swing.JPanel;

import org.hupo.psi.mi.psicquic.registry.ServiceType;
import org.hupo.psi.mi.psicquic.wsclient.PsicquicSimpleClient;

import psidev.psi.mi.tab.PsimiTabException;
import psidev.psi.mi.tab.PsimiTabReader;
import psidev.psi.mi.tab.model.BinaryInteraction;
import tk.nomis_tech.ppimapbuilder.ui.panel.DatabaseSelectionPanel;

/**
 * PPiMapBuilder interaction query window
 */
public class QueryWindow extends JFrame {

	private static final long serialVersionUID = 1L;
	private JButton startQuery;
	private JButton cancel;
	private DatabaseSelectionPanel dsp;

	public QueryWindow() {
		setTitle("PPiMapBuilder Query");
		setLayout(new BorderLayout());
		
		add(initMainPanel(), BorderLayout.CENTER);
		add(initBottomPanel(), BorderLayout.SOUTH);
		getRootPane().setDefaultButton(startQuery);

		initListeners();
		
		setBounds(0, 0, 400, 200);
		setResizable(false);
		setLocationRelativeTo(JFrame.getFrames()[0]);
	}

	public void updateLists(List<ServiceType> dbs) {
		dsp.updateList(dbs);
	}

	private JPanel initMainPanel() {
		JPanel main = new JPanel(new BorderLayout());

		dsp = new DatabaseSelectionPanel();
		main.add(dsp, BorderLayout.CENTER);

		return main;
	}

	private JPanel initBottomPanel() {
		JPanel bottom = new JPanel(new GridLayout(1,1));

		cancel = new JButton("Cancel");
		startQuery = new JButton("Ok");

		bottom.add(cancel);
		bottom.add(startQuery);

		return bottom;
	}

	private void initListeners() {
		startQuery.addActionListener(new ActionListener() {

			public void actionPerformed(ActionEvent e) {
				try {
					PsicquicSimpleClient client = new PsicquicSimpleClient(
							"http://www.ebi.ac.uk/Tools/webservices/psicquic/intact/webservices/current/search/");

					PsimiTabReader mitabReader = new PsimiTabReader();

					InputStream result = client.getByQuery("brca2");

					Collection<BinaryInteraction> binaryInteractions = mitabReader
							.read(result);

					System.out.println("Interactions found: "
							+ binaryInteractions.size());

				} catch (IOException ex) {
					ex.printStackTrace();
				} catch (PsimiTabException e1) {
					e1.printStackTrace();
				}

				QueryWindow.this.setVisible(false);
				QueryWindow.this.dispose();
			}

		});
		cancel.addActionListener(new ActionListener() {
			
			@Override
			public void actionPerformed(ActionEvent arg0) {
				QueryWindow.this.setVisible(false);
				QueryWindow.this.dispose();
			}
		});
	}
}
