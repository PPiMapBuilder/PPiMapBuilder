package ch.picard.ppimapbuilder.util.test;

import org.cytoscape.model.*;

import java.util.ArrayList;
import java.util.Collection;
import java.util.List;

public class DummyCyNetwork implements CyNetwork {

	private final List<CyEdge> edges = new ArrayList<CyEdge>();
	private final List<CyNode> nodes = new ArrayList<CyNode>();

	@Override
	public CyNode addNode() {
		DummyCyNode node = new DummyCyNode(this);
		nodes.add(node);
		return node;
	}

	@Override
	public boolean removeNodes(Collection<CyNode> cyNodes) {
		return false;
	}

	@Override
	public CyEdge addEdge(CyNode cyNode, CyNode cyNode2, boolean b) {
		DummyCyEdge edge = new DummyCyEdge(cyNode, cyNode2);
		edges.add(edge);
		return edge;
	}

	@Override
	public boolean removeEdges(Collection<CyEdge> cyEdges) {
		return edges.removeAll(cyEdges);
	}

	@Override
	public int getNodeCount() {
		return nodes.size();
	}

	@Override
	public int getEdgeCount() {
		return edges.size();
	}

	@Override
	public List<CyNode> getNodeList() {
		return nodes;
	}

	@Override
	public List<CyEdge> getEdgeList() {
		return edges;
	}

	@Override
	public boolean containsNode(CyNode cyNode) {
		return nodes.contains(cyNode);
	}

	@Override
	public boolean containsEdge(CyEdge cyEdge) {
		return edges.contains(cyEdge);
	}

	@Override
	public boolean containsEdge(CyNode cyNode, CyNode cyNode2) {
		return false;
	}

	@Override
	public CyNode getNode(long l) {
		return null;
	}

	@Override
	public CyEdge getEdge(long l) {
		return null;
	}

	@Override
	public List<CyNode> getNeighborList(CyNode cyNode, CyEdge.Type type) {
		return null;
	}

	@Override
	public List<CyEdge> getAdjacentEdgeList(CyNode cyNode, CyEdge.Type type) {
		return null;
	}

	@Override
	public Iterable<CyEdge> getAdjacentEdgeIterable(CyNode cyNode, CyEdge.Type type) {
		return null;
	}

	@Override
	public List<CyEdge> getConnectingEdgeList(CyNode cyNode, CyNode cyNode2, CyEdge.Type type) {
		return null;
	}

	@Override
	public CyTable getDefaultNetworkTable() {
		return new DummyCyTable();
	}

	@Override
	public CyTable getDefaultNodeTable() {
		return new DummyCyTable();
	}

	@Override
	public CyTable getDefaultEdgeTable() {
		return new DummyCyTable();
	}

	@Override
	public CyTable getTable(Class<? extends CyIdentifiable> aClass, String s) {
		return new DummyCyTable();
	}

	@Override
	public CyRow getRow(CyIdentifiable cyIdentifiable, String s) {
		return new DummyCyRow();
	}

	@Override
	public CyRow getRow(final CyIdentifiable cyIdentifiable) {
		if(cyIdentifiable instanceof DummyCyNode) {
			return new DummyCyRow(){
				@Override
				public <T> void set(String s, T t) {
					if(s.equals("name"))
						((DummyCyNode) cyIdentifiable).setName(t.toString());
				}
			};
		}
		return new DummyCyRow();
	}

	@Override
	public SavePolicy getSavePolicy() {
		return null;
	}

	@Override
	public void dispose() {

	}

	@Override
	public Long getSUID() {
		return null;
	}

	public List<CyNode> getNodes() {
		return nodes;
	}

}
