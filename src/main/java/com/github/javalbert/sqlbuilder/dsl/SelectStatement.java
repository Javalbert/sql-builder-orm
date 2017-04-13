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
import java.util.Arrays;
import java.util.Collections;
import java.util.List;

public class SelectStatement
implements ExpressionBuilder, Predicand, SelectColumn<SelectStatement>, ValueExpression {
	private String alias;
	@SuppressWarnings({ "rawtypes", "unchecked" })
	private List<SelectColumn> columns = Collections.EMPTY_LIST;
	private boolean distinct;
	@SuppressWarnings("unchecked")
	private List<TableColumn> groupByColumns = Collections.EMPTY_LIST;
	private BooleanExpression havingCondition;
	@SuppressWarnings("unchecked")
	private List<OrderByColumn> orderByColumns = Collections.EMPTY_LIST;
	@SuppressWarnings("unchecked")
	private List<SetOperation> setOperations = Collections.EMPTY_LIST;
	@SuppressWarnings("unchecked")
	private List<TableReference> tables = Collections.EMPTY_LIST;
	private BooleanExpression whereCondition;

	@Override
	public String getAlias() {
		return alias;
	}
	@SuppressWarnings("rawtypes")
	public List<SelectColumn> getColumns() {
		return columns;
	}
	public boolean isDistinct() {
		return distinct;
	}
	public List<TableColumn> getGroupByColumns() {
		return groupByColumns;
	}
	public BooleanExpression getHavingCondition() {
		return havingCondition;
	}
	public List<OrderByColumn> getOrderByColumns() {
		return orderByColumns;
	}
	public List<TableReference> getTables() {
		return tables;
	}
	public BooleanExpression getWhereCondition() {
		return whereCondition;
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

	public SelectStatement distinct(boolean distinct) {
		SelectStatement stmt = copy();
		stmt.distinct = distinct;
		return stmt;
	}
	
	public SelectStatement from(TableReference...tables) {
		if (tables == null || tables.length == 0) {
			throw new IllegalArgumentException("tables cannot be null or empty");
		}
		
		SelectStatement stmt = copy();
		stmt.tables = Collections.unmodifiableList(Arrays.asList(tables));
		return stmt;
	}
	
	public SelectStatement groupBy(TableColumn...groupByColumns) {
		SelectStatement stmt = copy();
		stmt.groupByColumns = Collections.unmodifiableList(Arrays.asList(groupByColumns));
		return stmt;
	}
	
	public SelectStatement having(BooleanExpression havingCondition) {
		SelectStatement stmt = copy();
		stmt.havingCondition = havingCondition;
		return stmt;
	}
	
	public SelectStatement orderBy(OrderByColumn...orderByColumns) {
		SelectStatement stmt = copy();
		stmt.orderByColumns = Collections.unmodifiableList(Arrays.asList(orderByColumns));
		return stmt;
	}
	
	public SelectStatement setOperations(SetOperation...setOperations) {
		SelectStatement stmt = copy();
		stmt.setOperations = Collections.unmodifiableList(Arrays.asList(setOperations));
		return stmt;
	}
	
	public SelectStatement where(BooleanExpression whereCondition) {
		SelectStatement stmt = copy();
		stmt.whereCondition = whereCondition;
		return stmt;
	}
	
	SelectStatement copy() {
		SelectStatement copy = new SelectStatement();
		copy.alias = alias;
		copy.columns = columns;
		copy.distinct = distinct;
		copy.groupByColumns = groupByColumns;
		copy.havingCondition = havingCondition;
		copy.orderByColumns = orderByColumns;
		copy.setOperations = setOperations;
		copy.tables = tables;
		copy.whereCondition = whereCondition;
		return copy;
	}
}
