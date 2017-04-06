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

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

public class SelectStatement implements SelectColumn<SelectStatement> {
	private String alias;
	@SuppressWarnings("rawtypes")
	private List<SelectColumn> columns;

	@Override
	public String getAlias() {
		return alias;
	}
	@SuppressWarnings("rawtypes")
	public List<SelectColumn> getColumns() {
		return columns;
	}
	
	SelectStatement(List<SelectColumn<?>> columns) {
		if (columns == null || columns.isEmpty()) {
			throw new IllegalArgumentException("columns cannot be null or empty");
		}
		this.columns = Collections.unmodifiableList(new ArrayList<>(columns));
	}
	
	private SelectStatement() {}

	@Override
	public SelectStatement as(String alias) {
		SelectStatement stmt = copy();
		stmt.alias = alias;
		return stmt;
	}
	
	public SelectStatement from(Table table) {
		SelectStatement stmt = copy();
		
		return stmt;
	}
	
	SelectStatement copy() {
		SelectStatement copy = new SelectStatement();
		copy.alias = alias;
		copy.columns = columns;
		return copy;
	}
}
