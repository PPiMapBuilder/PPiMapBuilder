package ch.picard.ppimapbuilder.data.interaction.client.web.miql;

import java.util.List;

/**
 * MiQL Expression Builder used to generate boolean MiQL expression.<br/>
 * Example result: "(123 AND test OR "yop yop")"
 */
public class MiQLExpressionBuilder extends AbstractMiQLQueryElement {

	private boolean root = false;

	public enum Operator {
		AND, OR
	}

	public MiQLExpressionBuilder() {
		separator = " ";
	}

	public MiQLExpressionBuilder(MiQLExpressionBuilder exp) {
		this();
		exprs.addAll(exp.exprs);
		setRoot(exp.root);
	}

	public void add(Operator op, String element) {
		if (size() > 0 && op != null)
			add(op.toString());
		add(element);
	}

	public void add(Operator op, AbstractMiQLQueryElement condition) {
		if (size() > 0 && op != null)
			add(op.toString());
		add(condition);
	}

	public void addAll(Operator op, List<String> conditions) {
		for (String elem : conditions) {
			add(op, elem);
		}
	}

	public void setRoot(boolean root) {
		this.root = root;
	}

	@Override
	public String toString() {
		StringBuilder out = new StringBuilder();

		if (size() > 1 && !root)
			out.append("(");
		out.append(super.toString());
		if (size() > 1 && !root)
			out.append(")");

		return out.toString();
	}
}