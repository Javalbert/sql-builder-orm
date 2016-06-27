/*******************************************************************************
 * Copyright (c) 2016 Albert Shun-Dat Chan
 *
 * Licensed under MIT License
 * See LICENSE file at the root of the project or
 * https://github.com/Javalbert/sql-builder-orm/blob/master/LICENSE
 *******************************************************************************/
package chan.shundat.albert.sqlbuilder;

public class ImmutableSelect extends Select {
	@Override
	public void setAlias(String alias) {
		throw new UnsupportedOperationException("immutable");
	}

	public ImmutableSelect(Select select) {
		alias = select.getAlias();
		nodes = NodeUtils.immutableNodes(select);
	}
}