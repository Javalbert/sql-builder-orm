/*******************************************************************************
 * Copyright 2016 Albert Shun-Dat Chan
 *
 * Licensed under the Apache License, Version 2.0 (the "License"); you may not use this file except in
 * compliance with the License. You may obtain a copy of the License at
 *
 * http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software distributed under the License is
 * distributed on an "AS IS" BASIS, WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied. See
 * the License for the specific language governing permissions and limitations under the License.
 *******************************************************************************/
package com.github.javalbert.sqlbuilder;

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