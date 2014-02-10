package tk.nomis_tech.ppimapbuilder.ui.querywindow;

import java.awt.BorderLayout;
import java.awt.Dimension;
import java.awt.GridLayout;
import java.awt.Rectangle;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.util.ArrayList;
import java.util.List;

import javax.swing.BoxLayout;
import javax.swing.JButton;
import javax.swing.JFrame;
import javax.swing.JPanel;

import org.cytoscape.work.TaskManager;

import tk.nomis_tech.ppimapbuilder.networkbuilder.PMBInteractionNetworkBuildTaskFactory;
import tk.nomis_tech.ppimapbuilder.util.Organism;
import tk.nomis_tech.ppimapbuilder.ui.querywindow.panel.DatabaseSelectionPanel;
import tk.nomis_tech.ppimapbuilder.ui.querywindow.panel.OtherOrganismSelectionPanel;
import tk.nomis_tech.ppimapbuilder.ui.querywindow.panel.ReferenceOrganismSelectionPanel;
import tk.nomis_tech.ppimapbuilder.ui.querywindow.panel.UniprotSelection;
import tk.nomis_tech.ppimapbuilder.webservice.PsicquicService;

/**
 * PPiMapBuilder interaction query window
 */
public class QueryWindow extends JFrame {

	private static final long serialVersionUID = 1L;
	private JButton startQuery;
	private JButton cancel;
	private DatabaseSelectionPanel dsp;
	private UniprotSelection uus;
	private PMBInteractionNetworkBuildTaskFactory createNetworkfactory;
	private TaskManager taskManager;
	private OtherOrganismSelectionPanel ogs;
	private ReferenceOrganismSelectionPanel org;

	public QueryWindow() {
		setTitle("PPiMapBuilder Query");
		setLayout(new BorderLayout());
		setMinimumSize(new Dimension(800, 600));

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

	public void updateLists(List<PsicquicService> dbs, List<Organism> orgs) {
		dsp.updateList(dbs);
		ogs.updateList(orgs);

		org.updateList(orgs);
	}

	private JPanel initMainPanel() {
		JPanel main = new JPanel();
		main.setLayout(new BoxLayout(main, BoxLayout.Y_AXIS));

		uus = new UniprotSelection();
		main.add(uus);

		dsp = new DatabaseSelectionPanel();
		main.add(dsp);

		ogs = new OtherOrganismSelectionPanel();
		main.add(ogs);

		org = new ReferenceOrganismSelectionPanel();
		main.add(org);

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

	public Organism getSelectedRefOrganism() {
		return org.getSelectedOrganism();
	}

	public ArrayList<String> getSelectedUniprotID() {
		return uus.getIdentifers();
	}

	public List<Organism> getSelectedOrganisms() {
		return ogs.getSelectedOrganisms();
	}

	public void setCreateNetworkfactory(
		PMBInteractionNetworkBuildTaskFactory createNetworkfactory) {
		this.createNetworkfactory = createNetworkfactory;
	}

	public void setTaskManager(TaskManager taskManager) {
		this.taskManager = taskManager;
	}

}