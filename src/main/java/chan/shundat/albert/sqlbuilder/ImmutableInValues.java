package chan.shundat.albert.sqlbuilder;

public class ImmutableInValues extends InValues {
	public ImmutableInValues(InValues values) {
		nodes = NodeUtils.immutableNodes(values);
	}
}