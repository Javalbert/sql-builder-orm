package chan.shundat.albert.sqlbuilder;

public class ImmutableGroupBy extends GroupBy {
	public ImmutableGroupBy(GroupBy groupBy) {
		nodes = NodeUtils.immutableNodes(groupBy);
	}
}