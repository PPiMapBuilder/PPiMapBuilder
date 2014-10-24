package ch.picard.ppimapbuilder.networkbuilder.query.tasks;

import ch.picard.ppimapbuilder.data.client.ThreadedClientManager;
import ch.picard.ppimapbuilder.data.interaction.client.web.InteractionUtils;
import ch.picard.ppimapbuilder.data.interaction.client.web.ThreadedPsicquicClient;
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

	private final ThreadedClientManager threadedClientManager;

	private final List<EncoreInteraction> interactions;

	private final ThreadedPsicquicClient psicquicClient;

	public SecondaryInteractionQuery(
			Organism organism, UniProtEntrySet proteinPool,
			ThreadedClientManager threadedClientManager
	) {
		this.organism = organism;
		this.proteinPool = proteinPool;

		this.threadedClientManager = threadedClientManager;

		this.interactions = new ArrayList<EncoreInteraction>();
		this.psicquicClient = threadedClientManager.getOrCreatePsicquicClient();
	}

	public SecondaryInteractionQuery call() throws Exception {
		//Get proteins in the current organism
		final Set<Protein> proteins = proteinPool.getProteinInOrganismWithReferenceEntry(organism).keySet();

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

		threadedClientManager.unRegister(psicquicClient);
		return this;
	}

	public Organism getOrganism() {
		return organism;
	}

	public List<EncoreInteraction> getInteractions() {
		return interactions;
	}
}
