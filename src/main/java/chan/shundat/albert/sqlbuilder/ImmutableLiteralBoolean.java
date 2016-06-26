package chan.shundat.albert.sqlbuilder;

public class ImmutableLiteralBoolean extends LiteralBoolean {
	@Override
	public void setAlias(String alias) {
		throw new UnsupportedOperationException("immutable");
	}
	@Override
	public void setValue(Boolean value) {
		throw new UnsupportedOperationException("immutable");
	}
	
	public ImmutableLiteralBoolean(LiteralBoolean literal) {
		super(literal.getValue());
		alias = literal.getAlias();
	}
}