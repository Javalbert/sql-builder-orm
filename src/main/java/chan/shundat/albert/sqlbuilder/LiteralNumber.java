package chan.shundat.albert.sqlbuilder;

public class LiteralNumber extends Literal<Number> {
	@Override
	public int getType() {
		return TYPE_LITERAL_NUMBER;
	}
	@Override
	public void setValue(Number value) {
		if (value == null) {
			throw new NullPointerException("value cannot be null");
		}
		super.setValue(value);
	}
	
	public LiteralNumber(LiteralNumber literal) {
		super(literal.getValue());
		alias = literal.getAlias();
	}
	
	public LiteralNumber(Number value) {
		if (value == null) {
			throw new NullPointerException("value cannot be null");
		}
		this.value = value;
	}
	
	@Override
	public LiteralNumber immutable() {
		LiteralNumber literal = new ImmutableLiteralNumber(this);
		return literal;
	}
	
	@Override
	public LiteralNumber mutable() {
		LiteralNumber literal = new LiteralNumber(this);
		return literal;
	}
}