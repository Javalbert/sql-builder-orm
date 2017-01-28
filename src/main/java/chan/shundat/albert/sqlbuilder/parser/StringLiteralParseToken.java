package chan.shundat.albert.sqlbuilder.parser;

import chan.shundat.albert.sqlbuilder.ExpressionBuilding;
import chan.shundat.albert.sqlbuilder.SqlStringUtils;

public class StringLiteralParseToken extends ParseToken implements LiteralParseToken {
	private String value;

	public String getValue() { return value; }
	
	public StringLiteralParseToken(String str) {
		super(SqlStringUtils.createLiteralToken(str));
		value = str;
	}

	@Override
	public <T> void toExpression(ExpressionBuilding<T> expression) {
		expression.literal(value);
	}
}