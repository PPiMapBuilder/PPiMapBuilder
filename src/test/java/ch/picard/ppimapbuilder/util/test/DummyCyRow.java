package ch.picard.ppimapbuilder.util.test;

import org.cytoscape.model.CyRow;
import org.cytoscape.model.CyTable;

import java.util.List;
import java.util.Map;

public class DummyCyRow implements CyRow {
	@Override
	public <T> T get(String s, Class<? extends T> aClass) {
		return null;
	}

	@Override
	public <T> T get(String s, Class<? extends T> aClass, T t) {
		return null;
	}

	@Override
	public <T> List<T> getList(String s, Class<T> tClass) {
		return null;
	}

	@Override
	public <T> List<T> getList(String s, Class<T> tClass, List<T> ts) {
		return null;
	}

	@Override
	public <T> void set(String s, T t) {

	}

	@Override
	public boolean isSet(String s) {
		return false;
	}

	@Override
	public Map<String, Object> getAllValues() {
		return null;
	}

	@Override
	public Object getRaw(String s) {
		return null;
	}

	@Override
	public CyTable getTable() {
		return null;
	}
}
