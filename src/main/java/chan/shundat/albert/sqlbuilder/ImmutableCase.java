package chan.shundat.albert.sqlbuilder;

public class ImmutableCase extends Case {
	@Override
	public void setAlias(String alias) {
		throw new UnsupportedOperationException("immutable");
	}
	
	public ImmutableCase(Case sqlCase) {
		alias = sqlCase.getAlias();
		nodes = NodeUtils.immutableNodes(sqlCase);
	}
}