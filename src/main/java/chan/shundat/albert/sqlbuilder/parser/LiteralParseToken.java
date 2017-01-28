package chan.shundat.albert.sqlbuilder.parser;

import chan.shundat.albert.sqlbuilder.ExpressionBuilding;

public interface LiteralParseToken {
	<T> void toExpression(ExpressionBuilding<T> expression);
}