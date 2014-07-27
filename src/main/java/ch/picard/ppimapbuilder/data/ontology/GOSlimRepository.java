package ch.picard.ppimapbuilder.data.ontology;

import ch.picard.ppimapbuilder.data.settings.PMBSettings;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class GOSlimRepository {

	private static GOSlimRepository _instance;

	public static GOSlimRepository getInstance() {
		if(_instance == null)
			_instance = new GOSlimRepository();
		return _instance;
	}

	private final Map<String, GeneOntologySet> goSlims;

	private GOSlimRepository() {
		this.goSlims = new HashMap<String, GeneOntologySet>();
		for(GeneOntologySet geneOntologySet : PMBSettings.getInstance().getGoSlimList())
			this.goSlims.put(geneOntologySet.getName(), geneOntologySet);
	}

	public boolean addGOSlim(GeneOntologySet geneOntologySet) {
		if(goSlims.containsKey(geneOntologySet.getName()))
			return false;
		goSlims.put(geneOntologySet.getName(), geneOntologySet);
		return true;
	}

	public GeneOntologySet getGOSlim(String name) {
		return goSlims.get(name);
	}

	public List<String> getGOSlimNames() {
		return new ArrayList<String>(goSlims.keySet());
	}

	public void resetToSettings() {
		_instance = new GOSlimRepository();
	}
}
