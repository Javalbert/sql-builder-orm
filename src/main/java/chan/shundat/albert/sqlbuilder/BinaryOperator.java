/*******************************************************************************
 * Copyright (c) 2016 Albert Shun-Dat Chan
 *
 * Licensed under MIT License
 * See LICENSE file at the root of the project or
 * https://github.com/Javalbert/sql-builder-orm/blob/master/LICENSE
 *******************************************************************************/
package chan.shundat.albert.sqlbuilder;

public class BinaryOperator extends Token {
	public static final String STRING_CONCAT = "||";
	
	public static final BinaryOperator CONCAT = new BinaryOperator(STRING_CONCAT);
	
	@Override
	public void setToken(String token) {
		throw new UnsupportedOperationException("this is a flyweight object");
	}
	@Override
	public int getType() {
		return TYPE_BINARY_OPERATOR;
	}
	
	public BinaryOperator(BinaryOperator operator) {
		this(operator.getToken());
	}
	
	public BinaryOperator(String operator) {
		super(operator, true);
	}
	
	@Override
	public BinaryOperator immutable() {
		return this;
	}
	
	@Override
	public BinaryOperator mutable() {
		return this;
	}
}