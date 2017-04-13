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

public class DSL {
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
	
	public static Condition group(Condition condition) {
		return condition.grouped();
	}
	
	@SuppressWarnings("rawtypes")
	public static Literal literal(Boolean value) {
		return value != null ? Boolean.TRUE.equals(value)
				? LiteralBoolean.TRUE : LiteralBoolean.FALSE : literalNull();
	}
	
	@SuppressWarnings("rawtypes")
	public static Literal literal(Number value) {
		return new LiteralNumber(value);
	}
	
	@SuppressWarnings("rawtypes")
	public static Literal literal(String value) {
		return value != null ? new LiteralString(value) : literalNull();
	}
	
	public static LiteralNull literalNull() {
		return LiteralNull.INSTANCE;
	}
	
	public static Function max(ValueExpression parameter) {
		return Functions.MAX.call(parameter);
	}
	
	public static Function min(ValueExpression parameter) {
		return Functions.MIN.call(parameter);
	}
	
	public static Parameter param(String name) {
		return new Parameter(name);
	}
	
	/**
	 * {@code SELECT *}
	 * @return
	 */
	public static SelectStatement select() {
		return select(SelectColumn.ALL);
	}
	
	public static SelectStatement select(@SuppressWarnings("rawtypes") SelectColumn...columns) {
		return new SelectStatement(Arrays.asList(columns));
	}
	
	public static Function sum(ValueExpression parameter) {
		return Functions.SUM.call(parameter);
	}
}
