package chan.shundat.albert.sqlbuilder;

public class ImmutableOrderBy extends OrderBy {
	public ImmutableOrderBy(OrderBy orderBy) {
		nodes = NodeUtils.immutableNodes(orderBy);
	}
}