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
}
