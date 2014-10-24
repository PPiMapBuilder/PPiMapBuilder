package ch.picard.ppimapbuilder.util.test;

import org.cytoscape.model.CyNetwork;
import org.cytoscape.model.CyNode;
import sun.reflect.generics.reflectiveObjects.NotImplementedException;

public class DummyCyNode implements CyNode {
	private CyNetwork cyNetwork;
	private String name;

	DummyCyNode(CyNetwork cyNetwork) {
		this.cyNetwork = cyNetwork;
	}

	void setName(String name) {
		this.name = name;
	}

	@Override
	public CyNetwork getNetworkPointer() {
		return cyNetwork;
	}

	@Override
	public void setNetworkPointer(CyNetwork cyNetwork) {
		this.cyNetwork = cyNetwork;
	}

	@Override
	public Long getSUID() {
		return null;
	}
}
