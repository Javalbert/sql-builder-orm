package chan.shundat.albert.sqlbuilder;

public class ImmutableTable extends Table {
	@Override
	public void setAlias(String alias) {
		throw new UnsupportedOperationException("immutable");
	}
	@Override
	public void setName(String name) {
		throw new UnsupportedOperationException("immutable");
	}
	
	public ImmutableTable(Table table) {
		super(table.getName(), table.getAlias());
	}
}