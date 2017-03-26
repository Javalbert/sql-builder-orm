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

import com.github.javalbert.utils.string.Strings;

public class Expression extends ExpressionBuilder<Expression> implements Aliasable, Node<Expression> {
	protected String alias;
	
	@Override
	public String getAlias() { return alias; }
	@Override
	public void setAlias(String alias) { this.alias = Strings.safeTrim(alias); }
	@Override
	public int getType() {
		return TYPE_EXPRESSION;
	}
	
	public Expression() {}
	
	public Expression(Expression expression) {
		alias = expression.getAlias();
		nodes = NodeUtils.mutableNodes(expression);
	}
	
	@Override
	public boolean accept(NodeVisitor visitor) {
		return NodeUtils.visit(this, nodes, visitor);
	}
	
	@Override
	public Expression immutable() {
		return new ImmutableExpression(this);
	}
	
	@Override
	public Expression mutable() {
		return new Expression(this);
	}
	
	/* BEGIN Fluent API */
	
	public Expression concat() {
		nodes.add(BinaryOperator.CONCAT);
		return this;
	}
	
	public Expression divide() {
		nodes.add(ArithmeticOperator.DIVIDE);
		return this;
	}
	
	public Expression minus() {
		nodes.add(ArithmeticOperator.MINUS);
		return this;
	}
	
	public Expression mod() {
		nodes.add(ArithmeticOperator.MOD);
		return this;
	}
	
	public Expression multiply() {
		nodes.add(ArithmeticOperator.MULTIPLY);
		return this;
	}

	public Expression plus() {
		nodes.add(ArithmeticOperator.PLUS);
		return this;
	}
	
	/* END Fluent API */
}