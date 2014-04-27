package tk.nomis_tech.ppimapbuilder.ui.querywindow;

import net.miginfocom.swing.MigLayout;
import org.cytoscape.work.TaskManager;
import tk.nomis_tech.ppimapbuilder.networkbuilder.PMBInteractionNetworkBuildTaskFactory;
import tk.nomis_tech.ppimapbuilder.ui.querywindow.panel.DatabaseSelectionPanel;
import tk.nomis_tech.ppimapbuilder.ui.querywindow.panel.OtherOrganismSelectionPanel;
import tk.nomis_tech.ppimapbuilder.ui.querywindow.panel.ReferenceOrganismSelectionPanel;
import tk.nomis_tech.ppimapbuilder.ui.querywindow.panel.UniprotSelection;
import tk.nomis_tech.ppimapbuilder.data.organism.Organism;
import tk.nomis_tech.ppimapbuilder.data.client.web.interaction.PsicquicService;

import javax.swing.*;
import javax.swing.border.CompoundBorder;
import javax.swing.border.EmptyBorder;
import javax.swing.border.LineBorder;
import javax.swing.border.MatteBorder;
import javax.swing.plaf.basic.BasicSplitPaneDivider;
import javax.swing.plaf.basic.BasicSplitPaneUI;
import java.awt.*;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.event.KeyEvent;
import java.util.ArrayList;
import java.util.List;
/**
 * PPiMapBuilder interaction query window
 */
public class QueryWindow extends JFrame {
	
	/** Instance of the PPiMapBuilder frame to prevent several instances */
	private static final long serialVersionUID = 1L;
	
	// Task management
	private PMBInteractionNetworkBuildTaskFactory createNetworkfactory;
	private TaskManager taskManager;

	// The create network window
	//private JFrame window;

	// Databases and organism panels containing all checkbox
	private UniprotSelection uus;
	private DatabaseSelectionPanel dsp;
	private OtherOrganismSelectionPanel ogs;
	private ReferenceOrganismSelectionPanel org;

	// Fancy design element
	private Color darkForeground;
	private CompoundBorder panelBorder;
	private CompoundBorder fancyBorder;
	
	private JButton startQuery;
	private JButton cancel;

	/** Create the network creation frame */
	public QueryWindow() {
		//window = new JFrame("PPiMapBuilder - Create a network");
		super("PPiMapBuilder - Create a network");
		
		// Create all component in the window
		initialize();
	}

	/*public QueryWindow() {
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
	}*/
	
	/** Initialize the contents of the frame */
	private void initialize() {
		// Slightly darker color than window background color
		darkForeground = UIManager.getColor("Panel.background");
		float hsbVals[] = Color.RGBtoHSB(darkForeground.getRed(), darkForeground.getGreen(), darkForeground.getBlue(), null);
		darkForeground = Color.getHSBColor(hsbVals[0], hsbVals[1], 0.9f * hsbVals[2]);

		// Simple border around panel and text area
		fancyBorder = new CompoundBorder(
			// Outside border 1px bottom light color
			new MatteBorder(0, 0, 1, 0, new Color(255, 255, 255)),
			// Border all around panel 1px dark grey 
			new LineBorder(new Color(154, 154, 154), 1)
		);
		// Border for left and right panel
		panelBorder = new CompoundBorder(
			// Dark margin around panel
			new MatteBorder(5, 0, 0, 0, darkForeground),
			new CompoundBorder(
				fancyBorder,
				new EmptyBorder(5, 5, 5, 5)
		));
		
		// Split panel
		JSplitPane splitPane = new JSplitPane();
		splitPane.setDividerSize(5);
		splitPane.setBorder(new EmptyBorder(0, 0, 0, 0));
		splitPane.setContinuousLayout(true);
		this.getContentPane().add(splitPane, BorderLayout.CENTER);
		// $hide>>$
		// Redraw the split panel divider
		try {
			splitPane.setUI(new BasicSplitPaneUI() {
				public BasicSplitPaneDivider createDefaultDivider() {
					return new BasicSplitPaneDivider(this) {
						private static final long serialVersionUID = 1L;
						
						@Override
						public void paint(Graphics g) {
							super.paint(g);
							g.setColor(darkForeground);
							g.fillRect(0, 0, getSize().width, getSize().height);

							Graphics2D g2d = (Graphics2D) g;
							int h = 12; int w = 2;
							int x = (getWidth() - w) / 2;
							int y = (getHeight() - h) / 2;
							g2d.setColor(new Color(154, 154, 154));
							g2d.drawOval(x, y, w, h);
						}
					};
				}
			});
		} catch (Exception e) {
			e.printStackTrace();
		}
		// $hide<<$
		
		// Left part
		uus = new UniprotSelection(darkForeground, panelBorder, fancyBorder);
		splitPane.setLeftComponent(uus);

		// Right part
		JPanel panMainForm = initMainFormPanel();
		splitPane.setRightComponent(panMainForm);

		// Bottom part
		JPanel panBottomForm = initBottomPanel();
		this.getContentPane().add(panBottomForm, BorderLayout.SOUTH);
		
		initListeners();

		// Resize window
		this.setMinimumSize(new Dimension(500, 300));
		this.setSize(new Dimension(550, 500));

		// Center window
		this.setLocationRelativeTo(null);
	}
	
	/**
	 * Creating the main form panel containing the organism selector and the
	 * source database selector
	 * @return the generated JPanel
	 */
	private JPanel initMainFormPanel() {
		// Main form panel
		JPanel panMainForm = new JPanel();
		panMainForm.setMinimumSize(new Dimension(290, 10));
		panMainForm.setBorder(new CompoundBorder(new MatteBorder(0, 0, 0, 5, darkForeground), panelBorder));
		panMainForm.setLayout(new MigLayout("inset 10", "[49.00,grow][14px:14px:14px,right]", "[][][][grow][][45%]"));

		org = new ReferenceOrganismSelectionPanel(this, panMainForm);
		
		ogs = new OtherOrganismSelectionPanel(panMainForm, darkForeground, panelBorder, fancyBorder);
		
		dsp = new DatabaseSelectionPanel(panMainForm, darkForeground, panelBorder, fancyBorder);

		return panMainForm;
	}
	
	

	public void updateLists(List<PsicquicService> dbs, List<Organism> orgs) {
		dsp.updateList(dbs);
		ogs.updateList(orgs);

		org.updateList(orgs);
	}
	
	/**
	 * Creating bottom panel with cancel and submit button
	 * @return the generated JPanel
	 */
	private JPanel initBottomPanel() {
		JPanel panBottomForm = new JPanel();

		//Bottom Panel
		panBottomForm.setBackground(darkForeground);
		panBottomForm.setPreferredSize(new Dimension(0, 42));
		panBottomForm.setBorder(new EmptyBorder(0, 0, 0, 0));

		//Cancel Button
		cancel = new JButton("Cancel");
		cancel.setMnemonic(KeyEvent.VK_CANCEL);
		panBottomForm.setLayout(new MigLayout("inset 5", "[grow][100px][][100px]", "[29px]"));
		//Add cancel to panel
		panBottomForm.add(cancel, "cell 1 0,alignx center,aligny center");

		//Submit Button
		startQuery = new JButton("Submit");
		//Add submit to panel
		panBottomForm.add(startQuery, "cell 3 0,alignx center,aligny center");

		//Set submit as default button
		this.getRootPane().setDefaultButton(startQuery);

		return panBottomForm;
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
	
	public OtherOrganismSelectionPanel getOgs() {
		return ogs;
	}

}
