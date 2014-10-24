package ch.picard.ppimapbuilder.util.test;

import org.cytoscape.model.CyNetwork;
import org.cytoscape.model.CyNetworkFactory;
import org.cytoscape.model.SavePolicy;

public class DummyCyNetworkFactory implements CyNetworkFactory {
	@Override
	public CyNetwork createNetwork() {
		return new DummyCyNetwork();
	}

	@Override
	public CyNetwork createNetwork(SavePolicy savePolicy) {
		return new DummyCyNetwork();
	}

	@Override
	public CyNetwork createNetworkWithPrivateTables() {
		return new DummyCyNetwork();
	}

	@Override
	public CyNetwork createNetworkWithPrivateTables(SavePolicy savePolicy) {
		return new DummyCyNetwork();
	}
}
