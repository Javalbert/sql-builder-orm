package chan.shundat.albert.sqlbuilder;

public class ImmutableFrom extends From {
	public ImmutableFrom(From from) {
		nodes = NodeUtils.immutableNodes(from);
	}
}