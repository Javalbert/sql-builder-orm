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
import com.github.javalbert.utils.string.Strings;

public class CommonTableExpression {
	@SuppressWarnings("unchecked")
	private List<String> columns = Collections.EMPTY_LIST;
	private final String queryName;
	private SelectStatement query;
	
	public List<String> getColumns() {
		return columns;
	}
	public SelectStatement getQuery() {
		return query;
	}
	public String getQueryName() {
		return queryName;
	}
	
	public CommonTableExpression(String queryName) {
		this.queryName = Strings.illegalArgOnEmpty(queryName, "query name cannot be null or empty");
	}
	
	public CommonTableExpression as(SelectStatement query) {
		CommonTableExpression cte = copy();
		cte.query = Objects.requireNonNull(query, "query cannot be null");
		return cte;
	}
	
	public CommonTableExpression columns(String...columns) {
		CommonTableExpression cte = copy();
		cte.columns = CollectionUtils.immutableArrayList(columns);
		return cte;
	}
	
	public CteList with(String queryName) {
		return new CteList().add0(this)
				.with(queryName);
	}
	
	CommonTableExpression copy() {
		CommonTableExpression copy = new CommonTableExpression(queryName);
		copy.columns = columns;
		copy.query = query;
		return copy;
	}
}
