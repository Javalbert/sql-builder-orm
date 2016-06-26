package chan.shundat.albert.sqlbuilder;

public class ImmutableColumnList extends ColumnList {
	public ImmutableColumnList(ColumnList columns) {
		nodes = NodeUtils.immutableNodes(columns);
	}
}