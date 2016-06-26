package chan.shundat.albert.sqlbuilder;

public class Join extends Token {
	public static final Join FULL_OUTER_JOIN = new Join(Keywords.FULL_OUTER_JOIN);
	public static final Join INNER_JOIN = new Join(Keywords.INNER_JOIN);
	public static final Join LEFT_OUTER_JOIN = new Join(Keywords.LEFT_OUTER_JOIN);
	public static final Join RIGHT_OUTER_JOIN = new Join(Keywords.RIGHT_OUTER_JOIN);
	
	@Override
	public void setToken(String token) {
		throw new UnsupportedOperationException("this is a flyweight object");
	}
	
	private Join(String keyword) {
		super(keyword);
	}
	
	@Override
	public Join immutable() {
		return this;
	}
	
	@Override
	public Join mutable() {
		return this;
	}
}