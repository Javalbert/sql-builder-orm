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

public class Having extends Condition {
	@Override
	public int getType() {
		return TYPE_HAVING;
	}
	
	public Having() {}
	
	public Having(Condition having) {
		nodes = NodeUtils.mutableNodes(having);
	}
	
	@Override
	public Having immutable() {
		return new ImmutableHaving(this);
	}
	
	@Override
	public Having mutable() {
		return new Having(this);
	}
	
	/* BEGIN Fluent API */
	
	@Override
	public Having and() {
		super.and();
		return this;
	}
	
	@Override
	public Having group(Condition condition) {
		super.group(condition);
		return this;
	}

	@Override
	public Having predicate(Predicate predicate) {
		super.predicate(predicate);
		return this;
	}

	@Override
	public Having or() {
		super.or();
		return this;
	}
	
	/* END Fluent API */
}