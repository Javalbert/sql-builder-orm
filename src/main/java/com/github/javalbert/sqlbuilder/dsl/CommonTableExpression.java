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

import java.util.Collections;
import java.util.List;
import java.util.Objects;

import com.github.javalbert.utils.collections.CollectionUtils;

public class CommonTableExpression {
	@SuppressWarnings("unchecked")
	private List<TableColumn> columns = Collections.EMPTY_LIST;
	private final Table queryName;
	private SelectStatement query;
	
	public List<TableColumn> getColumns() {
		return columns;
	}
	public SelectStatement getQuery() {
		return query;
	}
	public Table getQueryName() {
		return queryName;
	}
	
	CommonTableExpression(Table queryName) {
		this.queryName = Objects.requireNonNull(queryName, "query name cannot be null");
	}
	
	public CommonTableExpression as(SelectStatement query) {
		CommonTableExpression cte = copy();
		cte.query = Objects.requireNonNull(query, "query cannot be null");
		return cte;
	}
	
	public CommonTableExpression columns(TableColumn...columns) {
		CommonTableExpression cte = copy();
		cte.columns = CollectionUtils.immutableArrayList(columns);
		return cte;
	}
	
	public CteList with(Table queryName) {
		return new CteList().add0(this)
				.with(queryName);
	}
	
	public DeleteStatement delete(Table table) {
		return DSL.delete(table).with(this);
	}
	
	public InsertStatement insert(Table table) {
		return DSL.insert(table).with(this);
	}
	
	public SelectStatement select(SelectColumn<?>...columns) {
		return DSL.select(columns).with(this);
	}
	
	CommonTableExpression copy() {
		CommonTableExpression copy = new CommonTableExpression(queryName);
		copy.columns = columns;
		copy.query = query;
		return copy;
	}
}
