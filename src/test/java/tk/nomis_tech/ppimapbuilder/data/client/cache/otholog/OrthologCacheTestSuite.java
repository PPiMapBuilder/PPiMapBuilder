package tk.nomis_tech.ppimapbuilder.data.client.cache.otholog;

import org.junit.runner.RunWith;
import org.junit.runners.Suite;
import org.junit.runners.Suite.SuiteClasses;

@RunWith(Suite.class)
@SuiteClasses(value = {
		ProteinOrthologIndexTest.class,
		SpeciesPairProteinOrthologCacheTest.class,
		ProteinOrthologCacheClientTest.class,
})
public class OrthologCacheTestSuite {}
