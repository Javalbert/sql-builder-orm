/*******************************************************************************
 * Copyright (c) 2016 Albert Shun-Dat Chan
 *
 * Licensed under MIT License
 * See LICENSE file at the root of the project or
 * https://github.com/Javalbert/sql-builder-orm/blob/master/LICENSE
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