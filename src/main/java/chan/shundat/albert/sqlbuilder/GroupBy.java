package chan.shundat.albert.sqlbuilder;

import java.util.ArrayList;
import java.util.List;

@SuppressWarnings("rawtypes")
public class GroupBy implements ColumnBuilder<GroupBy>, Node<GroupBy>, NodeHolder {
	protected List<Node> nodes = new ArrayList<>();
	private Column workColumn;
	
	@Override
	public List<Node> getNodes() { return nodes; }
	@Override
	public int getType() { return TYPE_GROUP_BY; }
	
	public GroupBy() {}
	
	public GroupBy(GroupBy groupBy) {
		nodes = NodeUtils.mutableNodes(groupBy);
	}
	
	@Override
	public boolean accept(NodeVisitor visitor) {
		return NodeUtils.visit(this, nodes, visitor);
	}
	
	@Override
	public GroupBy immutable() {
		GroupBy groupBy = new ImmutableGroupBy(this);
		return groupBy;
	}
	
	@Override
	public GroupBy mutable() {
		GroupBy groupBy = new GroupBy(this);
		return groupBy;
	}
	
	/* BEGIN Fluent API */
	
	@Override
	public GroupBy column(String name) {
		NodeUtils.addColumn(this, workColumn, name);
		workColumn = null;
		return this;
	}

	@Override
	public GroupBy tableAlias(String alias) {
		workColumn = Column.byAlias(alias);
		return this;
	}

	@Override
	public GroupBy tableName(String name) {
		workColumn = Column.byTableName(name);
		return this;
	}
	
	/* END Fluent API */
}