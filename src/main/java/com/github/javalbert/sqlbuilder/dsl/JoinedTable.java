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

public class JoinedTable implements TableReference {
	private BooleanExpression joinCondition;
	private final JoinType joinType;
	private final TableReference leftTable;
	private final TableReference rightTable;
	
	public BooleanExpression getJoinCondition() {
		return joinCondition;
	}
	public JoinType getJoinType() {
		return joinType;
	}
	public TableReference getLeftTable() {
		return leftTable;
	}
	public TableReference getRightTable() {
		return rightTable;
	}
	
	JoinedTable(TableReference leftTable, TableReference rightTable, JoinType joinType) {
		this.joinType = Objects.requireNonNull(joinType, "joinType cannot be null");
		this.leftTable = Objects.requireNonNull(leftTable, "leftTable cannot be null");
		this.rightTable = Objects.requireNonNull(rightTable, "rightTable cannot be null");
	}
	
	public JoinedTable on(BooleanExpression joinCondition) {
		JoinedTable joinedTable = copy();
		joinedTable.joinCondition = joinCondition;
		return joinedTable;
	}
	
	JoinedTable copy() {
		JoinedTable copy = new JoinedTable(leftTable, rightTable, joinType);
		copy.joinCondition = joinCondition;
		return copy;
	}
}
