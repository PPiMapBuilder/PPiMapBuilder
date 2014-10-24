package ch.picard.ppimapbuilder.util.test;

import org.cytoscape.model.CyColumn;
import org.cytoscape.model.CyRow;
import org.cytoscape.model.CyTable;
import org.cytoscape.model.SavePolicy;

import java.util.ArrayList;
import java.util.Collection;
import java.util.List;

public class DummyCyTable implements CyTable {
	@Override
	public boolean isPublic() {
		return false;
	}

	@Override
	public void setPublic(boolean b) {

	}

	@Override
	public Mutability getMutability() {
		return null;
	}

	@Override
	public String getTitle() {
		return null;
	}

	@Override
	public void setTitle(String s) {

	}

	@Override
	public CyColumn getPrimaryKey() {
		return null;
	}

	@Override
	public CyColumn getColumn(String s) {
		return null;
	}

	@Override
	public Collection<CyColumn> getColumns() {
		return null;
	}

	@Override
	public void deleteColumn(String s) {

	}

	@Override
	public <T> void createColumn(String s, Class<? extends T> aClass, boolean b) {

	}

	@Override
	public <T> void createColumn(String s, Class<? extends T> aClass, boolean b, T t) {

	}

	@Override
	public <T> void createListColumn(String s, Class<T> tClass, boolean b) {

	}

	@Override
	public <T> void createListColumn(String s, Class<T> tClass, boolean b, List<T> ts) {

	}

	@Override
	public CyRow getRow(Object o) {
		return null;
	}

	@Override
	public boolean rowExists(Object o) {
		return false;
	}

	@Override
	public boolean deleteRows(Collection<?> objects) {
		return false;
	}

	@Override
	public List<CyRow> getAllRows() {
		return new ArrayList<CyRow>();
	}

	@Override
	public String getLastInternalError() {
		return null;
	}

	@Override
	public Collection<CyRow> getMatchingRows(String s, Object o) {
		return null;
	}

	@Override
	public int countMatchingRows(String s, Object o) {
		return 0;
	}

	@Override
	public int getRowCount() {
		return 0;
	}

	@Override
	public String addVirtualColumn(String s, String s2, CyTable cyTable, String s3, boolean b) {
		return null;
	}

	@Override
	public void addVirtualColumns(CyTable cyTable, String s, boolean b) {

	}

	@Override
	public SavePolicy getSavePolicy() {
		return null;
	}

	@Override
	public void setSavePolicy(SavePolicy savePolicy) {

	}

	@Override
	public void swap(CyTable cyTable) {

	}

	@Override
	public Long getSUID() {
		return null;
	}
}
