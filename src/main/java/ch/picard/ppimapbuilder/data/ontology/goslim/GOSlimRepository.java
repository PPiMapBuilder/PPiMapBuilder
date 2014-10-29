package ch.picard.ppimapbuilder.data.ontology.goslim;

import ch.picard.ppimapbuilder.data.ontology.GeneOntologyTermSet;
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

	private final Map<String, GOSlim> goSlims;

	private GOSlimRepository() {
		this.goSlims = new HashMap<String, GOSlim>();
		for(GOSlim geneOntologyTermSet : PMBSettings.getInstance().getGoSlimList())
			this.goSlims.put(geneOntologyTermSet.getName(), geneOntologyTermSet);
	}

	public boolean addGOSlim(GOSlim goSlim) {
		if(goSlims.containsKey(goSlim.getName()))
			return false;
		goSlims.put(goSlim.getName(), goSlim);
		return true;
	}

	public GeneOntologyTermSet getGOSlim(String name) {
		return goSlims.get(name);
	}

	public List<String> getGOSlimNames() {
		return new ArrayList<String>(goSlims.keySet());
	}

	public static void resetToSettings() {
		_instance = new GOSlimRepository();
	}

	public List<GOSlim> getGOSlims() {
		return new ArrayList<GOSlim>(goSlims.values());
	}

	public void remove(String name) {
		goSlims.remove(name);
	}
}
