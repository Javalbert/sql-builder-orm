package chan.shundat.albert.sqlbuilder;

public class LiteralString extends Literal<String> {
	@Override
	public int getType() {
		return TYPE_LITERAL_STRING;
	}
	@Override
	public void setValue(String value) {
		if (value == null) {
			throw new NullPointerException("value cannot be null");
		}
		super.setValue(value);
	}
	
	public LiteralString(LiteralString literal) {
		super(literal.getValue());
		alias = literal.getAlias();
	}
	
	public LiteralString(String value) {
		if (value == null) {
			throw new NullPointerException("value cannot be null");
		}
		this.value = value;
	}
	
	@Override
	public LiteralString immutable() {
		LiteralString literal = new ImmutableLiteralString(this);
		return literal;
	}
	
	@Override
	public LiteralString mutable() {
		LiteralString literal = new LiteralString(this);
		return literal;
	}
}