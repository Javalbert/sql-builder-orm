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
import java.util.Objects;

public class DeleteStatement implements DMLStatement {
	private CteList cteList = CteList.EMPTY;
	private final Table table;
	private BooleanExpression whereCondition;
	
	public CteList getCteList() {
		return cteList;
	}
	public Table getTable() {
		return table;
	}
	public BooleanExpression getWhereCondition() {
		return whereCondition;
	}
	
	/**
	 * Singleton defined in and used by {@code MergeAction} class
	 */
	DeleteStatement() {
		table = null;
	}
	
	DeleteStatement(Table table) {
		this.table = Objects.requireNonNull(table, "table cannot be null");
	}
	
	public DeleteStatement where(BooleanExpression whereCondition) {
		DeleteStatement stmt = copy();
		stmt.whereCondition = whereCondition;
		return stmt;
	}
	
	public DeleteStatement with(CommonTableExpression...ctes) {
		DeleteStatement stmt = copy();
		stmt.cteList = new CteList(Arrays.asList(ctes));
		return stmt;
	}
	
	public DeleteStatement with(CteList cteList) {
		DeleteStatement stmt = copy();
		stmt.cteList = cteList;
		return stmt;
	}
	
	DeleteStatement copy() {
		DeleteStatement copy = new DeleteStatement(table);
		copy.cteList = cteList;
		copy.whereCondition = whereCondition;
		return copy;
	}
}
