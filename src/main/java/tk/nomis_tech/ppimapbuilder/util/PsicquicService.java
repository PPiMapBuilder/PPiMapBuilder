package tk.nomis_tech.ppimapbuilder.util;

/**
 *
 * @author Kévin Gravouil
 */
public class PsicquicService {

    private final String name;
    private final String url;

    public PsicquicService(String name, String url) {
        this.name = name;
        this.url = url;
    }

    public String getName() {
        return name;
    }

    public String getUrl() {
        return url;
    }

}
