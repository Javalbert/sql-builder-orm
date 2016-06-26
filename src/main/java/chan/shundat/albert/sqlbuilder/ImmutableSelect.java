package chan.shundat.albert.sqlbuilder;

public class ImmutableSelect extends Select {
	@Override
	public void setAlias(String alias) {
		throw new UnsupportedOperationException("immutable");
	}

	public ImmutableSelect(Select select) {
		alias = select.getAlias();
		nodes = NodeUtils.immutableNodes(select);
	}
}