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

import com.github.javalbert.sqlbuilder.ExpressionBuilding;
import com.github.javalbert.sqlbuilder.Select;
import com.github.javalbert.sqlbuilder.SelectList;
import com.github.javalbert.sqlbuilder.With;

/**
 * Transforms the internal DSL as defined in <code>dsl</code> package to the more
 * string-based and mutable SQL builder API defined
 * in <code>com.github.javalbert.sqlbuilder</code> package.
 * 
 * @author Albert
 *
 */
public class DSLTransformer {
	public static final int FLAG_CTE_QUERY = 0x1;
	
	public Select buildSelect(SelectStatement stmt) {
		return buildSelect(stmt, 0);
	}
	
	public Select buildSelect(SelectStatement stmt, int flags) {
		// TODO
		Select select = new Select();
		
		if ((flags & FLAG_CTE_QUERY) == 0) {
			With with = buildWith(stmt);
			if (with != null) {
				select.with(with);
			}
		}
		
		select.list(buildSelectList(stmt));
		
		
		return select;
	}

	private com.github.javalbert.sqlbuilder.Case buildCase(Case dslCase) {
		// TODO
		com.github.javalbert.sqlbuilder.Case sqlCase = new com.github.javalbert.sqlbuilder.Case();
		
		boolean simpleCase = dslCase.getSimpleCaseExpression() != null;
		if (simpleCase) {
			handleExpressionBuilding(sqlCase, dslCase.getSimpleCaseExpression());
		}
		
		for (When when : dslCase.getWhenClauses()) {
			sqlCase.when();
			
			if (simpleCase) {
				handleExpressionBuilding(sqlCase, when.getSimpleExpression());
			} else {
				com.github.javalbert.sqlbuilder.Condition condition = new com.github.javalbert.sqlbuilder.Condition();
				sqlCase.condition(condition);
				handleRootCondition(condition, when.getBooleanExpression());
			}
			
			sqlCase.then();
		}
		
		return sqlCase;
	}

	/**
	 * 
	 * @param rootCondition its called the root because in the SQL builder API,
	 * the Condition is 
	 * @param booleanExpression
	 */
	private void handleRootCondition(
			com.github.javalbert.sqlbuilder.Condition rootCondition,
			BooleanExpression booleanExpression) {
		// TODO
		if (booleanExpression.getNodeType() == DSLNode.TYPE_CONDITION) {
			Condition dslCondition = (Condition)booleanExpression;
			BooleanExpression left = dslCondition.getLeftExpression();
			
		} else if (booleanExpression.getNodeType() == DSLNode.TYPE_PREDICATE) {
			
		} else if (booleanExpression.getNodeType() == DSLNode.TYPE_PREDICATE_BETWEEN) {
			
		} else if (booleanExpression.getNodeType() == DSLNode.TYPE_PREDICATE_EXISTS) {
			
		} else if (booleanExpression.getNodeType() == DSLNode.TYPE_PREDICATE_IN) {
			
		}
	}
	
	private SelectList buildSelectList(SelectStatement stmt) {
		SelectList list = new SelectList();
		
		if (stmt.isDistinct()) {
			list.distinct();
		}
		
		for (@SuppressWarnings("rawtypes") SelectColumn selectColumn : stmt.getColumns()) {
			handleExpressionBuilding(list, selectColumn);
			list.as(selectColumn.getAlias());
		}
		
		return list;
	}

	private With buildWith(SelectStatement stmt) {
		CteList cteList = stmt.getCteList();
		if (cteList.isEmpty()) {
			return null;
		}
		
		With with = new With();
		for (CommonTableExpression cte : stmt.getCteList()) {
			with.name(cte.getQueryName().getName());
			for (TableColumn tableColumn : cte.getColumns()) {
				with.column(tableColumn.getName());
			}
			with.as(buildSelect(cte.getQuery(), FLAG_CTE_QUERY));
		}
		return with;
	}
	
	private void handleExpressionBuilding(
			@SuppressWarnings("rawtypes") ExpressionBuilding building,
			DSLNode dslNode) {
		// TODO
		switch (dslNode.getNodeType()) {
			case DSLNode.TYPE_CASE:
				building.sqlCase(buildCase((Case)dslNode));
				break;
			case DSLNode.TYPE_EXPRESSION:
				break;
			case DSLNode.TYPE_FUNCTION:
				break;
			case DSLNode.TYPE_LITERAL_BOOLEAN:
				break;
			case DSLNode.TYPE_LITERAL_NULL:
				break;
			case DSLNode.TYPE_LITERAL_NUMBER:
				break;
			case DSLNode.TYPE_LITERAL_STRING:
				break;
			case DSLNode.TYPE_PARAMETER:
				break;
			case DSLNode.TYPE_SELECT_STATEMENT:
				break;
			case DSLNode.TYPE_TABLE_COLUMN:
				break;
		}
	}
}
