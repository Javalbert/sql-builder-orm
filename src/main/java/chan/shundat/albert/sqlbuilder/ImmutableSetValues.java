/*******************************************************************************
 * Copyright (c) 2016 Albert Shun-Dat Chan
 *
 * Licensed under MIT License
 * See LICENSE file at the root of the project or
 * https://github.com/Javalbert/sql-builder-orm/blob/master/LICENSE
 *******************************************************************************/
package chan.shundat.albert.sqlbuilder;

import chan.shundat.albert.sqlbuilder.NodeUtils;
import chan.shundat.albert.sqlbuilder.SetValues;

public class ImmutableSetValues extends SetValues {
	public ImmutableSetValues(SetValues values) {
		nodes = NodeUtils.immutableNodes(values);
	}
}