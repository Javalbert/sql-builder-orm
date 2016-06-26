package chan.shundat.albert.sqlbuilder;

public class ImmutableSetValue extends SetValue {
	public ImmutableSetValue(SetValue value) {
		nodes = NodeUtils.immutableNodes(value);
	}
}