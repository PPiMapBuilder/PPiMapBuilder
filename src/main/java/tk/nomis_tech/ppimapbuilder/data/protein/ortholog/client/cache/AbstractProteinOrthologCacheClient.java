package tk.nomis_tech.ppimapbuilder.data.protein.ortholog.client.cache;

import tk.nomis_tech.ppimapbuilder.data.protein.ortholog.OrthologGroup;
import tk.nomis_tech.ppimapbuilder.data.protein.ortholog.client.AbstractProteinOrthologClient;

abstract class AbstractProteinOrthologCacheClient extends AbstractProteinOrthologClient {

	protected abstract void addOrthologGroup(OrthologGroup orthologGroup) throws Exception;

}
