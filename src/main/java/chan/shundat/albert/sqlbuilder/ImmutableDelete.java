package chan.shundat.albert.sqlbuilder;

public class ImmutableDelete extends Delete {
	public ImmutableDelete(Delete delete) {
		nodes = NodeUtils.immutableNodes(delete);
	}
}