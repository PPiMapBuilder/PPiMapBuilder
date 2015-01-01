package ch.picard.ppimapbuilder.util.test;

import org.cytoscape.model.CyNetwork;
import org.cytoscape.model.CyNetworkManager;

import java.util.Set;

public class DummyCyNetworkManager implements CyNetworkManager {
	@Override
	public Set<CyNetwork> getNetworkSet() {
		return null;
	}

	@Override
	public CyNetwork getNetwork(long l) {
		return null;
	}

	@Override
	public boolean networkExists(long l) {
		return false;
	}

	@Override
	public void destroyNetwork(CyNetwork cyNetwork) {

	}

	@Override
	public void addNetwork(CyNetwork cyNetwork) {

	}

	@Override
	public void reset() {

	}
}
