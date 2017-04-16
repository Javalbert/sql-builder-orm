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
import java.util.List;

import com.github.javalbert.utils.collections.CollectionUtils;

public class UpdateStatement implements DMLStatement {
	private CteList cteList = CteList.EMPTY;
	private final Table table;
	private List<SetValue> values;
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
	
	UpdateStatement(Table table) {
		this.table = table;
	}
	
	public UpdateStatement set(SetValue...values) {
		UpdateStatement stmt = copy();
		stmt.values = CollectionUtils.immutableArrayList(values);
		return stmt;
	}
	
	public UpdateStatement where(BooleanExpression whereCondition) {
		UpdateStatement stmt = copy();
		stmt.whereCondition = whereCondition;
		return stmt;
	}
	
	public UpdateStatement with(CommonTableExpression...ctes) {
		UpdateStatement stmt = copy();
		stmt.cteList = new CteList(Arrays.asList(ctes));
		return stmt;
	}
	
	public UpdateStatement with(CteList cteList) {
		UpdateStatement stmt = copy();
		stmt.cteList = cteList;
		return stmt;
	}
	
	UpdateStatement copy() {
		UpdateStatement copy = new UpdateStatement(table);
		copy.cteList = cteList;
		copy.values = values;
		copy.whereCondition = whereCondition;
		return copy;
	}
}
