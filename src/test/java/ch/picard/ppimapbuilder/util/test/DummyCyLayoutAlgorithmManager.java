package ch.picard.ppimapbuilder.util.test;

import org.cytoscape.view.layout.CyLayoutAlgorithm;
import org.cytoscape.view.layout.CyLayoutAlgorithmManager;

import java.util.Collection;

public class DummyCyLayoutAlgorithmManager implements CyLayoutAlgorithmManager {
	@Override
	public CyLayoutAlgorithm getLayout(String s) {
		return new DummyCyLayoutAlgorithm();
	}

	@Override
	public Collection<CyLayoutAlgorithm> getAllLayouts() {
		return null;
	}

	@Override
	public CyLayoutAlgorithm getDefaultLayout() {
		return null;
	}
}
