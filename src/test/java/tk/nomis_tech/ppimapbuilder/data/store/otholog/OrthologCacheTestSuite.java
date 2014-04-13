package tk.nomis_tech.ppimapbuilder.data.store.otholog;

import org.junit.runner.RunWith;
import org.junit.runners.Suite;
import org.junit.runners.Suite.SuiteClasses;

@RunWith(Suite.class)
@SuiteClasses(value = {
		OrthologProteinIndexTest.class,
		OrganismPairOrthologCacheTest.class,
		OrthologCacheManagerTest.class,
})
public class OrthologCacheTestSuite {}
