package chan.shundat.albert.sqlbuilder;

public class ImmutableLiteralNull extends LiteralNull {
	@Override
	public void setAlias(String alias) {
		throw new UnsupportedOperationException("immutable");
	}
	@Override
	public void setValue(Object value) {
		throw new UnsupportedOperationException("immutable");
	}
	
	public ImmutableLiteralNull(LiteralNull literal) {
		super(literal);
	}
}