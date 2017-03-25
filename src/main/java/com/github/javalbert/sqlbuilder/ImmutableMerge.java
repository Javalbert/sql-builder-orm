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

public class ImmutableMerge extends Merge {
	public ImmutableMerge(Merge merge) {
		nodes = NodeUtils.immutableNodes(merge);
	}
	
	/* START Fluent API */
	
	@Override
	public ImmutableMerge and(Condition searchCondition) {
		throw NodeUtils.immutableException();
	}
	
	@Override
	public ImmutableMerge as(String alias) {
		throw NodeUtils.immutableException();
	}
	
	@Override
	public ImmutableMerge delete() {
		throw NodeUtils.immutableException();
	}
	
	@Override
	public ImmutableMerge insert(Insert insert) {
		throw NodeUtils.immutableException();
	}
	
	@Override
	public Merge into(String tableName) {
		throw NodeUtils.immutableException();
	}

	@Override
	public ImmutableMerge on(Condition condition) {
		throw NodeUtils.immutableException();
	}
	
	@Override
	public ImmutableMerge tableName(String name) {
		throw NodeUtils.immutableException();
	}
	
	@Override
	public ImmutableMerge then() {
		throw NodeUtils.immutableException();
	}

	@Override
	public ImmutableMerge update(Update update) {
		throw NodeUtils.immutableException();
	}

	@Override
	public ImmutableMerge using(Select select) {
		throw NodeUtils.immutableException();
	}

	@Override
	public ImmutableMerge using(String tableName) {
		throw NodeUtils.immutableException();
	}

	@Override
	public ImmutableMerge whenMatched() {
		throw NodeUtils.immutableException();
	}
	
	@Override
	public ImmutableMerge whenMatchedThen() {
		throw NodeUtils.immutableException();
	}

	@Override
	public ImmutableMerge whenNotMatched() {
		throw NodeUtils.immutableException();
	}
	
	@Override
	public ImmutableMerge whenNotMatchedThen() {
		throw NodeUtils.immutableException();
	}
	
	/* END Fluent API */
}
