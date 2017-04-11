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

import java.util.Objects;

public class Expression
implements ExpressionBuilder, Predicand, SelectColumn<Expression> {
	private String alias;
	private ExpressionBuilder left;
	private ExpressionOperator operator;
	private ExpressionBuilder right;
	
	@Override
	public String getAlias() {
		return alias;
	}
	public ExpressionBuilder getLeft() {
		return left;
	}
	public ExpressionOperator getOperator() {
		return operator;
	}
	public ExpressionBuilder getRight() {
		return right;
	}

	Expression(ExpressionBuilder left, ExpressionBuilder right, ExpressionOperator operator) {
		this.left = Objects.requireNonNull(left, "left cannot be null");
		this.operator = Objects.requireNonNull(operator, "operator cannot be null");
		this.right = Objects.requireNonNull(right, "right cannot be null");
	}
	
	@Override
	public Expression as(String alias) {
		Expression expression = copy();
		expression.alias = alias;
		return expression;
	}
	
	Expression copy() {
		Expression copy = new Expression(left, right, operator);
		copy.alias = alias;
		return copy;
	}
}
