package chan.shundat.albert.sqlbuilder;

public class ImmutableExpression extends Expression {
	@Override
	public void setAlias(String alias) {
		throw new UnsupportedOperationException("immutable");
	}
	
	public ImmutableExpression(Expression expression) {
		nodes = NodeUtils.immutableNodes(expression);
		alias = expression.getAlias();
	}
}