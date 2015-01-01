package ch.picard.ppimapbuilder.util.test;

import org.cytoscape.model.CyNetwork;
import org.cytoscape.model.CyNetworkFactory;
import org.cytoscape.model.SavePolicy;

import java.util.ArrayList;
import java.util.List;

public class DummyCyNetworkFactory implements CyNetworkFactory {

	private final List<DummyCyNetwork> networks = new ArrayList<DummyCyNetwork>();

	@Override
	public CyNetwork createNetwork() {
		DummyCyNetwork network = new DummyCyNetwork();
		networks.add(network);
		return network;
	}

	@Override
	public CyNetwork createNetwork(SavePolicy savePolicy) {
		return createNetwork();
	}

	@Override
	public CyNetwork createNetworkWithPrivateTables() {
		return createNetwork();
	}

	@Override
	public CyNetwork createNetworkWithPrivateTables(SavePolicy savePolicy) {
		return createNetwork();
	}

	public List<DummyCyNetwork> getNetworks() {
		return networks;
	}
}
