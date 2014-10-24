package ch.picard.ppimapbuilder.util.test;

import org.cytoscape.model.CyNetwork;
import org.cytoscape.view.model.CyNetworkView;
import org.cytoscape.view.model.CyNetworkViewFactory;

public class DummyCyNetworkViewFactory implements CyNetworkViewFactory {
	@Override
	public CyNetworkView createNetworkView(CyNetwork cyNetwork) {
		return new DummyCyNetworkView();
	}
}
