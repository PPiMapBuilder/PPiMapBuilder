package ch.picard.ppimapbuilder.data.protein.ortholog.client.cache;

import org.junit.runner.RunWith;
import org.junit.runners.Suite;
import org.junit.runners.Suite.SuiteClasses;
import ch.picard.ppimapbuilder.data.protein.ortholog.OrthologGroupTest;

@RunWith(Suite.class)
@SuiteClasses(value = {
		OrthologGroupTest.class,
		SpeciesPairProteinOrthologCacheTest.class,
		PMBProteinOrthologCacheClientTest.class
})
public class OrthologCacheTestSuite {}
