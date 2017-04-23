/*******************************************************************************
 * Copyright 2017 Albert Shun-Dat Chan
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
package com.github.javalbert.sqlbuilder.dsl;

import com.github.javalbert.utils.string.Strings;

public class ColumnAlias implements OrderByColumn {
	private final String alias;
	private SortType sortType = SortType.ASC;

	public String getAlias() {
		return alias;
	}
	@Override
	public int getOrderByColumnType() {
		return ORDER_COLUMN_ALIAS;
	}
	@Override
	public SortType getSortType() {
		return sortType;
	}
	
	public ColumnAlias(String alias) {
		this.alias = Strings.illegalArgOnEmpty(alias, "alias cannot be null or empty");
	}
	
	@Override
	public OrderByColumn asc() {
		ColumnAlias columnAlias = copy();
		columnAlias.sortType = SortType.ASC;
		return columnAlias;
	}
	
	@Override
	public OrderByColumn desc() {
		ColumnAlias columnAlias = copy();
		columnAlias.sortType = SortType.DESC;
		return columnAlias;
	}
	
	ColumnAlias copy() {
		ColumnAlias copy = new ColumnAlias(alias);
		copy.sortType = sortType;
		return copy;
	}
}
