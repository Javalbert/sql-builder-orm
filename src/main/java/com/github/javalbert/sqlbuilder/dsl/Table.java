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

public class Table implements TableReference {
	private String name;
	private TableAlias tableAlias;

	public String getName() {
		return name;
	}
	public TableAlias getTableAlias() {
		return tableAlias;
	}
	
	public Table(String name) {
		this.name = Strings.illegalArgOnEmpty(name, "name cannot be null or empty");
	}
	
	private Table() {}

	public Table as(TableAlias tableAlias) {
		Table table = copy();
		table.tableAlias = tableAlias;
		return table;
	}
	
	Table copy() {
		Table copy = new Table();
		copy.name = name;
		copy.tableAlias = tableAlias;
		return copy;
	}
}
