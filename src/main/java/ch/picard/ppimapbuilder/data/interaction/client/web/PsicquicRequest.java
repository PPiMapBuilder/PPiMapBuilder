package ch.picard.ppimapbuilder.data.interaction.client.web;

import ch.picard.ppimapbuilder.util.concurrent.IteratorRequest;
import ch.picard.ppimapbuilder.util.iterators.EmptyIterator;
import org.hupo.psi.mi.psicquic.wsclient.PsicquicSimpleClient;
import psidev.psi.mi.tab.PsimiTabReader;
import psidev.psi.mi.tab.model.BinaryInteraction;

import java.io.IOException;
import java.util.Iterator;

public class PsicquicRequest implements IteratorRequest<BinaryInteraction> {
	private final PsicquicSimpleClient client;
	private final String query;
	private final int firstResult;
	private final int maxResults;
	private	final PsimiTabReader psimiTabReader;
	private final static int MAX_TRY = 3;

	protected PsicquicRequest(PsicquicSimpleClient client, String query, int firstResult, int maxResults) {
		this.client = client;
		this.query = query;
		this.firstResult = firstResult;
		this.maxResults = maxResults;
		this.psimiTabReader = new PsimiTabReader();
	}

	@Override
	public Iterator<BinaryInteraction> call() throws Exception {
		int ntry = 0;
		while (++ntry <= MAX_TRY) {
			try {
				return psimiTabReader.iterate(
						client.getByQuery(
								query,
								PsicquicSimpleClient.MITAB25,
								firstResult,
								maxResults
						)
				);
			} catch (IOException ignored) {}
		}
		return new EmptyIterator<BinaryInteraction>();
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
