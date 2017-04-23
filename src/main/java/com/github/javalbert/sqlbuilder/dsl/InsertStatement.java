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

import java.util.Arrays;
import java.util.Collections;
import java.util.List;

import com.github.javalbert.utils.collections.CollectionUtils;

public class InsertStatement implements DMLStatement, WithClause {
	/**
	 * See <a href="https://en.wikipedia.org/wiki/Insert_(SQL)#Default_Values">Default Values</a>.
	 * <br>Used in the VALUES clause
	 */
	public static final ValueExpression DEFAULT = new ValueExpression() {
		@Override
		public int getNodeType() {
			return TYPE_INSERT_DEFAULT;
		};
	};
	
	private List<TableColumn> columns = Collections.emptyList();
	private CteList cteList = CteList.EMPTY;
	private SelectStatement subselect;
	private final Table table;
	private List<ValueExpression> values;
	
	public List<TableColumn> getColumns() {
		return columns;
	}
	@Override
	public CteList getCteList() {
		return cteList;
	}
	public SelectStatement getSubselect() {
		return subselect;
	}
	public Table getTable() {
		return table;
	}
	public List<ValueExpression> getValues() {
		return values;
	}
	
	InsertStatement(Table table) {
		this.table = table;
	}
	
	public InsertStatement columns(TableColumn...columns) {
		InsertStatement stmt = copy();
		stmt.columns = CollectionUtils.immutableArrayList(columns);
		return stmt;
	}
	
	public InsertStatement subselect(SelectStatement subselect) {
		InsertStatement stmt = copy();
		stmt.subselect = subselect;
		return stmt;
	}
	
	public InsertStatement values(ValueExpression...values) {
		InsertStatement stmt = copy();
		stmt.values = CollectionUtils.immutableArrayList(values);
		return stmt;
	}
	
	public InsertStatement with(CommonTableExpression...ctes) {
		InsertStatement stmt = copy();
		stmt.cteList = new CteList(Arrays.asList(ctes));
		return stmt;
	}
	
	public InsertStatement with(CteList cteList) {
		InsertStatement stmt = copy();
		stmt.cteList = cteList;
		return stmt;
	}
	
	InsertStatement copy() {
		InsertStatement copy = new InsertStatement(table);
		copy.columns = columns;
		copy.cteList = cteList;
		copy.subselect = subselect;
		copy.values = values;
		return copy;
	}
}
