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

public class TableColumn
implements ExpressionBuilder, OrderByColumn, Predicand, SelectColumn<TableColumn>, ValueExpression {
	public static final TableColumn ALL = new TableColumn() {
		@Override
		public String getAlias() {
			return null;
		}
		@Override
		public TableColumn as(String alias) {
			return null;
		}
	};
	
	private String alias;
	private String name;
	private TableAlias tableAlias;
	
	@Override
	public String getAlias() {
		return alias;
	}
	public String getName() {
		return name;
	}
	@Override
	public String getOrderByColumnLabel() {
		return !Strings.isNullOrEmpty(alias) ? alias : name;
	}
	public TableAlias getTableAlias() {
		return tableAlias;
	}
	
	public TableColumn(String name) {
		this.name = Strings.illegalArgOnEmpty(name, "name cannot be null or empty");
	}
	
	private TableColumn() {}
	
	@Override
	public TableColumn as(String alias) {
		TableColumn column = copy();
		column.alias = alias;
		return column;
	}
	
	public SetValue to(Boolean value) {
		return to(DSL.literal(value));
	}
	public SetValue to(Number value) {
		return to(DSL.literal(value));
	}
	public SetValue to(String value) {
		return to(DSL.literal(value));
	}
	
	public SetValue to(ValueExpression value) {
		return new SetValue(this, value);
	}
	
	TableColumn copy() {
		TableColumn copy = new TableColumn();
		copy.alias = alias;
		copy.name = name;
		copy.tableAlias = tableAlias;
		return copy;
	}

	TableColumn tableAlias(TableAlias tableAlias) {
		TableColumn column = copy();
		column.tableAlias = tableAlias;
		return column;
	}
}
