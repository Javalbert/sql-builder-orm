package chan.shundat.albert.sqlbuilder;

public class ImmutableColumn extends Column {
	@Override
	public void setAlias(String alias) {
		throw new UnsupportedOperationException("immutable");
	}
	@Override
	public void setName(String name) {
		throw new UnsupportedOperationException("immutable");
	}
	@Override
	public void setPrefix(Prefix prefix) {
		throw new UnsupportedOperationException("immutable");
	}
	@Override
	public void setPrefixValue(String prefixValue) {
		throw new UnsupportedOperationException("immutable");
	}
	
	public ImmutableColumn(Column column) {
		super(column.getPrefixValue(), 
				column.getPrefix(), 
				column.getName(), 
				column.getAlias());
	}
}