package chan.shundat.albert.sqlbuilder;

public class ImmutableToken extends Token {
	@Override
	public void setToken(String token) {
		throw new UnsupportedOperationException("token is immutable");
	}
	
	public ImmutableToken(String token) {
		super(token);
	}
	
	public ImmutableToken(String token, boolean isNextNodeAnExpression) {
		super(token, isNextNodeAnExpression);
	}

	public ImmutableToken(Token token) {
		this(token.getToken(), token.isNextNodeAnExpression());
	}
}