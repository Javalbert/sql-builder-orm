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

@SuppressWarnings("rawtypes")
public class Insert implements DMLStatement<Insert>, Node<Insert>, NodeHolder {
	protected List<Node> nodes = new ArrayList<>();
	
	@Override
	public List<Node> getNodes() { return nodes; }
	@Override
	public int getType() { return TYPE_INSERT; }

	public Insert() {}

	public Insert(String name) {
		into(name);
	}
	
	public Insert(Insert insert) {
		nodes = NodeUtils.mutableNodes(insert);
	}
	
	@Override
	public boolean accept(NodeVisitor visitor) {
		return NodeUtils.visit(this, nodes, visitor);
	}
	
	@Override
	public Insert immutable() {
		Insert insert = new ImmutableInsert(this);
		return insert;
	}

	@Override
	public Insert mutable() {
		Insert insert = new Insert(this);
		return insert;
	}
	
	/* BEGIN Fluent API */
	
	public Insert columns(ColumnList columns) {
		nodes.add(Objects.requireNonNull(columns, "columns cannot be null"));
		return this;
	}
	
	public Insert into(String name) {
		Table table = new Table(name);
		nodes.add(table);
		return this;
	}
	
	public Insert values(ColumnValues values) {
		nodes.add(Objects.requireNonNull(values, "values cannot be null"));
		return this;
	}
	
	public Insert subselect(Select select) {
		nodes.add(Objects.requireNonNull(select, "select cannot be null"));
		return this;
	}

	public Insert with(With with) {
		nodes.add(Objects.requireNonNull(with, "with cannot be null"));
		return this;
	}
	
	/* END Fluent API */
}