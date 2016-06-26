package chan.shundat.albert.sqlbuilder;

import chan.shundat.albert.utils.string.Strings;

public class Expression extends ExpressionBuilder<Expression> implements Aliasable, Node<Expression> {
	protected String alias;
	
	@Override
	public String getAlias() { return alias; }
	@Override
	public void setAlias(String alias) { this.alias = Strings.safeTrim(alias); }
	@Override
	public int getType() {
		return TYPE_EXPRESSION;
	}
	
	public Expression() {}
	
	public Expression(Expression expression) {
		alias = expression.getAlias();
		nodes = NodeUtils.mutableNodes(expression);
	}
	
	@Override
	public boolean accept(NodeVisitor visitor) {
		return NodeUtils.visit(this, nodes, visitor);
	}
	
	@Override
	public Expression immutable() {
		Expression expression = new ImmutableExpression(this);
		return expression;
	}
	
	@Override
	public Expression mutable() {
		Expression expression = new Expression(this);
		return expression;
	}
	
	/* BEGIN Fluent API */
	
	public Expression concat() {
		nodes.add(BinaryOperator.CONCAT);
		return this;
	}
	
	public Expression divide() {
		nodes.add(ArithmeticOperator.DIVIDE);
		return this;
	}
	
	public Expression minus() {
		nodes.add(ArithmeticOperator.MINUS);
		return this;
	}
	
	public Expression mod() {
		nodes.add(ArithmeticOperator.MOD);
		return this;
	}
	
	public Expression multiply() {
		nodes.add(ArithmeticOperator.MULTIPLY);
		return this;
	}

	public Expression plus() {
		nodes.add(ArithmeticOperator.PLUS);
		return this;
	}
	
	/* END Fluent API */
}