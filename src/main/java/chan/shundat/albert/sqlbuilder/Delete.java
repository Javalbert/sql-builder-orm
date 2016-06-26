package chan.shundat.albert.sqlbuilder;

import java.util.ArrayList;
import java.util.List;

@SuppressWarnings("rawtypes")
public class Delete implements DMLStatement<Delete>, NodeHolder, TableNameSpecifier<Delete> {
	protected List<Node> nodes = new ArrayList<>();

	@Override
	public List<Node> getNodes() { return nodes; }
	@Override
	public int getType() { return TYPE_DELETE; }

	public Delete() {}
	
	public Delete(Delete delete) {
		nodes = NodeUtils.mutableNodes(delete);
	}
	
	@Override
	public boolean accept(NodeVisitor visitor) {
		return NodeUtils.visit(this, nodes, visitor);
	}

	@Override
	public Delete immutable() {
		Delete delete = new ImmutableDelete(this);
		return delete;
	}

	@Override
	public Delete mutable() {
		Delete delete = new Delete(this);
		return delete;
	}
	
	/* BEGIN Fluent API */
	
	// DELETE FROM syntax not ANSI standard
//	public Delete from(From from) {
//		if (from == null) {
//			throw new NullPointerException("from cannot be null");
//		}
//		nodes.add(from);
//		return this;
//	}

	@Override
	public Delete tableName(String name) {
		Table table = new Table(name);
		nodes.add(table);
		return this;
	}
	
	public Delete where(Where where) {
		if (where == null) {
			throw new NullPointerException("where cannot be null");
		}
		nodes.add(where);
		return this;
	}
	
	public Delete with(With with) {
		if (with == null) {
			throw new NullPointerException("with cannot be null");
		}
		nodes.add(with);
		return this;
	}
	
	/* END Fluent API */
}