package chan.shundat.albert.sqlbuilder;

public class LiteralNull extends Literal<Object> {
	@Override
	public int getType() {
		return TYPE_LITERAL_NULL;
	}
	@Override
	public void setValue(Object value) {
		throw new UnsupportedOperationException("cannot set value for NULL literal");
	}

	public LiteralNull() {}
	
	public LiteralNull(LiteralNull literal) {
		super(literal.getValue());
		alias = literal.getAlias();
	}
	
	@Override
	public LiteralNull immutable() {
		LiteralNull literal = new ImmutableLiteralNull(this);
		return literal;
	}
	
	@Override
	public LiteralNull mutable() {
		LiteralNull literal = new LiteralNull(this);
		return literal;
	}
}