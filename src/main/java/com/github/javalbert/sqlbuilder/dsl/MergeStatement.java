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
import java.util.Collections;
import java.util.List;
import java.util.Objects;

import com.github.javalbert.utils.collections.CollectionUtils;

public class MergeStatement implements DMLStatement {
	@SuppressWarnings("unchecked")
	private List<MergeAction> mergeActions = Collections.EMPTY_LIST;
	private BooleanExpression searchCondition;
	private TableReference sourceTable;
	private final Table targetTable;
	
	public List<MergeAction> getMergeActions() {
		return mergeActions;
	}
	public BooleanExpression getSearchCondition() {
		return searchCondition;
	}
	public TableReference getSourceTable() {
		return sourceTable;
	}
	public Table getTargetTable() {
		return targetTable;
	}
	
	MergeStatement(Table targetTable) {
		this.targetTable = Objects.requireNonNull(targetTable, "target table cannot be null");
	}
	
	public MergeStatement on(BooleanExpression searchCondition) {
		MergeStatement stmt = copy();
		stmt.searchCondition = searchCondition;
		return stmt;
	}
	
	public MergeStatement then(InsertStatement statement) {
		return newMergeAction(lastAction().insert(statement));
	}
	
	public MergeStatement then(UpdateStatement statement) {
		return newMergeAction(lastAction().update(statement));
	}
	
	public MergeStatement thenDelete() {
		return newMergeAction(lastAction().delete());
	}
	
	public MergeStatement using(SelectStatement sourceTable) {
		MergeStatement stmt = copy();
		stmt.sourceTable = sourceTable;
		return stmt;
	}
	
	public MergeStatement using(Table sourceTable) {
		MergeStatement stmt = copy();
		stmt.sourceTable = sourceTable;
		return stmt;
	}
	
	public MergeStatement whenMatched() {
		return whenMatchedAnd(null);
	}
	
	public MergeStatement whenMatchedAnd(BooleanExpression searchCondition) {
		return newMergeAction(true, searchCondition);
	}
	
	public MergeStatement whenNotMatched() {
		return whenNotMatchedAnd(null);
	}
	
	public MergeStatement whenNotMatchedAnd(BooleanExpression searchCondition) {
		return newMergeAction(false, searchCondition);
	}
	
	MergeStatement copy() {
		MergeStatement copy = new MergeStatement(targetTable);
		copy.mergeActions = mergeActions;
		copy.searchCondition = searchCondition;
		copy.sourceTable = sourceTable;
		return copy;
	}
	
	private MergeAction lastAction() {
		return CollectionUtils.lastOrIllegalState(
				mergeActions,
				"call any of the WHEN [NOT] MATCHED methods first");
	}
	
	private MergeStatement newMergeAction(boolean whenMatched, BooleanExpression searchCondition) {
		return newMergeAction(new MergeAction(whenMatched).and(searchCondition));
	}
	
	private MergeStatement newMergeAction(MergeAction mergeAction) {
		MergeStatement stmt = copy();
		List<MergeAction> mergeActions = new ArrayList<>(stmt.mergeActions);
		mergeActions.add(mergeAction);
		stmt.mergeActions = CollectionUtils.immutableArrayList(mergeActions);
		return stmt;
	}
}
