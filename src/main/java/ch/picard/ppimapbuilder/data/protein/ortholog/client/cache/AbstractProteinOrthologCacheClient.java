package ch.picard.ppimapbuilder.data.protein.ortholog.client.cache;

import ch.picard.ppimapbuilder.data.protein.ortholog.OrthologGroup;
import ch.picard.ppimapbuilder.data.protein.ortholog.client.AbstractProteinOrthologClient;

abstract class AbstractProteinOrthologCacheClient extends AbstractProteinOrthologClient {

	protected abstract void addOrthologGroup(OrthologGroup orthologGroup) throws Exception;

}
