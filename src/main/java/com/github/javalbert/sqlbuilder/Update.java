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

public class Update implements
		DMLStatement<Update>, 
		Node<Update>,
		NodeHolder,
		TableNameSpecifier<Update> {
	@SuppressWarnings("rawtypes")
	protected List<Node> nodes = new ArrayList<>();
	
	@SuppressWarnings("rawtypes")
	@Override
	public List<Node> getNodes() { return nodes; }
	@Override
	public int getType() { return TYPE_UPDATE; }

	public Update() {}
	
	public Update(String tableName) {
		tableName(tableName);
	}
	
	public Update(Update update) {
		nodes = NodeUtils.mutableNodes(update);
	}
	
	@Override
	public boolean accept(NodeVisitor visitor) {
		return NodeUtils.visit(this, nodes, visitor);
	}

	@Override
	public Update immutable() {
		return new ImmutableUpdate(this);
	}

	@Override
	public Update mutable() {
		return new Update(this);
	}
	
	/* BEGIN Fluent API */
	
	public Update set(SetValues values) {
		nodes.add(Objects.requireNonNull(values, "values cannot be null"));
		return this;
	}
	
	@Override
	public Update tableName(String name) {
		nodes.add(new Table(name));
		return this;
	}
	
	public Update where(Where where) {
		nodes.add(Objects.requireNonNull(where, "where cannot be null"));
		return this;
	}
	
	public Update with(With with) {
		nodes.add(Objects.requireNonNull(with, "with cannot be null"));
		return this;
	}
	
	/* END Fluent API */
}