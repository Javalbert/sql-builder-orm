/*******************************************************************************
 * Copyright 2017 Albert Shun-Dat Chan
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

public class Merge implements DMLStatement<Merge>, NodeHolder, TableNameSpecifier<Merge> {
	public static final Token DELETE = new ConstantToken(Keywords.DELETE);
	public static final Token THEN = new ConstantToken(Keywords.THEN);
	public static final Token WHEN_MATCHED = new ConstantToken(Keywords.WHEN_MATCHED);
	public static final Token WHEN_NOT_MATCHED = new ConstantToken(Keywords.WHEN_NOT_MATCHED);
	
	@SuppressWarnings("rawtypes")
	protected List<Node> nodes = new ArrayList<>();

	@SuppressWarnings("rawtypes")
	@Override
	public List<Node> getNodes() {
		return nodes;
	}
	
	@Override
	public int getType() {
		return TYPE_MERGE;
	}
	
	public Merge() {}
	
	public Merge(Merge merge) {
		nodes = NodeUtils.mutableNodes(merge);
	}

	@Override
	public boolean accept(NodeVisitor visitor) {
		return NodeUtils.visit(this, nodes, visitor);
	}

	@Override
	public Merge immutable() {
		return new ImmutableMerge(this);
	}

	@Override
	public Merge mutable() {
		return new Merge(this);
	}
	
	/* START Fluent API */
	
	/**
	 * Simply delegates to <code>Merge.on(Condition)</code> but should be called after calling
	 * either <code>Merge.whenMatched()</code> or <code>Merge.whenNotMatched()</code> and
	 * improves code readability
	 * @param searchCondition
	 * @return
	 */
	public Merge and(Condition searchCondition) {
		return on(searchCondition);
	}
	
	public Merge as(String alias) {
		NodeUtils.setAlias(alias, nodes);
		return this;
	}
	
	public Merge delete() {
		nodes.add(DELETE);
		return this;
	}
	
	public Merge insert(Insert insert) {
		nodes.add(Objects.requireNonNull(insert, "insert cannot be null"));
		return this;
	}
	
	/**
	 * Simply delegates to <code>Merge.tableName(String)</code> but improves code readability
	 * @param tableName
	 * @return
	 */
	public Merge into(String tableName) {
		return tableName(tableName);
	}
	
	public Merge on(Condition searchCondition) {
		nodes.add(Objects.requireNonNull(searchCondition, "condition cannot be null"));
		return this;
	}
	
	@Override
	public Merge tableName(String name) {
		nodes.add(new Table(name));
		return this;
	}
	
	public Merge then() {
		nodes.add(THEN);
		return this;
	}
	
	public Merge update(Update update) {
		nodes.add(Objects.requireNonNull(update, "update cannot be null"));
		return this;
	}
	
	public Merge using(Select select) {
		nodes.add(Objects.requireNonNull(select, "select cannot be null"));
		return this;
	}

	/**
	 * Simply delegates to <code>Merge.tableName(String)</code> but improves code readability
	 * @param select
	 * @return
	 */
	public Merge using(String tableName) {
		return tableName(tableName);
	}
	
	public Merge whenMatched() {
		nodes.add(WHEN_MATCHED);
		return this;
	}
	
	public Merge whenMatchedThen() {
		return whenMatched().then();
	}
	
	public Merge whenNotMatched() {
		nodes.add(WHEN_NOT_MATCHED);
		return this;
	}
	
	public Merge whenNotMatchedThen() {
		return whenNotMatched().then();
	}
	
	/* END Fluent API */
}
