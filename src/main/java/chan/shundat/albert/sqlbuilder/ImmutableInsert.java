package chan.shundat.albert.sqlbuilder;

public class ImmutableInsert extends Insert {
	public ImmutableInsert(Insert insert) {
		nodes = NodeUtils.immutableNodes(insert);
	}
}