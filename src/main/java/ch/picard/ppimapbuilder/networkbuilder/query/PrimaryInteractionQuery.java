package ch.picard.ppimapbuilder.networkbuilder.query;

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

import java.util.*;
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

	private final PMBQueryInteractionTask.ThreadedClientManager threadedClientManager;

	PrimaryInteractionQuery(
			Organism refOrganism, Organism organism, UniProtEntrySet POIproteinPool, UniProtEntrySet proteinPool,
			PMBQueryInteractionTask.ThreadedClientManager threadedClientManager, Double minimum_orthology_score
	) {

		this.refOrganism = refOrganism;
		this.organism = organism;
		this.POIproteinPool = POIproteinPool;
		this.proteinPool = proteinPool;

		this.threadedClientManager = threadedClientManager;
		this.psicquicClient = threadedClientManager.registerPsicquicClient();
		this.proteinOrthologClient =threadedClientManager.registerProteinOrthologClient();
		this.uniProtClient = threadedClientManager.registerUniProtClient();

		MINIMUM_ORTHOLOGY_SCORE = minimum_orthology_score;

		this.newInteractors = new UniProtEntrySet();
	}

	public PrimaryInteractionQuery call() throws Exception {
		// Get list of protein of interest's orthologs in this organism
		Set<Protein> POIinOrg = POIproteinPool.getInOrg(organism).keySet();

		// Generate MiQL query
		final List<String> additionnalQueries = new ArrayList<String>();
		for (final Protein protein : POIinOrg) {
			additionnalQueries.add(
					InteractionUtils.generateMiQLQueryIDTaxID(
							protein.getUniProtId(),
							organism.getTaxId()
					)
			);
		}

		//Get primary interactors via interaction search
		Set<Protein> interactors = ProteinUtils.newProteins(
			InteractionUtils.getInteractors(
					InteractionUtils.filter(
							psicquicClient.getByQueries(additionnalQueries),
							new InteractionUtils.UniProtInteractionFilter()
					)
			),
			organism
		);
		threadedClientManager.unRegister(psicquicClient);

		//Remove POIs
		interactors.removeAll(POIinOrg);

		//Search new interactors
		if (!interactors.isEmpty()) {

			if (organism.equals(refOrganism)) {
				for (UniProtEntry interactor :
						uniProtClient.retrieveProteinsData(ProteinUtils.asIdentifiers(interactors)).values()
				) {
					if(
							!interactor.getOrganism().equals(refOrganism) &&
							interactor.getOrganism().sameSpecies(refOrganism)
					) {
						interactor = new UniProtEntry.Builder(interactor)
								.setOrganism(refOrganism)
								.build();
					}
					newInteractors.add(interactor);
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

					if (!proteinPool.contains(protInRefOrg)) {
						UniProtEntry proteinEntry = uniProtClient.retrieveProteinData(protInRefOrg.getUniProtId());

						if(proteinEntry != null) {
							if(
									!proteinEntry.getOrganism().equals(refOrganism) &&
											proteinEntry.getOrganism().sameSpecies(refOrganism)
									) {
								proteinEntry = new UniProtEntry.Builder(proteinEntry)
										.setOrganism(refOrganism)
										.build();
							}

							proteinEntry.addOrtholog(interactor);

							newInteractors.add(proteinEntry);
						}
					}
				}
			}
		}

		threadedClientManager.unRegister(proteinOrthologClient);
		threadedClientManager.unRegister(uniProtClient);

		return this;
	}

	public Organism getOrganism() {
		return organism;
	}

	public UniProtEntrySet getNewInteractors() {
		return newInteractors;
	}
}
