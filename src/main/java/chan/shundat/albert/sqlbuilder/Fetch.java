/*******************************************************************************
 * Copyright (c) 2016 Albert Shun-Dat Chan
 *
 * Licensed under MIT License
 * See LICENSE file at the root of the project or
 * https://github.com/Javalbert/sql-builder-orm/blob/master/LICENSE
 *******************************************************************************/
package chan.shundat.albert.sqlbuilder;

public class Fetch implements Node<Fetch> {
	private int fetchCount;

	public int getFetchCount() { return fetchCount; }
	public void setFetchCount(int fetchCount) { this.fetchCount = fetchCount; }
	@Override
	public int getType() {
		return TYPE_FETCH;
	}
	
	public Fetch(Fetch fetch) {
		this(fetch.getFetchCount());
	}
	
	public Fetch(int fetchCount) {
		this.fetchCount = fetchCount;
	}
	
	@Override
	public boolean accept(NodeVisitor visitor) {
		return visitor.visit(this);
	}
	
	@Override
	public Fetch immutable() {
		Fetch fetch = new ImmutableFetch(this);
		return fetch;
	}
	
	@Override
	public Fetch mutable() {
		Fetch fetch = new Fetch(this);
		return fetch;
	}
}