package ch.picard.ppimapbuilder.networkbuilder.query;

import ch.picard.ppimapbuilder.data.interaction.client.web.InteractionUtils;
import ch.picard.ppimapbuilder.data.interaction.client.web.ThreadedPsicquicSimpleClient;
import ch.picard.ppimapbuilder.data.organism.Organism;
import ch.picard.ppimapbuilder.data.protein.Protein;
import ch.picard.ppimapbuilder.data.protein.UniProtEntrySet;
import psidev.psi.mi.tab.model.BinaryInteraction;
import uk.ac.ebi.enfin.mi.cluster.EncoreInteraction;

import java.util.ArrayList;
import java.util.List;
import java.util.Set;
import java.util.concurrent.Callable;

class SecondaryInteractionQuery implements Callable<SecondaryInteractionQuery> {

	private final Organism organism;
	private final UniProtEntrySet proteinPool;

	private final ThreadedPsicquicSimpleClient psicquicClient;

	private final List<EncoreInteraction> interactions;

	SecondaryInteractionQuery(
			Organism organism, UniProtEntrySet proteinPool,
			ThreadedPsicquicSimpleClient psicquicClient
	) {
		this.organism = organism;
		this.proteinPool = proteinPool;

		this.psicquicClient = psicquicClient;

		this.interactions = new ArrayList<EncoreInteraction>();
	}

	public SecondaryInteractionQuery call() throws Exception {
		//Get proteins in the current organism
		final Set<Protein> proteins = proteinPool.getInOrg(organism);

		//Get secondary interactions
		List<BinaryInteraction> interactionsBinary = psicquicClient.getInteractionsInProteinPool(proteins, organism);

		//Filter non uniprot and non current organism
		interactionsBinary = InteractionUtils.filter(
				interactionsBinary,
				new InteractionUtils.UniProtInteractionFilter(),
				new InteractionUtils.OrganismInteractionFilter(organism)
		);

		//Cluster
		interactions.addAll(InteractionUtils.clusterInteraction(
				interactionsBinary
		));

		return this;
	}

	public Organism getOrganism() {
		return organism;
	}

	public List<EncoreInteraction> getInteractions() {
		return interactions;
	}
}
