/*******************************************************************************
 * Copyright (c) 2016 Albert Shun-Dat Chan
 *
 * Licensed under MIT License
 * See LICENSE file at the root of the project or
 * https://github.com/Javalbert/sql-builder-orm/blob/master/LICENSE
 *******************************************************************************/
package chan.shundat.albert.sqlbuilder.vendor;

import chan.shundat.albert.sqlbuilder.BinaryOperator;
import chan.shundat.albert.sqlbuilder.Literal;
import chan.shundat.albert.sqlbuilder.Node;
import chan.shundat.albert.sqlbuilder.Token;

@SuppressWarnings("rawtypes")
public class MSSQL extends ANSI {
	public static final String LITERAL_BOOLEAN_FALSE = "0";
	public static final String LITERAL_BOOLEAN_TRUE = "1";
	public static final String STRING_CONCAT = "+";
	
	@Override
	public String print(Literal literal) {
		StringBuilder builder = new StringBuilder();
		switch (literal.getType()) {
			case Node.TYPE_LITERAL_BOOLEAN:
				builder.append((boolean)literal.getValue() ? LITERAL_BOOLEAN_TRUE : LITERAL_BOOLEAN_FALSE);
				break;
			default:
				return super.print(literal);
		}
		appendAsKeyword(builder, literal);
		return builder.toString();
	}
	
	@Override
	public String print(Token token) {
		if (token == BinaryOperator.CONCAT) {
			return STRING_CONCAT;
		}
		return super.print(token);
	}
}