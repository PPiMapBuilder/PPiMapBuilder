package ch.picard.ppimapbuilder.data.protein.ortholog;


import ch.picard.ppimapbuilder.data.protein.ortholog.client.ProteinOrthologWebCachedClientTest;
import ch.picard.ppimapbuilder.data.protein.ortholog.client.cache.PMBProteinOrthologCacheClientLoadedTest;
import ch.picard.ppimapbuilder.data.protein.ortholog.client.cache.PMBProteinOrthologCacheClientTest;
import ch.picard.ppimapbuilder.data.protein.ortholog.client.cache.SpeciesPairProteinOrthologCacheTest;
import ch.picard.ppimapbuilder.data.protein.ortholog.client.cache.loader.InParanoidCacheLoaderTaskFactoryTest;
import ch.picard.ppimapbuilder.data.protein.ortholog.client.web.InParanoidClientTest;
import org.junit.runner.RunWith;
import org.junit.runners.Suite;

@RunWith(Suite.class)
@Suite.SuiteClasses({OrthologGroupTest.class, ProteinOrthologWebCachedClientTest.class, InParanoidClientTest.class,
		PMBProteinOrthologCacheClientTest.class, PMBProteinOrthologCacheClientLoadedTest.class,
		SpeciesPairProteinOrthologCacheTest.class, InParanoidCacheLoaderTaskFactoryTest.class})
public class OrthologyTestSuite {
}

