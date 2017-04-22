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

import java.util.List;

import com.github.javalbert.sqlbuilder.ExpressionBuilding;
import com.github.javalbert.sqlbuilder.InValues;
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
	public Select buildSelect(SelectStatement stmt) {
		return buildSelect(stmt, false);
	}
	
	public Select buildSelect(SelectStatement stmt, boolean subquery) {
		// TODO
		Select select = new Select();
		
		if (!subquery) {
			With with = buildWith(stmt);
			if (with != null) {
				select.with(with);
			}
		}
		
		select.list(buildSelectList(stmt));
		
		
		return select;
	}
	
	private com.github.javalbert.sqlbuilder.Predicate buildBetweenPredicate(
			BetweenPredicate dslPredicate) {
		com.github.javalbert.sqlbuilder.Predicate predicate = newPredicate(dslPredicate);
		
		if (dslPredicate.getOperator() == PredicateOperator.BETWEEN) {
			predicate.between();
		} else if (dslPredicate.getOperator() == PredicateOperator.NOT_BETWEEN) {
			predicate.notBetween();
		}
		
		handleExpressionBuilding(predicate, dslPredicate.getValue1());
		predicate.and();
		handleExpressionBuilding(predicate, dslPredicate.getValue2());
		
		return predicate;
	}

	private com.github.javalbert.sqlbuilder.Case buildCase(Case dslCase) {
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
				handleCondition(condition, when.getBooleanExpression());
			}
			
			handleExpressionBuilding(sqlCase.then(), when.getThen());
		}
		
		if (dslCase.getElseExpression() != null) {
			handleExpressionBuilding(sqlCase.ifElse(), dslCase.getElseExpression());
		}
		
		return sqlCase.end();
	}

	private com.github.javalbert.sqlbuilder.Predicate buildExistsPredicate(ExistsPredicate dslPredicate) {
		com.github.javalbert.sqlbuilder.Predicate predicate = newPredicate(dslPredicate);
		
		if (dslPredicate.getOperator() == PredicateOperator.EXISTS) {
			predicate.exists();
		} else if (dslPredicate.getOperator() == PredicateOperator.NOT_EXISTS) {
			predicate.notExists();
		}
		
		return predicate.subquery(buildSelect(dslPredicate.getSubquery(), true));
	}
	
	private com.github.javalbert.sqlbuilder.Function buildFunction(Function dslFunction) {
		// TODO
		return null;
	}
	
	/**
	 * Except for BETWEEN, EXISTS, and IN
	 * @param condition
	 * @param predicate
	 * @return 
	 */
	@SuppressWarnings("incomplete-switch")
	private com.github.javalbert.sqlbuilder.Predicate buildPredicate(Predicate dslPredicate) {
		com.github.javalbert.sqlbuilder.Predicate predicate = newPredicate(dslPredicate);
		
		switch (dslPredicate.getOperator()) {
			case EQ:
				predicate.eq();
				break;
			case GT:
				predicate.gt();
				break;
			case GT_EQ:
				predicate.gteq();
				break;
			case IS_NOT_NULL:
				return predicate.isNotNull();
			case IS_NULL:
				return predicate.isNull();
			case LIKE:
				predicate.like();
				break;
			case LT:
				predicate.lt();
				break;
			case LT_EQ:
				predicate.lteq();
				break;
			case NOT_EQ:
				predicate.noteq();
				break;
			case NOT_LIKE:
				predicate.notLike();
				break;
		}
		
		handleExpressionBuilding(predicate, dslPredicate.getRightPredicand());
		
		return predicate;
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
			with.as(buildSelect(cte.getQuery(), true));
		}
		return with;
	}
	
	private void handleCondition(
			com.github.javalbert.sqlbuilder.Condition condition,
			BooleanExpression booleanExpression) {
		if (booleanExpression.getNodeType() == DSLNode.TYPE_CONDITION) {
			Condition dslCondition = (Condition)booleanExpression;
			
			if (dslCondition.isGrouped()) {
				com.github.javalbert.sqlbuilder.Condition nestedCondition = new com.github.javalbert.sqlbuilder.Condition();
				condition.group(nestedCondition);
				handleConditionOperator(nestedCondition, dslCondition);
			} else {
				handleConditionOperator(condition, dslCondition);
			}
		} else if (booleanExpression.getNodeType() == DSLNode.TYPE_PREDICATE) {
			condition.predicate(buildPredicate((Predicate)booleanExpression));
		} else if (booleanExpression.getNodeType() == DSLNode.TYPE_PREDICATE_BETWEEN) {
			condition.predicate(buildBetweenPredicate((BetweenPredicate)booleanExpression));
		} else if (booleanExpression.getNodeType() == DSLNode.TYPE_PREDICATE_EXISTS) {
			condition.predicate(buildExistsPredicate((ExistsPredicate)booleanExpression));
		} else if (booleanExpression.getNodeType() == DSLNode.TYPE_PREDICATE_IN) {
			condition.predicate(buildInPredicate((InPredicate)booleanExpression));
		}
	}

	private com.github.javalbert.sqlbuilder.Predicate buildInPredicate(InPredicate dslPredicate) {
		com.github.javalbert.sqlbuilder.Predicate predicate = newPredicate(dslPredicate);
		
		if (dslPredicate.getOperator() == PredicateOperator.IN) {
			predicate.in();
		} else if (dslPredicate.getOperator() == PredicateOperator.NOT_IN) {
			predicate.notIn();
		}
		
		List<ValueExpression> valueExpressions = dslPredicate.getValues();
		if (valueExpressions.size() == 1
				&& valueExpressions.get(0).getNodeType() == DSLNode.TYPE_SELECT_STATEMENT) {
			return predicate.subquery(buildSelect((SelectStatement)valueExpressions.get(0), true));
		}
		
		InValues values = new InValues();
		for (ValueExpression valueExpression : valueExpressions) {
			handleExpressionBuilding(values, valueExpression);
		}
		return predicate.values(values);
	}

	private void handleConditionOperator(
			com.github.javalbert.sqlbuilder.Condition condition,
			Condition dslCondition) {
		handleCondition(condition, dslCondition.getLeftExpression());
		
		if (dslCondition.getLogicalOperator() == LogicalOperator.AND) {
			condition.and();
		} else if (dslCondition.getLogicalOperator() == LogicalOperator.OR) {
			condition.or();
		}

		handleCondition(condition, dslCondition.getRightExpression());
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
				building.function(buildFunction((Function)dslNode));
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

	private com.github.javalbert.sqlbuilder.Predicate newPredicate(Predicate dslPredicate) {
		com.github.javalbert.sqlbuilder.Predicate predicate = new com.github.javalbert.sqlbuilder.Predicate();
		handleExpressionBuilding(predicate, dslPredicate.getLeftPredicand());
		return predicate;
	}
}
