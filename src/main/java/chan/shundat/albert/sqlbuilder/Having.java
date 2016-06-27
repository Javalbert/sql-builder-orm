/*******************************************************************************
 * Copyright (c) 2016 Albert Shun-Dat Chan
 *
 * Licensed under MIT License
 * See LICENSE file at the root of the project or
 * https://github.com/Javalbert/sql-builder-orm/blob/master/LICENSE
 *******************************************************************************/
package chan.shundat.albert.sqlbuilder;

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
		Having having = new ImmutableHaving(this);
		return having;
	}
	
	@Override
	public Having mutable() {
		Having having = new Having(this);
		return having;
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