package ch.picard.ppimapbuilder;

import ch.picard.ppimapbuilder.util.ClassLoaderHack;
import ppi_query.api.Api;
import ppi_query.api.ApiImpl;

import java.util.List;
import java.util.concurrent.Callable;

public class PPiQueryService implements Api {

    private final Api api;
    private final static PPiQueryService instance = new PPiQueryService();

    private PPiQueryService() {
        api = ClassLoaderHack.runWithClojure(new Callable<ApiImpl>() {
            @Override
            public ApiImpl call() throws Exception {
                return new ApiImpl();
            }
        });
    }

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

    @Override
    public List getInteractome(final List databaseNames, final Long organismId) {
        return ClassLoaderHack.runWithClojure(new Callable<List>() {
            @Override
            public List call() throws Exception {
                return api.getInteractome(databaseNames, organismId);
            }
        });
    }
}
