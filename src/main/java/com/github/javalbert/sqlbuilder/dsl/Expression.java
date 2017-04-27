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

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.List;
import java.util.Objects;

public class Expression
implements ExpressionBuilder, ExpressionNode, Predicand,
SelectColumn<Expression>, ValueExpression {
	private String alias;
	private boolean grouped;
	private List<ExpressionNode> nodes = Collections.emptyList();
	
	@Override
	public String getAlias() {
		return alias;
	}
	public boolean isGrouped() {
		return grouped;
	}
	public List<ExpressionNode> getNodes() {
		return nodes;
	}
	@Override
	public int getNodeType() {
		return NODE_EXPRESSION;
	}
	@Override
	public int getExpressionType() {
		return EXPRESSION_EXPRESSION;
	}

	Expression(ExpressionBuilder left, ExpressionBuilder right, ExpressionOperator operator) {
		nodes = Arrays.asList(
				Objects.requireNonNull(left, "left cannot be null"),
				ExpressionOperatorNode.get(operator),
				Objects.requireNonNull(right, "right cannot be null"));
	}
	
	private Expression() {}
	
	@Override
	public Expression as(String alias) {
		Expression expression = copy();
		expression.alias = alias;
		return expression;
	}

	@Override
	public Expression concat(ExpressionBuilder right) {
		return grouped ? ExpressionBuilder.super.concat(right)
				: copyWithOperator(right, ExpressionOperator.CONCAT);
	}
	
	@Override
	public Expression divide(ExpressionBuilder right) {
		return grouped ? ExpressionBuilder.super.divide(right)
				: copyWithOperator(right, ExpressionOperator.DIVIDE);
	}
	
	@Override
	public Expression minus(ExpressionBuilder right) {
		return grouped ? ExpressionBuilder.super.minus(right)
				: copyWithOperator(right, ExpressionOperator.MINUS);
	}
	
	@Override
	public Expression mod(ExpressionBuilder right) {
		return grouped ? ExpressionBuilder.super.mod(right)
				: copyWithOperator(right, ExpressionOperator.MOD);
	}
	
	@Override
	public Expression multiply(ExpressionBuilder right) {
		return grouped ? ExpressionBuilder.super.multiply(right)
				: copyWithOperator(right, ExpressionOperator.MULTIPLY);
	}
	
	@Override
	public Expression plus(ExpressionBuilder right) {
		return grouped ? ExpressionBuilder.super.plus(right)
				: copyWithOperator(right, ExpressionOperator.PLUS);
	}
	
	Expression copy() {
		Expression copy = new Expression();
		copy.alias = alias;
		copy.grouped = grouped;
		copy.nodes = nodes;
		return copy;
	}
	
	Expression grouped() {
		if (grouped) {
			return this;
		}
		Expression expression = copy();
		expression.grouped = true;
		return expression;
	}
	
	private Expression copyWithOperator(ExpressionBuilder right, ExpressionOperator operator) {
		Expression expression = copy();
		List<ExpressionNode> nodes = new ArrayList<>(this.nodes);
		nodes.add(ExpressionOperatorNode.get(operator));
		nodes.add(Objects.requireNonNull(right, "right cannot be null"));
		expression.nodes = Collections.unmodifiableList(nodes);
		return expression;
	}
}
