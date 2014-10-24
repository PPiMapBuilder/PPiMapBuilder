package ch.picard.ppimapbuilder.util.test;

import org.cytoscape.model.CyNode;
import org.cytoscape.view.layout.CyLayoutAlgorithm;
import org.cytoscape.view.model.CyNetworkView;
import org.cytoscape.view.model.View;
import org.cytoscape.work.TaskIterator;

import java.util.Set;

public class DummyCyLayoutAlgorithm implements CyLayoutAlgorithm {
	@Override
	public TaskIterator createTaskIterator(CyNetworkView cyNetworkView, Object o, Set<View<CyNode>> views, String s) {
		return new TaskIterator();
	}

	@Override
	public boolean isReady(CyNetworkView cyNetworkView, Object o, Set<View<CyNode>> views, String s) {
		return false;
	}

	@Override
	public Object createLayoutContext() {
		return null;
	}

	@Override
	public Object getDefaultLayoutContext() {
		return null;
	}

	@Override
	public Set<Class<?>> getSupportedNodeAttributeTypes() {
		return null;
	}

	@Override
	public Set<Class<?>> getSupportedEdgeAttributeTypes() {
		return null;
	}

	@Override
	public boolean getSupportsSelectedOnly() {
		return false;
	}

	@Override
	public String getName() {
		return null;
	}
}
