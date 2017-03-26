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

public class From implements Node<From>, NodeHolder, TableNameSpecifier<From> {
	public static final Token LEFT_PARENTHESIS = new ConstantToken("(");
	public static final Token RIGHT_PARENTHESIS = new ConstantToken(")");
	
	@SuppressWarnings("rawtypes")
	protected List<Node> nodes = new ArrayList<>();
	
	@SuppressWarnings("rawtypes")
	@Override
	public List<Node> getNodes() { return nodes; }
	@Override
	public int getType() { return TYPE_FROM; }
	
	public From() {}
	
	public From(From from) {
		nodes = NodeUtils.mutableNodes(from);
	}
	
	@Override
	public boolean accept(NodeVisitor visitor) {
		return NodeUtils.visit(this, nodes, visitor);
	}
	
	@Override
	public From immutable() {
		return new ImmutableFrom(this);
	}
	
	@Override
	public From mutable() {
		return new From(this);
	}
	
	/* BEGIN Fluent API */
	
	public From append(String token) {
		nodes.add(new Token(token));
		return this;
	}
	
	public From as(String alias) {
		NodeUtils.setAlias(alias, nodes);
		return this;
	}
	
	public From fullOuterJoin() {
		nodes.add(Join.FULL_OUTER_JOIN);
		return this;
	}
	
	public From inlineView(Select select) {
		nodes.add(Objects.requireNonNull(select, "select cannot be null"));
		return this;
	}
	
	public From innerJoin() {
		nodes.add(Join.INNER_JOIN);
		return this;
	}
	
	public From leftOuterJoin() {
		nodes.add(Join.LEFT_OUTER_JOIN);
		return this;
	}
	
	public From leftParens() {
		nodes.add(LEFT_PARENTHESIS);
		return this;
	}
	
	public From on(Condition condition) {
		nodes.add(Objects.requireNonNull(condition, "condition cannot be null"));
		return this;
	}
	
	public From rightOuterJoin() {
		nodes.add(Join.RIGHT_OUTER_JOIN);
		return this;
	}
	
	public From rightParens() {
		nodes.add(RIGHT_PARENTHESIS);
		return this;
	}

	public From tableAlias(String alias) {
		Table table = new Table(null, alias);
		nodes.add(table);
		return this;
	}
	
	@Override
	public From tableName(String name) {
		nodes.add(new Table(name));
		return this;
	}
	
	/* END Fluent API */
}