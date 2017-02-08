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
public class GroupBy implements ColumnBuilder<GroupBy>, Node<GroupBy>, NodeHolder {
	protected List<Node> nodes = new ArrayList<>();
	private Column workColumn;
	
	@Override
	public List<Node> getNodes() { return nodes; }
	@Override
	public int getType() { return TYPE_GROUP_BY; }
	
	public GroupBy() {}
	
	public GroupBy(GroupBy groupBy) {
		nodes = NodeUtils.mutableNodes(groupBy);
	}
	
	@Override
	public boolean accept(NodeVisitor visitor) {
		return NodeUtils.visit(this, nodes, visitor);
	}
	
	@Override
	public GroupBy immutable() {
		GroupBy groupBy = new ImmutableGroupBy(this);
		return groupBy;
	}
	
	@Override
	public GroupBy mutable() {
		GroupBy groupBy = new GroupBy(this);
		return groupBy;
	}
	
	/* BEGIN Fluent API */
	
	@Override
	public GroupBy column(String name) {
		NodeUtils.addColumn(this, workColumn, name);
		workColumn = null;
		return this;
	}

	@Override
	public GroupBy tableAlias(String alias) {
		workColumn = Column.byAlias(alias);
		return this;
	}

	@Override
	public GroupBy tableName(String name) {
		workColumn = Column.byTableName(name);
		return this;
	}
	
	/* END Fluent API */
}