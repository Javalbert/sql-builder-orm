package chan.shundat.albert.sqlbuilder.parser;

import java.math.BigDecimal;

import chan.shundat.albert.sqlbuilder.ExpressionBuilding;

public class NumberLiteralParseToken extends ParseToken implements LiteralParseToken {
	private BigDecimal number;
	
	public NumberLiteralParseToken(String token) {
		super(token);
		number = new BigDecimal(token);
	}

	@Override
	public <T> void toExpression(ExpressionBuilding<T> expression) {
		expression.literal(number);
	}
}