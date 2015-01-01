package ch.picard.ppimapbuilder.util.test;

import org.cytoscape.model.CyNetwork;
import org.cytoscape.session.CyNetworkNaming;

public class DummyCyNetworkNaming implements CyNetworkNaming {
	@Override
	public String getSuggestedSubnetworkTitle(CyNetwork cyNetwork) {
		return null;
	}

	@Override
	public String getSuggestedNetworkTitle(String s) {
		return s;
	}
}
