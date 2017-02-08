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

@SuppressWarnings("rawtypes")
public class OrderBy implements ColumnBuilder<OrderBy>, Node<OrderBy>, NodeHolder {
	protected List<Node> nodes = new ArrayList<>();
	private Column workColumn;
	
	@Override
	public List<Node> getNodes() { return nodes; }
	@Override
	public int getType() { return TYPE_ORDER_BY; }
	
	public OrderBy() {}
	
	public OrderBy(OrderBy orderBy) {
		nodes = NodeUtils.mutableNodes(orderBy);
	}
	
	@Override
	public boolean accept(NodeVisitor visitor) {
		return NodeUtils.visit(this, nodes, visitor);
	}
	
	@Override
	public OrderBy immutable() {
		OrderBy orderBy = new ImmutableOrderBy(this);
		return orderBy;
	}
	
	@Override
	public OrderBy mutable() {
		OrderBy orderBy = new OrderBy(this);
		return orderBy;
	}
	
	/* BEGIN Fluent API */

	public OrderBy alias(String alias) {
		Column column = new Column(null, null, null, alias);
		nodes.add(column);
		return this;
	}
	
	public OrderBy asc() {
		nodes.add(SortType.ASC);
		return this;
	}
	
	@Override
	public OrderBy column(String name) {
		NodeUtils.addColumn(this, workColumn, name);
		workColumn = null;
		return this;
	}
	
	public OrderBy desc() {
		nodes.add(SortType.DESC);
		return this;
	}
	
	public OrderBy fetch(int n) {
		nodes.add(new Fetch(n));
		return this;
	}

	public OrderBy offset(int skip) {
		nodes.add(new Offset(skip));
		return this;
	}
	
	@Override
	public OrderBy tableAlias(String alias) {
		workColumn = Column.byAlias(alias);
		return this;
	}

	@Override
	public OrderBy tableName(String name) {
		workColumn = Column.byTableName(name);
		return this;
	}
	
	/* END Fluent API */
}