/*******************************************************************************
 * Copyright (c) 2016 Albert Shun-Dat Chan
 *
 * Licensed under MIT License
 * See LICENSE file at the root of the project or
 * https://github.com/Javalbert/sql-builder-orm/blob/master/LICENSE
 *******************************************************************************/
package chan.shundat.albert.sqlbuilder;

public class SortType extends Token {
	public static final SortType ASC = new SortType(Keywords.ASC);
	public static final SortType DESC = new SortType(Keywords.DESC);
	
	@Override
	public void setToken(String token) {
		throw new UnsupportedOperationException("this is a flyweight object");
	}
	@Override
	public int getType() {
		return TYPE_ORDER_BY_SORT;
	}
	
	private SortType(String keyword) {
		super(keyword, false);
	}
	
	@Override
	public SortType immutable() {
		return this;
	}
	
	@Override
	public SortType mutable() {
		return this;
	}
}