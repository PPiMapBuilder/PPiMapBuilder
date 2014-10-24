package ch.picard.ppimapbuilder.data.organism;

import java.io.IOException;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

class OrganismRepository {

    protected final List<Organism> organisms;

    OrganismRepository(Organism... organisms) {
        this.organisms = Arrays.asList(organisms);
    }

    OrganismRepository(List<Organism> organisms) {
        this.organisms = new ArrayList<Organism>(organisms);
    }

    public Organism getOrganismBySimpleName(String simpleName) {
        for (Organism org : organisms)
            if (simpleName.startsWith(org.getSimpleScientificName()))
                return org;
        return null;
    }

    public List<Organism> getOrganisms() {
        return organisms;
    }

    public List<String> getOrganismNames() {
        ArrayList<String> names = new ArrayList<String>();
        for (Organism o : getOrganisms()) {
            names.add(o.getScientificName());
        }
        return names;

    }

    public Organism getOrganismByScientificName(String scientificName) {
        for (Organism o : organisms) {
            if (o.getScientificName().equals(scientificName)) {
                return o;
            }
        }
        return null;
    }

    public Organism getOrganismByTaxId(int taxId) {
        for (Organism org : organisms)
            if (org.getTaxId() == taxId)
                return org;
        return null;
    }

}
