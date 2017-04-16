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

public class MergeAction {
	public static final DeleteStatement DELETE = new DeleteStatement();
	
	private DMLStatement dmlStatement;
	private BooleanExpression searchCondition;
	private final boolean whenMatched;
	
	public DMLStatement getDmlStatement() {
		return dmlStatement;
	}
	public BooleanExpression getSearchCondition() {
		return searchCondition;
	}
	public boolean isWhenMatched() {
		return whenMatched;
	}
	
	MergeAction(boolean whenMatched) {
		this.whenMatched = whenMatched;
	}
	
	public MergeAction and(BooleanExpression searchCondition) {
		MergeAction action = copy();
		action.searchCondition = searchCondition;
		return action;
	}
	
	public MergeAction delete() {
		return copyWith(DELETE);
	}
	
	public MergeAction insert(InsertStatement statement) {
		return copyWith(statement);
	}
	
	public MergeAction update(UpdateStatement statement) {
		return copyWith(statement);
	}
	
	MergeAction copy() {
		MergeAction copy = new MergeAction(whenMatched);
		copy.dmlStatement = dmlStatement;
		copy.searchCondition = searchCondition;
		return copy;
	}
	
	private MergeAction copyWith(DMLStatement statement) {
		MergeAction action = copy();
		action.dmlStatement = statement;
		return action;
	}
}
