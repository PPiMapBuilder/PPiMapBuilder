package ch.picard.ppimapbuilder.util.test;

import org.cytoscape.model.CyEdge;
import org.cytoscape.model.CyNode;
import sun.reflect.generics.reflectiveObjects.NotImplementedException;

public class DummyCyEdge implements CyEdge {
	private final CyNode target;
	private final CyNode source;

	public DummyCyEdge(CyNode target, CyNode source) {
		this.target = target;
		this.source = source;
	}

	@Override
	public CyNode getSource() {
		return source;
	}

	@Override
	public CyNode getTarget() {
		return target;
	}

	@Override
	public boolean isDirected() {
		return false;
	}

	@Override
	public Long getSUID() {
		return null;
	}

}
