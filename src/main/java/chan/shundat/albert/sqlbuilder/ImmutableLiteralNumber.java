package chan.shundat.albert.sqlbuilder;

public class ImmutableLiteralNumber extends LiteralNumber {
	@Override
	public void setAlias(String alias) {
		throw new UnsupportedOperationException("immutable");
	}
	@Override
	public void setValue(Number value) {
		throw new UnsupportedOperationException("immutable");
	}
	
	public ImmutableLiteralNumber(LiteralNumber literal) {
		super(literal.getValue());
		alias = literal.getAlias();
	}
}