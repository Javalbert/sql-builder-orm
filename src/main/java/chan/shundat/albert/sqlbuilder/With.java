package chan.shundat.albert.sqlbuilder;

import java.util.ArrayList;
import java.util.List;

import chan.shundat.albert.utils.string.Strings;

@SuppressWarnings("rawtypes")
public class With implements Node<With>, NodeHolder {
	protected List<Node> nodes = new ArrayList<>();

	@Override
	public List<Node> getNodes() { return nodes; }
	@Override
	public int getType() { return TYPE_WITH; }

	public With() {}
	
	public With(With with) {
		nodes = NodeUtils.mutableNodes(with);
	}
	
	@Override
	public boolean accept(NodeVisitor visitor) {
		return NodeUtils.visit(this, nodes, visitor);
	}
	
	@Override
	public With immutable() {
		With with = new ImmutableWith(this);
		return with;
	}
	
	@Override
	public With mutable() {
		With with = new With(this);
		return with;
	}
	
	/* BEGIN Fluent API */
	
	public With column(String column) {
		CommonTableExpression cte = getLast();
		
		List<String> columns = cte.getColumns();
		if (columns == null) {
			columns = new ArrayList<>();
			cte.setColumns(columns);
		}
		
		columns.add(Strings.safeTrim(column));
		return this;
	}

	public With as(Select select) {
		if (select == null) {
			throw new NullPointerException("select cannot be null");
		}
		getLast().setSelect(select);
		return this;
	}

	public With name(String name) {
		CommonTableExpression cte = new CommonTableExpression(name);
		nodes.add(cte);
		return this;
	}
	
	/* END Fluent API */
	
	private CommonTableExpression getLast() {
		CommonTableExpression cte = !nodes.isEmpty() ? (CommonTableExpression)nodes.get(nodes.size() - 1) : null;
		if (cte == null) {
			throw new IllegalStateException("No nodes");
		}
		return cte;
	}
}