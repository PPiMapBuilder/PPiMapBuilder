package ch.picard.ppimapbuilder.util.test;

import org.cytoscape.model.CyNetwork;
import org.cytoscape.view.model.CyNetworkView;
import org.cytoscape.view.model.CyNetworkViewManager;

import java.util.ArrayList;
import java.util.Collection;
import java.util.HashSet;
import java.util.Set;

public class DummyCyNetworkViewManager implements CyNetworkViewManager {
	@Override
	public Set<CyNetworkView> getNetworkViewSet() {
		return new HashSet<CyNetworkView>();
	}

	@Override
	public Collection<CyNetworkView> getNetworkViews(CyNetwork cyNetwork) {
		return new ArrayList<CyNetworkView>();
	}

	@Override
	public boolean viewExists(CyNetwork cyNetwork) {
		return false;
	}

	@Override
	public void destroyNetworkView(CyNetworkView cyNetworkView) {

	}

	@Override
	public void addNetworkView(CyNetworkView cyNetworkView) {

	}

	@Override
	public void reset() {

	}
}
