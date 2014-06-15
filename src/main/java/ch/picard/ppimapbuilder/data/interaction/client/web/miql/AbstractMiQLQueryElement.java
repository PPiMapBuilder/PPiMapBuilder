package ch.picard.ppimapbuilder.data.interaction.client.web.miql;

import java.util.ArrayList;
import java.util.List;

public class AbstractMiQLQueryElement {
	protected final List<Object> exprs = new ArrayList<Object>();
	private String cache = null;
	protected String separator = "";
	
	public AbstractMiQLQueryElement() {}
	
	public AbstractMiQLQueryElement(String elem) {
		add(elem);
	}
	
	public AbstractMiQLQueryElement(AbstractMiQLQueryElement elem) {
		add(elem);
	}

	public void add(AbstractMiQLQueryElement elem) {
		exprs.add(elem);
		cache = null;
	}
	
	public void add(String elem) {
		if(elem.contains(" "))
			exprs.add(new StringBuilder("\"").append(elem).append("\""));
		else exprs.add(elem);
		cache = null;
	}

	public void addAll(List<String> elements) {
		for (String elem : elements) {
			add(elem);
		}
	}
	
	public void addAllElement(List<AbstractMiQLQueryElement> elements) {
		exprs.addAll(elements);
	}
	
	public int size() {
		return exprs.size();
	}
	
	@Override
	public String toString() {
		if(cache == null) {
			StringBuilder out = new StringBuilder();
			
			boolean first = true;
			for(Object expr: exprs) {
				if(!first) out.append(separator);
				out.append(expr.toString());
				first = false;
			}
			
			cache = out.toString();
		}
		return cache;
	}
}
