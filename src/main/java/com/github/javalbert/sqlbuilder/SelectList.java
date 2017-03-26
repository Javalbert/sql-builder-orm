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

public class SelectList extends ExpressionBuilder<SelectList> implements Node<SelectList> {
	public static final Token DISTINCT = new ConstantToken(Keywords.DISTINCT, true);

	@Override
	public int getType() { return TYPE_SELECT_LIST; }
	
	public SelectList() {}
	
	public SelectList(SelectList list) {
		nodes = NodeUtils.mutableNodes(list);
	}
	
	@Override
	public boolean accept(NodeVisitor visitor) {
		return NodeUtils.visit(this, nodes, visitor);
	}
	
	@Override
	public SelectList immutable() {
		return new ImmutableSelectList(this);
	}
	
	@Override
	public SelectList mutable() {
		return new SelectList(this);
	}
	
	/* BEGIN Fluent API */
	
	public SelectList as(String alias) {
		NodeUtils.setAlias(alias, nodes);
		return this;
	}

	public SelectList distinct() {
		nodes.add(DISTINCT);
		return this;
	}
	
	/* END Fluent API */
}