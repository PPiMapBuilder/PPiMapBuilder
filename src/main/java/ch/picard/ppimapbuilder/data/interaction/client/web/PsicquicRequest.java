package ch.picard.ppimapbuilder.data.interaction.client.web;

import ch.picard.ppimapbuilder.util.concurrent.ListRequest;
import org.hupo.psi.mi.psicquic.wsclient.PsicquicSimpleClient;
import psidev.psi.mi.tab.PsimiTabReader;
import psidev.psi.mi.tab.model.BinaryInteraction;

import java.util.List;

public class PsicquicRequest implements ListRequest<BinaryInteraction> {
	private final PsicquicSimpleClient client;
	private final String query;
	private final int firstResult;
	private final int maxResults;

	protected PsicquicRequest(PsicquicSimpleClient client, String query, int firstResult, int maxResults) {
		this.client = client;
		this.query = query;
		this.firstResult = firstResult;
		this.maxResults = maxResults;
	}

	@Override
	public List<BinaryInteraction> call() throws Exception {
		return (List<BinaryInteraction>) new PsimiTabReader().read(
				client.getByQuery(
						query,
						PsicquicSimpleClient.MITAB25,
						firstResult,
						maxResults
				)
		);
	}

	public int getFirstResult() {
		return firstResult;
	}

	public int getMaxResults() {
		return maxResults;
	}

	public String getQuery() {
		return query;
	}
}
