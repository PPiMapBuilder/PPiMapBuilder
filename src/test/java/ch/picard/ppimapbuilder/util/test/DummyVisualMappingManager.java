package ch.picard.ppimapbuilder.util.test;

import org.cytoscape.view.model.CyNetworkView;
import org.cytoscape.view.model.VisualLexicon;
import org.cytoscape.view.vizmap.VisualMappingManager;
import org.cytoscape.view.vizmap.VisualStyle;

import java.util.HashSet;
import java.util.Set;

public class DummyVisualMappingManager implements VisualMappingManager {
	@Override
	public void setVisualStyle(VisualStyle visualStyle, CyNetworkView cyNetworkView) {

	}

	@Override
	public VisualStyle getVisualStyle(CyNetworkView cyNetworkView) {
		return null;
	}

	@Override
	public Set<VisualStyle> getAllVisualStyles() {
		return new HashSet<VisualStyle>();
	}

	@Override
	public void addVisualStyle(VisualStyle visualStyle) {

	}

	@Override
	public void removeVisualStyle(VisualStyle visualStyle) {

	}

	@Override
	public VisualStyle getDefaultVisualStyle() {
		return null;
	}

	@Override
	public void setCurrentVisualStyle(VisualStyle visualStyle) {

	}

	@Override
	public VisualStyle getCurrentVisualStyle() {
		return null;
	}

	@Override
	public Set<VisualLexicon> getAllVisualLexicon() {
		return null;
	}
}
