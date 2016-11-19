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
package chan.shundat.albert.sqlbuilder;

public class Where extends Condition {
	@Override
	public int getType() {
		return TYPE_WHERE;
	}
	
	public Where() {}
	
	public Where(Condition where) {
		nodes = NodeUtils.mutableNodes(where);
	}
	
	@Override
	public Where immutable() {
		Where where = new ImmutableWhere(this);
		return where;
	}
	
	@Override
	public Where mutable() {
		Where where = new Where(this);
		return where;
	}
	
	/* BEGIN Fluent API */
	
	@Override
	public Where and() {
		super.and();
		return this;
	}
	
	@Override
	public Where group(Condition condition) {
		super.group(condition);
		return this;
	}

	@Override
	public Where predicate(Predicate predicate) {
		super.predicate(predicate);
		return this;
	}

	@Override
	public Where or() {
		super.or();
		return this;
	}
	
	/* END Fluent API */
}