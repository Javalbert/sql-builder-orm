package chan.shundat.albert.sqlbuilder;

import java.util.ArrayList;
import java.util.List;

@SuppressWarnings("rawtypes")
public class Select implements Aliasable, DMLStatement<Select>, NodeHolder {
	protected String alias;
	protected List<Node> nodes = new ArrayList<>();

	@Override
	public String getAlias() { return alias; }
	@Override
	public void setAlias(String alias) { this.alias = alias; }
	@Override
	public List<Node> getNodes() { return nodes; }
	@Override
	public int getType() { return TYPE_SELECT; }

	public Select() {}
	
	public Select(Select select) {
		alias = select.getAlias();
		nodes = NodeUtils.mutableNodes(select);
	}
	
	@Override
	public boolean accept(NodeVisitor visitor) {
		return NodeUtils.visit(this, nodes, visitor);
	}
	
	@Override
	public Select immutable() {
		Select select = new ImmutableSelect(this);
		return select;
	}
	
	@Override
	public Select mutable() {
		Select select = new Select(this);
		return select;
	}
	
	/* BEGIN Fluent API */
	
	public Select except() {
		return except(null);
	}
	
	public Select except(Select select) {
		nodes.add(SetOperator.except(select));
		return this;
	}
	
	public Select from(From from) {
		if (from == null) {
			throw new NullPointerException("from cannot be null");
		}
		nodes.add(from);
		return this;
	}
	
	public Select groupBy(GroupBy groupBy) {
		if (groupBy == null) {
			throw new NullPointerException("groupBy cannot be null");
		}
		nodes.add(groupBy);
		return this;
	}
	
	public Select having(Having having) {
		if (having == null) {
			throw new NullPointerException("having cannot be null");
		}
		nodes.add(having);
		return this;
	}
	
	public Select intersect() {
		return intersect(null);
	}
	
	public Select intersect(Select select) {
		nodes.add(SetOperator.intersect(select));
		return this;
	}
	
	public Select list(SelectList list) {
		if (list == null) {
			throw new NullPointerException("list cannot be null");
		}
		nodes.add(list);
		return this;
	}
	
	public Select orderBy(OrderBy orderBy) {
		if (orderBy == null) {
			throw new NullPointerException("orderBy cannot be null");
		}
		nodes.add(orderBy);
		return this;
	}
	
	public Select query(Select select) {
		if (select == null) {
			throw new NullPointerException("select cannot be null");
		}
		nodes.add(select);
		return this;
	}
	
	public Select union() {
		return union(null);
	}
	
	public Select union(Select select) {
		nodes.add(SetOperator.union(select));
		return this;
	}
	
	public Select unionAll() {
		return unionAll(null);
	}
	
	public Select unionAll(Select select) {
		nodes.add(SetOperator.unionAll(select));
		return this;
	}
	
	public Select where(Where where) {
		if (where == null) {
			throw new NullPointerException("where cannot be null");
		}
		nodes.add(where);
		return this;
	}
	
	public Select with(With with) {
		if (with == null) {
			throw new NullPointerException("with cannot be null");
		}
		nodes.add(with);
		return this;
	}
	
	/* END Fluent API */
}