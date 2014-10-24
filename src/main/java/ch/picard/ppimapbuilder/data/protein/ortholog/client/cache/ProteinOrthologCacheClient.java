package ch.picard.ppimapbuilder.data.protein.ortholog.client.cache;

import ch.picard.ppimapbuilder.data.protein.ortholog.OrthologGroup;
import ch.picard.ppimapbuilder.data.protein.ortholog.client.AbstractProteinOrthologClient;
import ch.picard.ppimapbuilder.data.protein.ortholog.client.ProteinOrthologClient;

public interface ProteinOrthologCacheClient extends ProteinOrthologClient {

	public void addOrthologGroup(OrthologGroup orthologGroup) throws Exception;

}
