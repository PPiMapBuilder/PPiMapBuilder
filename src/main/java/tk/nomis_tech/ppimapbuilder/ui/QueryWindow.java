package tk.nomis_tech.ppimapbuilder.ui;

import java.awt.BorderLayout;
import java.awt.Dimension;
import java.awt.GridLayout;
import java.awt.Rectangle;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.io.IOException;
import java.io.InputStream;
import java.util.ArrayList;
import java.util.Collection;
import java.util.List;
import java.util.logging.Level;
import java.util.logging.Logger;

import javax.swing.JButton;
import javax.swing.JFrame;
import javax.swing.JPanel;

import org.cytoscape.work.Task;
import org.cytoscape.work.TaskManager;
import org.hupo.psi.mi.psicquic.wsclient.PsicquicSimpleClient;

import psidev.psi.mi.tab.PsimiTabException;
import psidev.psi.mi.tab.PsimiTabReader;
import psidev.psi.mi.tab.model.BinaryInteraction;
import tk.nomis_tech.ppimapbuilder.networkbuilder.PMBInteractionNetworkBuildTaskFactory;
import tk.nomis_tech.ppimapbuilder.ui.panel.DatabaseSelectionPanel;
import tk.nomis_tech.ppimapbuilder.util.PsicquicService;

/**
 * PPiMapBuilder interaction query window
 */
public class QueryWindow extends JFrame {

	private static final long serialVersionUID = 1L;
	private JButton startQuery;
	private JButton cancel;
	private DatabaseSelectionPanel dsp;
	private PMBInteractionNetworkBuildTaskFactory createNetworkfactory;
	private TaskManager taskManager;

	public QueryWindow(PMBInteractionNetworkBuildTaskFactory createNetworkfactory, TaskManager taskManager) {
		setTitle("PPiMapBuilder Query");
		setLayout(new BorderLayout());
		
		this.createNetworkfactory = createNetworkfactory;
		this.taskManager = taskManager;

		add(initMainPanel(), BorderLayout.CENTER);
		add(initBottomPanel(), BorderLayout.SOUTH);
		getRootPane().setDefaultButton(startQuery);

		initListeners();

		Dimension d = new Dimension(300, 200);
		setBounds(new Rectangle(d));
		setMinimumSize(d);
		setResizable(true);
		setLocationRelativeTo(JFrame.getFrames()[0]);
	}

	public QueryWindow() {
		setTitle("PPiMapBuilder Query");
		setLayout(new BorderLayout());

		add(initMainPanel(), BorderLayout.CENTER);
		add(initBottomPanel(), BorderLayout.SOUTH);
		getRootPane().setDefaultButton(startQuery);

		initListeners();

		Dimension d = new Dimension(300, 200);
		setBounds(new Rectangle(d));
		setMinimumSize(d);
		setResizable(true);
		setLocationRelativeTo(JFrame.getFrames()[0]);
	}

	public void updateLists(List<PsicquicService> dbs) {
		dsp.updateList(dbs);
	}

	private JPanel initMainPanel() {
		JPanel main = new JPanel(new BorderLayout());

		dsp = new DatabaseSelectionPanel();
		main.add(dsp, BorderLayout.CENTER);

		return main;
	}

	private JPanel initBottomPanel() {
		JPanel bottom = new JPanel(new GridLayout(1, 1));

		cancel = new JButton("Cancel");
		startQuery = new JButton("Ok");

		bottom.add(cancel);
		bottom.add(startQuery);

		return bottom;
	}

	private void initListeners() {
		startQuery.addActionListener(new ActionListener() {

			public void actionPerformed(ActionEvent e) {

				QueryWindow.this.setVisible(false);
				QueryWindow.this.dispose();
				
				taskManager.execute(createNetworkfactory.createTaskIterator());
				
				
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

	public List<PsicquicService> getSelectedDatabases() {
		return dsp.getSelectedDatabases();
	}

	public void setCreateNetworkfactory(
			PMBInteractionNetworkBuildTaskFactory createNetworkfactory) {
		this.createNetworkfactory = createNetworkfactory;
	}

	public void setTaskManager(TaskManager taskManager) {
		this.taskManager = taskManager;
	}

}
