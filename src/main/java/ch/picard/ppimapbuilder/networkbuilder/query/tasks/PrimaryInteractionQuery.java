package ch.picard.ppimapbuilder.networkbuilder.query.tasks;

import ch.picard.ppimapbuilder.data.client.ThreadedClientManager;
import ch.picard.ppimapbuilder.data.interaction.client.web.InteractionUtils;
import ch.picard.ppimapbuilder.data.interaction.client.web.ThreadedPsicquicClient;
import ch.picard.ppimapbuilder.data.organism.Organism;
import ch.picard.ppimapbuilder.data.protein.Protein;
import ch.picard.ppimapbuilder.data.protein.ProteinUtils;
import ch.picard.ppimapbuilder.data.protein.UniProtEntry;
import ch.picard.ppimapbuilder.data.protein.UniProtEntrySet;
import ch.picard.ppimapbuilder.data.protein.client.web.UniProtEntryClient;
import ch.picard.ppimapbuilder.data.protein.ortholog.OrthologScoredProtein;
import ch.picard.ppimapbuilder.data.protein.ortholog.client.ThreadedProteinOrthologClientDecorator;
import org.cytoscape.work.TaskMonitor;

import java.util.Arrays;
import java.util.Map;
import java.util.Set;
import java.util.concurrent.Callable;

class PrimaryInteractionQuery implements Callable<PrimaryInteractionQuery> {

	private final Organism refOrganism;
	private final Organism organism;
	private final UniProtEntrySet POIproteinPool;
	private final UniProtEntrySet proteinPool;

	private final ThreadedPsicquicClient psicquicClient;
	private final ThreadedProteinOrthologClientDecorator proteinOrthologClient;
	private final UniProtEntryClient uniProtClient;

	private final UniProtEntrySet newInteractors;
	private final Double MINIMUM_ORTHOLOGY_SCORE;

	private final ThreadedClientManager threadedClientManager;
	private final TaskMonitor taskMonitor;

	public PrimaryInteractionQuery(
			Organism refOrganism, Organism organism, UniProtEntrySet POIproteinPool, UniProtEntrySet proteinPool,
			ThreadedClientManager threadedClientManager, Double minimum_orthology_score
	) {
		this(
				refOrganism, organism, POIproteinPool, proteinPool,
				threadedClientManager, minimum_orthology_score,
				null
		);
	}

	public PrimaryInteractionQuery(
			Organism refOrganism, Organism organism, UniProtEntrySet POIproteinPool, UniProtEntrySet proteinPool,
			ThreadedClientManager threadedClientManager, Double minimum_orthology_score,
			TaskMonitor taskMonitor
	) {
		this.refOrganism = refOrganism;
		this.organism = organism;
		this.POIproteinPool = POIproteinPool;
		this.proteinPool = proteinPool;

		this.threadedClientManager = threadedClientManager;
		this.taskMonitor = taskMonitor;
		this.psicquicClient = threadedClientManager.getOrCreatePsicquicClient();
		this.proteinOrthologClient = threadedClientManager.getOrCreateProteinOrthologClient();
		this.uniProtClient = threadedClientManager.getOrCreateUniProtClient();

		MINIMUM_ORTHOLOGY_SCORE = minimum_orthology_score;

		this.newInteractors = new UniProtEntrySet();
	}

	public PrimaryInteractionQuery call() throws Exception {
		// Get list of protein of interest's orthologs in this organism
		Set<Protein> POIinOrg = POIproteinPool.getProteinInOrganismWithReferenceEntry(organism).keySet();

		//Get primary interactors via interaction search
		Set<Protein> interactors = ProteinUtils.newProteins(
				InteractionUtils.getInteractors(
						InteractionUtils.filter(
								psicquicClient.getProteinsInteractor(POIinOrg),
								new InteractionUtils.UniProtInteractionFilter()
						)
				),
				organism
		);
		threadedClientManager.unRegister(psicquicClient);

		//Remove POIs
		interactors.removeAll(POIinOrg);

		if (taskMonitor != null) taskMonitor.setProgress(0.66);

		//Search new interactors
		if (!interactors.isEmpty()) {

			if (organism.equals(refOrganism)) {
				for (UniProtEntry interactor :
						uniProtClient.retrieveProteinsData(ProteinUtils.asIdentifiers(interactors)).values()
						) {
					if (interactor.getOrganism().equals(organism))
						newInteractors.add(ProteinUtils.correctEntryOrganism(interactor, refOrganism));
				}
			} else {
				Map<Protein, Map<Organism, OrthologScoredProtein>> orthologs =
						proteinOrthologClient.getOrthologsMultiOrganismMultiProtein(
								interactors,
								Arrays.asList(refOrganism),
								MINIMUM_ORTHOLOGY_SCORE
						);

				for (Protein interactor : interactors) {
					final Map<Organism, OrthologScoredProtein> map = orthologs.get(interactor);
					if (map == null)
						continue;

					final Protein protInRefOrg = map.get(refOrganism);
					if (protInRefOrg == null)
						continue;

					UniProtEntry entry = proteinPool.find(protInRefOrg.getUniProtId());

					if (entry == null) {
						entry = uniProtClient.retrieveProteinData(protInRefOrg.getUniProtId());

						if (entry == null || entry.getOrganism().equals(organism))
							continue;

						newInteractors.add(ProteinUtils.correctEntryOrganism(entry, refOrganism));
					}

					entry.addOrtholog(interactor);
				}
			}
		}

		threadedClientManager.unRegister(proteinOrthologClient);
		threadedClientManager.unRegister(uniProtClient);

		if (taskMonitor != null) taskMonitor.setProgress(1);

		return this;
	}

	public Organism getOrganism() {
		return organism;
	}

	public UniProtEntrySet getNewInteractors() {
		return newInteractors;
	}
}
