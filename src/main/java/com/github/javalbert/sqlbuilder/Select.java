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
public class Select implements Aliasable, DMLStatement<Select>, NodeHolder {
	protected String alias;
	protected List<Node> nodes = new ArrayList<>();

	@Override
	public String getAlias() { return alias; }
	@Override
	public void setAlias(String alias) { this.alias = alias; }
	@Override
	public List<Node> getNodes() { return nodes; }
	@Override
	public int getType() { return TYPE_SELECT; }

	public Select() {}
	
	public Select(Select select) {
		alias = select.getAlias();
		nodes = NodeUtils.mutableNodes(select);
	}
	
	@Override
	public boolean accept(NodeVisitor visitor) {
		return NodeUtils.visit(this, nodes, visitor);
	}
	
	@Override
	public Select immutable() {
		Select select = new ImmutableSelect(this);
		return select;
	}
	
	@Override
	public Select mutable() {
		Select select = new Select(this);
		return select;
	}
	
	/* BEGIN Fluent API */
	
	public Select except() {
		return except(null);
	}
	
	public Select except(Select select) {
		nodes.add(SetOperator.except(select));
		return this;
	}
	
	public Select fetch(int n) {
		nodes.add(new Fetch(n));
		return this;
	}
	
	public Select from(From from) {
		nodes.add(Objects.requireNonNull(from, "from cannot be null"));
		return this;
	}
	
	public Select groupBy(GroupBy groupBy) {
		nodes.add(Objects.requireNonNull(groupBy, "groupBy cannot be null"));
		return this;
	}
	
	public Select having(Having having) {
		nodes.add(Objects.requireNonNull(having, "having cannot be null"));
		return this;
	}
	
	public Select intersect() {
		return intersect(null);
	}
	
	public Select intersect(Select select) {
		nodes.add(SetOperator.intersect(select));
		return this;
	}
	
	public Select list(SelectList list) {
		nodes.add(Objects.requireNonNull(list, "list cannot be null"));
		return this;
	}

	public Select offset(int skip) {
		nodes.add(new Offset(skip));
		return this;
	}
	
	public Select orderBy(OrderBy orderBy) {
		nodes.add(Objects.requireNonNull(orderBy, "orderBy cannot be null"));
		return this;
	}
	
	public Select query(Select select) {
		nodes.add(Objects.requireNonNull(select, "select cannot be null"));
		return this;
	}
	
	public Select union() {
		return union(null);
	}
	
	public Select union(Select select) {
		nodes.add(SetOperator.union(select));
		return this;
	}
	
	public Select unionAll() {
		return unionAll(null);
	}
	
	public Select unionAll(Select select) {
		nodes.add(SetOperator.unionAll(select));
		return this;
	}
	
	public Select where(Where where) {
		nodes.add(Objects.requireNonNull(where, "where cannot be null"));
		return this;
	}
	
	public Select with(With with) {
		nodes.add(Objects.requireNonNull(with, "with cannot be null"));
		return this;
	}
	
	/* END Fluent API */
}