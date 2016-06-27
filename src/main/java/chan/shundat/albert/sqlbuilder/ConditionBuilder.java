/*******************************************************************************
 * Copyright (c) 2016 Albert Shun-Dat Chan
 *
 * Licensed under MIT License
 * See LICENSE file at the root of the project or
 * https://github.com/Javalbert/sql-builder-orm/blob/master/LICENSE
 *******************************************************************************/
package chan.shundat.albert.sqlbuilder;

import java.util.ArrayList;
import java.util.List;

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
		if (condition == null) {
			throw new NullPointerException("condition cannot be null");
		}
		nodes.add(condition);
		return (T)this;
	}

	@Override
	public T predicate(Predicate predicate) {
		if (predicate == null) {
			throw new NullPointerException("predicate cannot be null");
		}
		nodes.add(predicate);
		return (T)this;
	}

	@Override
	public T or() {
		nodes.add(LogicalOperator.OR);
		return (T)this;
	}
	
	/* END Fluent API */
}