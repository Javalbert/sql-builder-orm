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

import java.util.Objects;

public class DSL {
	/**
	 * Only useful for specifying alias for {@code TableColumn} objects
	 * and ORDER BY clause
	 * @param alias
	 * @return
	 */
	public static ColumnAlias alias(String alias) {
		return new ColumnAlias(alias);
	}
	
	public static Function avg(ValueExpression parameter) {
		return Functions.AVG.call(parameter);
	}
	
	public static Case sqlCase() {
		return sqlCase(null);
	}
	
	public static Case sqlCase(ValueExpression simpleExpression) {
		return new Case(simpleExpression);
	}
	
	public static Function count(ValueExpression parameter) {
		return Functions.COUNT.call(parameter);
	}
	
	/**
	 * DEFAULT keyword for INSERT statements
	 * <br>e.g. {@code INSERT ... VALUES (..., DEFAULT, ...)}
	 * @return {@code InsertStatement.DEFAULT}
	 */
	public static ValueExpression sqlDefault() {
		return InsertStatement.DEFAULT;
	}
	
	public static DeleteStatement delete(Table table) {
		return new DeleteStatement(table);
	}
	
	public static SetOperation except(SelectStatement query) {
		return new SetOperation(query, SetOperator.EXCEPT);
	}
	
	public static ExistsPredicate exists(SelectStatement subquery) {
		return new ExistsPredicate(subquery);
	}
	
	public static Condition group(Condition condition) {
		return condition.grouped();
	}

	/**
	 * Should only be used for MERGE statements
	 */
	public static InsertStatement insert() {
		return new InsertStatement(null);
	}
	
	public static InsertStatement insert(Table table) {
		return new InsertStatement(Objects.requireNonNull(table, "table cannot be null"));
	}
	
	public static SetOperation intersect(SelectStatement query) {
		return new SetOperation(query, SetOperator.INTERSECT);
	}
	
	@SuppressWarnings("rawtypes")
	public static Literal literal(Boolean value) {
		return value != null ? Boolean.TRUE.equals(value)
				? LiteralBoolean.TRUE : LiteralBoolean.FALSE : literalNull();
	}
	
	@SuppressWarnings("rawtypes")
	public static Literal literal(Number value) {
		return value != null ? new LiteralNumber(value) : literalNull();
	}
	
	@SuppressWarnings("rawtypes")
	public static Literal literal(String value) {
		return value != null ? new LiteralString(value) : literalNull();
	}
	
	public static LiteralNull literalNull() {
		return LiteralNull.INSTANCE;
	}
	
	public static MergeStatement merge(Table targetTable) {
		return new MergeStatement(targetTable);
	}
	
	public static Function max(ValueExpression parameter) {
		return Functions.MAX.call(parameter);
	}
	
	public static Function min(ValueExpression parameter) {
		return Functions.MIN.call(parameter);
	}
	
	public static JoinedTable nest(JoinedTable joinedTable) {
		return joinedTable.nested();
	}
	
	public static ExistsPredicate notExists(SelectStatement subquery) {
		return new ExistsPredicate(subquery, true);
	}
	
	public static Parameter param(String name) {
		return new Parameter(name);
	}
	
	/**
	 * Shorthand for SQL {@code SELECT *} or Java {@code DSL.select(DSL.star())}
	 * <br>
	 * <br>
	 * Useful for EXISTS predicate
	 * <br>
	 * For source table in MERGE statements, use {@code select(myTableAlias.of(star()))}
	 * @return
	 */
	public static SelectStatement select() {
		return select(star());
	}
	
	public static SelectStatement select(SelectColumn<?>...columns) {
		return new SelectStatement(columns);
	}
	
	/**
	 * 
	 * @return {@code TableColumn.ALL}
	 */
	public static TableColumn star() {
		return TableColumn.ALL;
	}
	
	public static Function sum(ValueExpression parameter) {
		return Functions.SUM.call(parameter);
	}
	
	public static SetOperation union(SelectStatement query) {
		return new SetOperation(query, SetOperator.UNION);
	}
	
	public static SetOperation unionAll(SelectStatement query) {
		return new SetOperation(query, SetOperator.UNION_ALL);
	}

	/**
	 * Should only be used for MERGE statements
	 */
	public static UpdateStatement update() {
		return new UpdateStatement(null);
	}
	
	public static UpdateStatement update(Table table) {
		return new UpdateStatement(Objects.requireNonNull(table, "table cannot be null"));
	}
	
	public static CommonTableExpression with(Table queryName) {
		return new CommonTableExpression(queryName);
	}
}
