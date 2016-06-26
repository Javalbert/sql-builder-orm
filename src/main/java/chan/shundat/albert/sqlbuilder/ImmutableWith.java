package chan.shundat.albert.sqlbuilder;

public class ImmutableWith extends With {
	public ImmutableWith(With with) {
		nodes = NodeUtils.immutableNodes(with);
	}
}