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

import com.github.javalbert.utils.collections.CollectionUtils;

public class SelectStatement
implements ExpressionBuilder, Predicand, SelectColumn<SelectStatement>, ValueExpression {
	private String alias;
	@SuppressWarnings({ "rawtypes", "unchecked" })
	private List<SelectColumn> columns = Collections.EMPTY_LIST;
	private CteList cteList = CteList.EMPTY;
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
	public CteList getCteList() {
		return cteList;
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
	
	SelectStatement(SelectColumn<?>...columns) {
		this(Arrays.asList(columns));
	}
	
	SelectStatement(List<SelectColumn<?>> columns) {
		this.columns = CollectionUtils.immutableArrayList(
				CollectionUtils.illegalArgOnEmpty(columns, "columns cannot be null or empty"));
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
	
	public SelectStatement except(SelectStatement query) {
		SelectStatement stmt = copy();
		List<SetOperation> setOperations = new ArrayList<>(stmt.setOperations);
		setOperations.add(DSL.except(query));
		stmt.setOperations = Collections.unmodifiableList(setOperations);
		return stmt;
	}
	
	public SelectStatement from(TableReference...tables) {
		if (tables == null || tables.length == 0) {
			throw new IllegalArgumentException("tables cannot be null or empty");
		}
		
		SelectStatement stmt = copy();
		stmt.tables = CollectionUtils.immutableArrayList(tables);
		return stmt;
	}
	
	public SelectStatement groupBy(TableColumn...groupByColumns) {
		SelectStatement stmt = copy();
		stmt.groupByColumns = CollectionUtils.immutableArrayList(groupByColumns);
		return stmt;
	}
	
	public SelectStatement having(BooleanExpression havingCondition) {
		SelectStatement stmt = copy();
		stmt.havingCondition = havingCondition;
		return stmt;
	}
	
	public SelectStatement intersect(SelectStatement query) {
		SelectStatement stmt = copy();
		List<SetOperation> setOperations = new ArrayList<>(stmt.setOperations);
		setOperations.add(DSL.intersect(query));
		stmt.setOperations = Collections.unmodifiableList(setOperations);
		return stmt;
	}
	
	public SelectStatement orderBy(OrderByColumn...orderByColumns) {
		SelectStatement stmt = copy();
		stmt.orderByColumns = CollectionUtils.immutableArrayList(orderByColumns);
		return stmt;
	}
	
	public SelectStatement setOperations(SetOperation...setOperations) {
		SelectStatement stmt = copy();
		stmt.setOperations = CollectionUtils.immutableArrayList(setOperations);
		return stmt;
	}
	
	public SelectStatement union(SelectStatement query) {
		SelectStatement stmt = copy();
		List<SetOperation> setOperations = new ArrayList<>(stmt.setOperations);
		setOperations.add(DSL.union(query));
		stmt.setOperations = Collections.unmodifiableList(setOperations);
		return stmt;
	}
	
	public SelectStatement unionAll(SelectStatement query) {
		SelectStatement stmt = copy();
		List<SetOperation> setOperations = new ArrayList<>(stmt.setOperations);
		setOperations.add(DSL.unionAll(query));
		stmt.setOperations = Collections.unmodifiableList(setOperations);
		return stmt;
	}
	
	public SelectStatement where(BooleanExpression whereCondition) {
		SelectStatement stmt = copy();
		stmt.whereCondition = whereCondition;
		return stmt;
	}
	
	public SelectStatement with(CommonTableExpression...ctes) {
		SelectStatement stmt = copy();
		stmt.cteList = new CteList(Arrays.asList(ctes));
		return stmt;
	}
	
	public SelectStatement with(CteList cteList) {
		SelectStatement stmt = copy();
		stmt.cteList = cteList;
		return stmt;
	}
	
	SelectStatement copy() {
		SelectStatement copy = new SelectStatement();
		copy.alias = alias;
		copy.columns = columns;
		copy.cteList = cteList;
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
