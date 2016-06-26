package chan.shundat.albert.sqlbuilder;

public class ImmutableWhere extends Where {
	public ImmutableWhere(Condition where) {
		nodes = NodeUtils.immutableNodes(where);
	}
}