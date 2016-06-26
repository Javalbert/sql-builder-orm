package chan.shundat.albert.sqlbuilder;

public class ImmutableLiteralString extends LiteralString {
	@Override
	public void setAlias(String alias) {
		throw new UnsupportedOperationException("immutable");
	}
	@Override
	public void setValue(String value) {
		throw new UnsupportedOperationException("immutable");
	}
	
	public ImmutableLiteralString(LiteralString literal) {
		super(literal.getValue());
		alias = literal.getAlias();
	}
}