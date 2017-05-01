/*******************************************************************************
 * Copyright 2017 Albert Shun-Dat Chan
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
package com.github.javalbert.sqlbuilder.dsl;

public class ExpressionOperatorNode implements ExpressionNode {
	public static final ExpressionOperatorNode CONCAT = new ExpressionOperatorNode(ExpressionOperator.CONCAT);
	public static final ExpressionOperatorNode DIVIDE = new ExpressionOperatorNode(ExpressionOperator.DIVIDE);
	public static final ExpressionOperatorNode MINUS = new ExpressionOperatorNode(ExpressionOperator.MINUS);
	public static final ExpressionOperatorNode MOD = new ExpressionOperatorNode(ExpressionOperator.MOD);
	public static final ExpressionOperatorNode MULTIPLY = new ExpressionOperatorNode(ExpressionOperator.MULTIPLY);
	public static final ExpressionOperatorNode PLUS = new ExpressionOperatorNode(ExpressionOperator.PLUS);
	
	private final ExpressionOperator expressionOperator;
	
	public ExpressionOperator getExpressionOperator() {
		return expressionOperator;
	}
	@Override
	public int getExpressionType() {
		return EXPRESSION_OPERATOR;
	}
	
	private ExpressionOperatorNode(ExpressionOperator expressionOperator) {
		this.expressionOperator = expressionOperator;
	}
	
	public static ExpressionOperatorNode get(ExpressionOperator operator) {
		switch (operator) {
			case CONCAT: return CONCAT;
			case DIVIDE: return DIVIDE;
			case MINUS: return MINUS;
			case MOD: return MOD;
			case MULTIPLY: return MULTIPLY;
			case PLUS: return PLUS;
			default:
				throw new IllegalArgumentException("unsupported ExpressionOperator " + operator);
		}
	}
}
