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

@SuppressWarnings({ "rawtypes", "unchecked" })
public abstract class ConditionBuilder<T> implements ConditionBuilding<T>, NodeHolder {
	protected List<Node> nodes = new ArrayList<>();
	
	@Override
	public List<Node> getNodes() { return nodes; }
	
	/* BEGIN Fluent API */
	
	@Override
	public T and() {
		nodes.add(LogicalOperator.AND);
		return (T)this;
	}
	
	@Override
	public T group(Condition condition) {
		nodes.add(Objects.requireNonNull(condition, "condition cannot be null"));
		return (T)this;
	}

	@Override
	public T predicate(Predicate predicate) {
		nodes.add(Objects.requireNonNull(predicate, "predicate cannot be null"));
		return (T)this;
	}

	@Override
	public T or() {
		nodes.add(LogicalOperator.OR);
		return (T)this;
	}
	
	/* END Fluent API */
}