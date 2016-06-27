/*******************************************************************************
 * Copyright (c) 2016 Albert Shun-Dat Chan
 *
 * Licensed under MIT License
 * See LICENSE file at the root of the project or
 * https://github.com/Javalbert/sql-builder-orm/blob/master/LICENSE
 *******************************************************************************/
package chan.shundat.albert.sqlbuilder;

public class ArithmeticOperator extends BinaryOperator {
	public static final String STRING_DIVIDE = "/";
	public static final String STRING_MINUS = "-";
	public static final String STRING_MOD = "%";
	public static final String STRING_MULTIPLY = "*";
	public static final String STRING_PLUS = "+";

	public static final ArithmeticOperator DIVIDE = new ArithmeticOperator(STRING_DIVIDE);
	public static final ArithmeticOperator MINUS = new ArithmeticOperator(STRING_MINUS);
	public static final ArithmeticOperator MOD = new ArithmeticOperator(STRING_MOD);
	public static final ArithmeticOperator MULTIPLY = new ArithmeticOperator(STRING_MULTIPLY);
	public static final ArithmeticOperator PLUS = new ArithmeticOperator(STRING_PLUS);
	
	private ArithmeticOperator(String operator) {
		super(operator);
	}
	
	@Override
	public ArithmeticOperator immutable() {
		return this;
	}
	
	@Override
	public ArithmeticOperator mutable() {
		return this;
	}
}