package ch.picard.ppimapbuilder.ui.settingwindow.panel;

import ch.picard.ppimapbuilder.data.organism.InParanoidOrganismRepository;
import ch.picard.ppimapbuilder.data.organism.Organism;
import ch.picard.ppimapbuilder.data.organism.UserOrganismRepository;
import ch.picard.ppimapbuilder.ui.settingwindow.SettingWindow;
import ch.picard.ppimapbuilder.ui.util.field.JSearchTextField;
import ch.picard.ppimapbuilder.ui.util.field.ListDeletableItem;
import ch.picard.ppimapbuilder.ui.util.PMBUIStyle;
import ch.picard.ppimapbuilder.ui.util.tabpanel.TabContent;

import javax.swing.*;
import javax.swing.border.EmptyBorder;
import java.awt.*;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.util.ArrayList;

public class OrganismSettingPanel extends JPanel implements TabContent {

	private final ListDeletableItem panSourceOrganism;
	private final JPanel panSearchOrganism;
	private final JSearchTextField searchBox;
	private final SettingWindow owner;

	public OrganismSettingPanel(final SettingWindow owner) {
		super(new BorderLayout());
		setName("Organisms");

		setBorder(new EmptyBorder(5, 5, 5, 5));

		final JLabel lblSourceOrganisms = new JLabel("Preferred organisms:");
		add(lblSourceOrganisms, BorderLayout.NORTH);

		this.owner = owner;
		this.panSourceOrganism = new ListDeletableItem();
		add(panSourceOrganism.getComponent());

		this.panSearchOrganism = new JPanel();
		{
			this.searchBox = new JSearchTextField(owner);
			this.searchBox.setIcon(JSearchTextField.class.getResource("search.png"));
			this.searchBox.setMinimumSize(new Dimension(200, 30));
			this.searchBox.setPreferredSize(new Dimension(200, 30));
			this.searchBox.setMaximumSize(new Dimension(200, 30));
			this.searchBox.setBorder(PMBUIStyle.fancyPanelBorder);

			this.searchBox.setSuggestWidth(150);
			this.searchBox.setPreferredSuggestSize(new Dimension(150, 50));
			this.searchBox.setMinimumSuggestSize(new Dimension(150, 50));
			this.searchBox.setMaximumSuggestSize(new Dimension(150, 50));

			this.panSearchOrganism.add(searchBox);

			JButton addOrganism = new JButton("Add");
			addOrganism.addActionListener(new ActionListener() {

				@Override
				public void actionPerformed(ActionEvent e) {
					UserOrganismRepository.getInstance().addOrganism(searchBox.getText());
					owner.newModificationMade();
					validate();
				}

			});
			this.panSearchOrganism.add(addOrganism);
		}

		add(panSearchOrganism, BorderLayout.SOUTH);
	}

	@Override
	public void validate() {
		panSourceOrganism.removeAllRow();
		for (final Organism org : UserOrganismRepository.getInstance().getOrganisms()) {
			panSourceOrganism.addRow(
					new ListDeletableItem.ListRow(org.getScientificName(), "Taxonomy ID: " + org.getTaxId())
							.addDeleteButton(new ActionListener() {

								@Override
								public void actionPerformed(ActionEvent e) {
									//System.out.println(org.getScientificName()+" clicked");
									UserOrganismRepository.getInstance().removeOrganismExceptLastOne(org.getScientificName());
									validate();
									owner.newModificationMade();
								}
							})
			);
		}

		ArrayList<String> data = new ArrayList<String>(InParanoidOrganismRepository.getInstance().getOrganismNames());
		for (Organism o : UserOrganismRepository.getInstance().getOrganisms()) {
			data.remove(o.getScientificName());
		}
		searchBox.setSuggestData(data);
		searchBox.setText("");

		panSourceOrganism.repaint();
		super.validate();
	}

	@Override
	public void setVisible(boolean opening) {
		super.setVisible(opening);
		if (opening) {
			validate();
		}
	}

	@Override
	public JComponent getComponent() {
		return this;
	}
}
