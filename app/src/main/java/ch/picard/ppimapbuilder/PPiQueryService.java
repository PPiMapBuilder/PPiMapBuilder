package ch.picard.ppimapbuilder;

import ch.picard.ppimapbuilder.util.ClassLoaderHack;
import ppi_query.api.Api;
import ppi_query.api.ApiImpl;

import java.util.List;
import java.util.concurrent.Callable;

public class PPiQueryService implements Api {

    private final Api api = new ApiImpl();
    private final static PPiQueryService instance = new PPiQueryService();

    private PPiQueryService() {}

    public static PPiQueryService getInstance() {
        return instance;
    }

    @Override
    public List getPsicquicServices() {
        return ClassLoaderHack.runWithClojure(new Callable<List>() {
            @Override
            public List call() throws Exception {
                return api.getPsicquicServices();
            }
        });
    }

    @Override
    public List getOrganisms() {
        return ClassLoaderHack.runWithClojure(new Callable<List>() {
            @Override
            public List call() throws Exception {
                return api.getOrganisms();
            }
        });
    }
}
