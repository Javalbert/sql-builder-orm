package chan.shundat.albert.sqlbuilder;

public class LiteralBoolean extends Literal<Boolean> {
	@Override
	public int getType() {
		return TYPE_LITERAL_BOOLEAN;
	}
	@Override
	public void setValue(Boolean value) {
		if (value == null) {
			throw new NullPointerException("value cannot be null");
		}
		super.setValue(value);
	}
	
	public LiteralBoolean(LiteralBoolean literal) {
		super(literal.getValue());
		alias = literal.getAlias();
	}
	
	protected LiteralBoolean(Boolean value) {
		if (value == null) {
			throw new NullPointerException("value cannot be null");
		}
		this.value = value;
	}
	
	@Override
	public LiteralBoolean immutable() {
		LiteralBoolean literal = new ImmutableLiteralBoolean(this);
		return literal;
	}
	
	@Override
	public LiteralBoolean mutable() {
		LiteralBoolean literal = new LiteralBoolean(this);
		return literal;
	}
}