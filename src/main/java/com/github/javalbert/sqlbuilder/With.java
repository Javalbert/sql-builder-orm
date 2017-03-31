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

public class With implements Node<With>, NodeHolder {
	@SuppressWarnings("rawtypes")
	protected List<Node> nodes = new ArrayList<>();

	@SuppressWarnings("rawtypes")
	@Override
	public List<Node> getNodes() { return nodes; }
	@Override
	public int getType() { return TYPE_WITH; }

	public With() {}
	
	public With(With with) {
		nodes = NodeUtils.mutableNodes(with);
	}
	
	@Override
	public boolean accept(NodeVisitor visitor) {
		return NodeUtils.visit(this, nodes, visitor);
	}
	
	@Override
	public With immutable() {
		return new ImmutableWith(this);
	}
	
	@Override
	public With mutable() {
		return new With(this);
	}
	
	/* BEGIN Fluent API */

	public With as(Select select) {
		getLast().setSelect(Objects.requireNonNull(select, "select cannot be null"));
		return this;
	}
	
	public With column(String column) {
		CommonTableExpression cte = getLast();
		
		List<String> columns = cte.getColumns();
		if (columns == null) {
			columns = new ArrayList<>();
			cte.setColumns(columns);
		}
		
		columns.add(Strings.safeTrim(column));
		return this;
	}

	public With name(String name) {
		CommonTableExpression cte = new CommonTableExpression(name);
		nodes.add(cte);
		return this;
	}
	
	/* END Fluent API */
	
	private CommonTableExpression getLast() {
		CommonTableExpression cte = !nodes.isEmpty() ? (CommonTableExpression)nodes.get(nodes.size() - 1) : null;
		if (cte == null) {
			throw new IllegalStateException("No nodes");
		}
		return cte;
	}
}