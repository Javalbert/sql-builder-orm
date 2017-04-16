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

public class Condition implements BooleanExpression {
	private BooleanExpression leftExpression;
	private LogicalOperator logicalOperator;
	private boolean grouped;
	private BooleanExpression rightExpression;
	
	public BooleanExpression getLeftExpression() {
		return leftExpression;
	}
	public LogicalOperator getLogicalOperator() {
		return logicalOperator;
	}
	public boolean isGrouped() {
		return grouped;
	}
	public BooleanExpression getRightExpression() {
		return rightExpression;
	}

	Condition(
			BooleanExpression leftExpression,
			BooleanExpression rightExpression,
			LogicalOperator logicalOperator) {
		this.leftExpression = leftExpression;
		this.logicalOperator = logicalOperator;
		this.rightExpression = rightExpression;
	}
	
	Condition copy() {
		Condition copy = new Condition(leftExpression, rightExpression, logicalOperator);
		copy.grouped = grouped;
		return copy;
	}
	
	Condition grouped() {
		Condition condition = copy();
		condition.grouped = true;
		return condition;
	}
}
