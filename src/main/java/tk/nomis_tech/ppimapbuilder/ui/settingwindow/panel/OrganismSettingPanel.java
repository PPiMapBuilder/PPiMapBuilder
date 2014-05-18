package tk.nomis_tech.ppimapbuilder.ui.settingwindow.panel;

import java.awt.*;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.event.MouseEvent;
import java.awt.event.MouseListener;
import java.awt.geom.Rectangle2D;
import java.util.ArrayList;
import java.util.LinkedHashMap;

import javax.swing.BorderFactory;
import javax.swing.Box;
import javax.swing.BoxLayout;
import javax.swing.ImageIcon;
import javax.swing.JButton;
import javax.swing.JCheckBox;
import javax.swing.JLabel;
import javax.swing.JPanel;
import javax.swing.JScrollPane;
import javax.swing.JTextField;
import javax.swing.border.Border;
import javax.swing.border.EmptyBorder;

import tk.nomis_tech.ppimapbuilder.data.client.web.interaction.PsicquicService;
import tk.nomis_tech.ppimapbuilder.data.organism.InParanoidOrganismRepository;
import tk.nomis_tech.ppimapbuilder.data.organism.Organism;
import tk.nomis_tech.ppimapbuilder.data.organism.UserOrganismRepository;
import tk.nomis_tech.ppimapbuilder.data.settings.PMBSettings;
import tk.nomis_tech.ppimapbuilder.ui.settingwindow.SettingWindow;
import tk.nomis_tech.ppimapbuilder.ui.util.HelpIcon;
import tk.nomis_tech.ppimapbuilder.ui.util.JSearchTextField;
import tk.nomis_tech.ppimapbuilder.ui.util.LogoIcon;

public class OrganismSettingPanel extends TabContentPanel {

	private final JPanel panSourceOrganism;
	private final JPanel panSearchOrganism;
	private JButton addOrganism;
	private JSearchTextField searchBox;
	
	public OrganismSettingPanel(SettingWindow owner) {
		super(new BorderLayout(), "Organisms");

		setBorder(new EmptyBorder(5, 5, 5, 5));

		final JLabel lblSourceOrganisms = new JLabel("Preferred organisms:");
		add(lblSourceOrganisms, BorderLayout.NORTH);

		panSourceOrganism = new JPanel();
		panSourceOrganism.setBackground(Color.white);
		panSourceOrganism.setBorder(new EmptyBorder(0, 0, 0, 0));

		panSourceOrganism.setLayout(new BoxLayout(panSourceOrganism, BoxLayout.Y_AXIS));

		updatePanSourceOrganism();
		
		// Source databases scrollpane containing a panel that will contain checkbox at display
		final JScrollPane scrollPaneSourceOrganisms = new JScrollPane(panSourceOrganism);
		scrollPaneSourceOrganisms.setViewportBorder(new EmptyBorder(0, 0, 0, 0));
		add(scrollPaneSourceOrganisms, BorderLayout.CENTER);

		panSearchOrganism = new JPanel();
		
		searchBox = new JSearchTextField(owner);
		searchBox.setIcon(JSearchTextField.class.getResource("search.png"));
		searchBox.setMinimumSize(new Dimension(200, 30));
		searchBox.setPreferredSize(new Dimension(200, 30));
		searchBox.setMaximumSize(new Dimension(200, 30));
		
		updateSuggestions();		
		
		searchBox.setSuggestWidth(150);
		searchBox.setPreferredSuggestSize(new Dimension(150, 50));
		searchBox.setMinimumSuggestSize(new Dimension(150, 50));
		searchBox.setMaximumSuggestSize(new Dimension(150, 50));
		
		
		panSearchOrganism.add(searchBox);
		
		addOrganism = new JButton("Add");
		addOrganism.addActionListener(new ActionListener() {
			
			@Override
			public void actionPerformed(ActionEvent e) {
				UserOrganismRepository.getInstance().addOrganism(searchBox.getText());
				updatePanSourceOrganism();
				updateSuggestions();
			}
			
		});
		panSearchOrganism.add(addOrganism);
		
		add(panSearchOrganism, BorderLayout.SOUTH);
	}
	
	public void updatePanSourceOrganism() {
		panSourceOrganism.removeAll();
		for (final Organism org : UserOrganismRepository.getInstance().getOrganisms()) {
			
			ImageIcon icon = new ImageIcon(OrganismSettingPanel.class.getResource("delete.png"));
			JLabel nameLabel = new JLabel(org.getScientificName());
			JButton deleteOrga = new JButton(icon);
			Dimension iconDim = new Dimension(icon.getIconWidth()+2, icon.getIconHeight()+2);
			deleteOrga.setMinimumSize(iconDim);
			deleteOrga.setMaximumSize(iconDim);
			deleteOrga.setPreferredSize(iconDim);
			deleteOrga.setContentAreaFilled(false);
			deleteOrga.setBorder(BorderFactory.createEmptyBorder());
			deleteOrga.addActionListener(new ActionListener() {
				
				@Override
				public void actionPerformed(ActionEvent e) {
					//System.out.println(org.getScientificName()+" clicked");
					UserOrganismRepository.getInstance().removeOrganismExceptLastOne(org.getScientificName());
					updatePanSourceOrganism();
					updateSuggestions();
				}
			});
			
			JPanel panOrgaName = new JPanel();
			panOrgaName.setLayout(new BoxLayout(panOrgaName, BoxLayout.LINE_AXIS));
			panOrgaName.add(nameLabel);
			panOrgaName.add(Box.createHorizontalGlue());
			panOrgaName.add(deleteOrga);
			
			
			panSourceOrganism.add(panOrgaName);
		}
	}
	
	public void updateSuggestions() {
		ArrayList<String> data = new ArrayList<String>();
		data = (ArrayList<String>) InParanoidOrganismRepository.getInstance().getOrganismNames();
		for (Organism o : UserOrganismRepository.getInstance().getOrganisms()) {
			data.remove(o.getScientificName());
		}
		searchBox.setSuggestData(data);
		searchBox.setText("");
		searchBox.repaint();
		//addOrganism.requestFocusInWindow();
		
	}
}
