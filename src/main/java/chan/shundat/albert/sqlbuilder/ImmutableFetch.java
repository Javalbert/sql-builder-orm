/*******************************************************************************
 * Copyright (c) 2016 Albert Shun-Dat Chan
 *
 * Licensed under MIT License
 * See LICENSE file at the root of the project or
 * https://github.com/Javalbert/sql-builder-orm/blob/master/LICENSE
 *******************************************************************************/
package chan.shundat.albert.sqlbuilder;

public class ImmutableFetch extends Fetch {
	@Override
	public void setFetchCount(int fetchCount) {
		throw new UnsupportedOperationException("immutable");
	}
	
	public ImmutableFetch(Fetch fetch) {
		super(fetch.getFetchCount());
	}
}