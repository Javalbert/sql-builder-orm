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

import java.util.ArrayList;
import java.util.List;
import java.util.Objects;

import com.github.javalbert.utils.string.Strings;

/**
 * Can build both Expressions and Conditions
 * @author Albert
 *
 */
public class Case implements Aliasable, ConditionBuilding<Case>, ExpressionBuilding<Case>, Node<Case>, NodeHolder {
	public static final Token ELSE = new ConstantToken(Keywords.ELSE);
	public static final Token END = new ConstantToken(Keywords.END, false);
	public static final Token THEN = new ConstantToken(Keywords.THEN);
	public static final Token WHEN = new ConstantToken(Keywords.WHEN);

	protected String alias;
	@SuppressWarnings("rawtypes")
	protected List<Node> nodes = new ArrayList<>();
	private Column workColumn;

	@Override
	public String getAlias() { return alias; }
	@Override
	public void setAlias(String alias) {
		this.alias = Strings.safeTrim(alias);
	}
	@SuppressWarnings("rawtypes")
	@Override
	public List<Node> getNodes() { return nodes; }
	@Override
	public int getType() { return TYPE_CASE; }
	
	public Case() {}
	
	public Case(Case sqlCase) {
		alias = sqlCase.getAlias();
		nodes = NodeUtils.mutableNodes(sqlCase);
	}
	
	@Override
	public boolean accept(NodeVisitor visitor) {
		return NodeUtils.visit(this, nodes, visitor);
	}
	
	@Override
	public Case immutable() {
		return new ImmutableCase(this);
	}
	
	@Override
	public Case mutable() {
		return new Case(this);
	}
	
	/* BEGIN Fluent API */
	
	@Override
	public Case and() {
		nodes.add(LogicalOperator.AND);
		return this;
	}
	
	@Override
	public Case append(String token) {
		return append(token, true);
	}
	
	@Override
	public Case append(String token, boolean isNextNodeAnExpression) {
		nodes.add(new Token(token, isNextNodeAnExpression));
		return this;
	}
	
	@Override
	public Case column(String name) {
		NodeUtils.addColumn(this, workColumn, name);
		workColumn = null;
		return this;
	}
	
	public Case condition(Condition condition) {
		nodes.add(Objects.requireNonNull(condition, "condition cannot be null"));
		return this;
	}

	public Case end() {
		nodes.add(END);
		return this;
	}

	@Override
	public Case expression(Expression expression) {
		nodes.add(Objects.requireNonNull(expression, "expression cannot be null"));
		return this;
	}
	
	@Override
	public Case function(Function function) {
		nodes.add(Objects.requireNonNull(function, "function cannot be null"));
		return this;
	}

	@Override
	public Case group(Condition condition) {
		nodes.add(Objects.requireNonNull(condition, "condition cannot be null"));
		return this;
	}
	
	/**
	 * ELSE after CASE-WHEN-THEN
	 * @return
	 */
	public Case ifElse() {
		nodes.add(ELSE);
		return this;
	}
	
	@Override
	public Case literal(Boolean bool) {
		nodes.add(bool != null ? new LiteralBoolean(bool) : new LiteralNull());
		return this;
	}
	
	@Override
	public Case literal(Number number) {
		nodes.add(number != null ? new LiteralNumber(number) : new LiteralNull());
		return this;
	}
	
	@Override
	public Case literal(String str) {
		nodes.add(str != null ? new LiteralString(str) : new LiteralNull());
		return this;
	}
	
	@Override
	public Case literalNull() {
		nodes.add(new LiteralNull());
		return this;
	}
	
	@Override
	public Case param(String name) {
		nodes.add(new Param(name));
		return this;
	}
	
	@Override
	public Case predicate(Predicate predicate) {
		nodes.add(Objects.requireNonNull(predicate, "predicate cannot be null"));
		return this;
	}

	@Override
	public Case or() {
		nodes.add(LogicalOperator.OR);
		return this;
	}
	
	@Override
	public Case sqlCase(Case sqlCase) {
		nodes.add(sqlCase);
		return this;
	}
	
	@Override
	public Case subquery(Select select) {
		nodes.add(Objects.requireNonNull(select, "select cannot be null"));
		return this;
	}
	
	@Override
	public Case tableAlias(String alias) {
		workColumn = Column.byAlias(alias);
		return this;
	}
	
	@Override
	public Case tableName(String name) {
		workColumn = Column.byTableName(name);
		return this;
	}
	
	public Case then() {
		nodes.add(THEN);
		return this;
	}
	
	public Case when() {
		nodes.add(WHEN);
		return this;
	}
	
	/* END Fluent API */
}