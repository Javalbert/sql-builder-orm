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

import com.github.javalbert.sqlbuilder.ColumnBuilder;
import com.github.javalbert.sqlbuilder.ColumnList;
import com.github.javalbert.sqlbuilder.ColumnValues;
import com.github.javalbert.sqlbuilder.Delete;
import com.github.javalbert.sqlbuilder.ExpressionBuilding;
import com.github.javalbert.sqlbuilder.From;
import com.github.javalbert.sqlbuilder.GroupBy;
import com.github.javalbert.sqlbuilder.Having;
import com.github.javalbert.sqlbuilder.InValues;
import com.github.javalbert.sqlbuilder.Insert;
import com.github.javalbert.sqlbuilder.Merge;
import com.github.javalbert.sqlbuilder.OrderBy;
import com.github.javalbert.sqlbuilder.Select;
import com.github.javalbert.sqlbuilder.SelectList;
import com.github.javalbert.sqlbuilder.SetValues;
import com.github.javalbert.sqlbuilder.Update;
import com.github.javalbert.sqlbuilder.Where;
import com.github.javalbert.sqlbuilder.With;
import com.github.javalbert.utils.string.Strings;

/**
 * Transforms the internal DSL as defined in <code>dsl</code> package to the more
 * string-based and mutable SQL builder API defined
 * in <code>com.github.javalbert.sqlbuilder</code> package.
 * 
 * @author Albert
 *
 */
public class DSLTransformer {
	/* START Public methods */
	
	public Delete buildDelete(DeleteStatement stmt) {
		Delete delete = new Delete();
		
		With with = buildWith(stmt);
		if (with != null) {
			delete.with(with);
		}
		
		if (stmt.getTable() != null) {
			// Should only be null for MERGE statement
			delete.tableName(stmt.getTable().getName());
		}
		
		Where where = new Where();
		delete.where(where);
		handleCondition(where, stmt.getWhereCondition());
		
		return delete;
	}
	
	public Insert buildInsert(InsertStatement stmt) {
		Insert insert = new Insert();
		
		With with = buildWith(stmt);
		if (with != null) {
			insert.with(with);
		}
		
		if (stmt.getTable() != null) {
			// Should only be null for MERGE statement
			insert.into(stmt.getTable().getName());
		}
		
		if (!stmt.getColumns().isEmpty()) {
			ColumnList columns = new ColumnList();
			insert.columns(columns);
			
			for (TableColumn tableColumn : stmt.getColumns()) {
				columns.column(tableColumn.getName());
			}
		}
		
		if (!stmt.getValues().isEmpty()) {
			ColumnValues values = new ColumnValues();
			insert.values(values);
			
			for (ValueExpression value : stmt.getValues()) {
				handleExpressionBuilding(values, value);
			}
		} else if (stmt.getSubselect() != null) {
			insert.subselect(buildSelect(stmt.getSubselect()));
		}
		
		return insert;
	}
	
	public Merge buildMerge(MergeStatement stmt) {
		Merge merge = new Merge();
		
		merge.tableName(stmt.getTargetTable().getName());
		
		TableReference sourceTable = stmt.getSourceTable();
		if (sourceTable.getTableType() == TableReference.TABLE_TABLE) {
			merge.using(((Table)sourceTable).getName());
		} else if (sourceTable.getTableType() == TableReference.TABLE_INLINE_VIEW) {
			merge.using(buildSelect((SelectStatement)sourceTable));
		}
		
		com.github.javalbert.sqlbuilder.Condition searchCondition = new com.github.javalbert.sqlbuilder.Condition();
		merge.on(searchCondition);
		handleCondition(searchCondition, stmt.getSearchCondition());
		
		for (MergeAction action : stmt.getMergeActions()) {
			if (action.isWhenMatched()) {
				merge.whenMatched();
			} else {
				merge.whenNotMatched();
			}
			
			if (action.getSearchCondition() != null) {
				com.github.javalbert.sqlbuilder.Condition actionCondition = new com.github.javalbert.sqlbuilder.Condition();
				merge.and(actionCondition);
				handleCondition(actionCondition, action.getSearchCondition());
			}
			merge.then();
			
			DMLStatement dmlAction = action.getDmlStatement();
			if (dmlAction.getDmlType() == DMLStatement.DML_INSERT) {
				merge.insert(buildInsert(((InsertStatement)dmlAction)));
			} else if (dmlAction.getDmlType() == DMLStatement.DML_UPDATE) {
				merge.update(buildUpdate(((UpdateStatement)dmlAction)));
			} else if (dmlAction.getDmlType() == DMLStatement.DML_DELETE) {
				merge.delete();
			}
		}
		
		return merge;
	}
	
	public Select buildSelect(SelectStatement stmt) {
		Select select = new Select();
		
		With with = buildWith(stmt);
		if (with != null) {
			select.with(with);
		}
		
		select.list(buildSelectList(stmt));
		
		if (!stmt.getTables().isEmpty()) {
			select.from(buildFrom(stmt));
		}
		
		if (stmt.getWhereCondition() != null) {
			Where where = new Where();
			select.where(where);
			handleCondition(where, stmt.getWhereCondition());
		}
		
		if (!stmt.getGroupByColumns().isEmpty()) {
			GroupBy groupBy = new GroupBy();
			select.groupBy(groupBy);
			
			for (TableColumn column : stmt.getGroupByColumns()) {
				handleTableColumn(groupBy, column);
			}
		}
		
		if (stmt.getHavingCondition() != null) {
			Having having = new Having();
			select.having(having);
			handleCondition(having, stmt.getHavingCondition());
		}
		
		if (!stmt.getOrderByColumns().isEmpty()) {
			select.orderBy(buildOrderBy(stmt));
		}
		
		if (!stmt.getSetOperations().isEmpty()) {
			handleSetOperations(select, stmt);
		}
		
		return select;
	}
	
	public Update buildUpdate(UpdateStatement stmt) {
		Update update = new Update();
		
		With with = buildWith(stmt);
		if (with != null) {
			update.with(with);
		}
		
		if (stmt.getTable() != null) {
			// Should only be null for MERGE statement
			update.tableName(stmt.getTable().getName());
		}
		
		if (!stmt.getValues().isEmpty()) {
			SetValues values = new SetValues();
			update.set(values);
			
			for (SetValue value : stmt.getValues()) {
				com.github.javalbert.sqlbuilder.SetValue setValue = new com.github.javalbert.sqlbuilder.SetValue();
				
				setValue.column(value.getColumn().getName());
				handleExpressionBuilding(setValue, value.getValue());
				
				values.add(setValue);
			}
		}
		
		Where where = new Where();
		update.where(where);
		handleCondition(where, stmt.getWhereCondition());
		
		return update;
	}
	
	/* END Public methods */
	
	/* START Private methods */

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
		
		return predicate.subquery(buildSelect(dslPredicate.getSubquery()));
	}

	private com.github.javalbert.sqlbuilder.Expression buildExpression(Expression dslExpression) {
		com.github.javalbert.sqlbuilder.Expression expression = new com.github.javalbert.sqlbuilder.Expression();
		handleExpressionBuilding(expression, dslExpression.getLeft());
		
		switch (dslExpression.getOperator()) {
			case CONCAT: expression.concat(); break;
			case DIVIDE: expression.divide(); break;
			case MINUS: expression.minus(); break;
			case MOD: expression.mod(); break;
			case MULTIPLY: expression.multiply(); break;
			case PLUS: expression.plus(); break;
		}
		
		handleExpressionBuilding(expression, dslExpression.getRight());
		return expression;
	}
	
	private From buildFrom(SelectStatement stmt) {
		From from = new From();
		
		// Usually just one table reference, but if multiple
		// then it is of the form "FROM tableRef1, tableRef2, ... "
		for (TableReference tableReference : stmt.getTables()) {
			handleTableReference(from, tableReference);
		}
		
		return from;
	}

	private com.github.javalbert.sqlbuilder.Function buildFunction(Function dslFunction) {
		com.github.javalbert.sqlbuilder.Function function = new com.github.javalbert.sqlbuilder.Function(dslFunction.getName());
		for (ValueExpression parameter : dslFunction.getParameters()) {
			handleExpressionBuilding(function, parameter);
		}
		return function;
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
				&& valueExpressions.get(0).getNodeType() == DSLNode.NODE_SELECT_STATEMENT) {
			return predicate.subquery(buildSelect((SelectStatement)valueExpressions.get(0)));
		}
		
		InValues values = new InValues();
		for (ValueExpression valueExpression : valueExpressions) {
			handleExpressionBuilding(values, valueExpression);
		}
		return predicate.values(values);
	}

	private OrderBy buildOrderBy(SelectStatement stmt) {
		OrderBy orderBy = new OrderBy();
		
		for (OrderByColumn orderByColumn : stmt.getOrderByColumns()) {
			if (orderByColumn.getOrderByColumnType() == OrderByColumn.ORDER_TABLE_COLUMN) {
				handleTableColumn(orderBy, (TableColumn)orderByColumn);
			} else if (orderByColumn.getOrderByColumnType() == OrderByColumn.ORDER_COLUMN_ALIAS) {
				orderBy.alias(((ColumnAlias)orderByColumn).getAlias());
			}
			
			if (orderByColumn.getSortType() == SortType.ASC) {
				orderBy.asc();
			} else if (orderByColumn.getSortType() == SortType.DESC) {
				orderBy.desc();
			}
		}
		return orderBy;
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
		
		for (SelectColumn<?> selectColumn : stmt.getColumns()) {
			handleExpressionBuilding(list, selectColumn);
			list.as(selectColumn.getAlias());
		}
		
		return list;
	}

	private With buildWith(WithClause stmt) {
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
			with.as(buildSelect(cte.getQuery()));
		}
		return with;
	}
	
	private void handleCondition(
			com.github.javalbert.sqlbuilder.Condition condition,
			BooleanExpression booleanExpression) {
		if (booleanExpression.getNodeType() == DSLNode.NODE_CONDITION) {
			Condition dslCondition = (Condition)booleanExpression;
			
			if (dslCondition.isGrouped()) {
				com.github.javalbert.sqlbuilder.Condition nestedCondition = new com.github.javalbert.sqlbuilder.Condition();
				condition.group(nestedCondition);
				handleConditionOperator(nestedCondition, dslCondition);
			} else {
				handleConditionOperator(condition, dslCondition);
			}
		} else if (booleanExpression.getNodeType() == DSLNode.NODE_PREDICATE) {
			condition.predicate(buildPredicate((Predicate)booleanExpression));
		} else if (booleanExpression.getNodeType() == DSLNode.NODE_PREDICATE_BETWEEN) {
			condition.predicate(buildBetweenPredicate((BetweenPredicate)booleanExpression));
		} else if (booleanExpression.getNodeType() == DSLNode.NODE_PREDICATE_EXISTS) {
			condition.predicate(buildExistsPredicate((ExistsPredicate)booleanExpression));
		} else if (booleanExpression.getNodeType() == DSLNode.NODE_PREDICATE_IN) {
			condition.predicate(buildInPredicate((InPredicate)booleanExpression));
		}
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
			ExpressionBuilding<?> building,
			DSLNode dslNode) {
		switch (dslNode.getNodeType()) {
			case DSLNode.NODE_CASE:
				building.sqlCase(buildCase((Case)dslNode));
				break;
			case DSLNode.NODE_EXPRESSION:
				building.expression(buildExpression((Expression)dslNode));
				break;
			case DSLNode.NODE_FUNCTION:
				building.function(buildFunction((Function)dslNode));
				break;
			case DSLNode.NODE_LITERAL_BOOLEAN:
				building.literal(((LiteralBoolean)dslNode).getValue());
				break;
			case DSLNode.NODE_LITERAL_NULL:
				building.literalNull();
				break;
			case DSLNode.NODE_LITERAL_NUMBER:
				building.literal(((LiteralNumber)dslNode).getValue());
				break;
			case DSLNode.NODE_LITERAL_STRING:
				building.literal(((LiteralString)dslNode).getValue());
				break;
			case DSLNode.NODE_PARAMETER:
				building.param(((Parameter)dslNode).getName());
				break;
			case DSLNode.NODE_SELECT_STATEMENT:
				building.subquery(buildSelect((SelectStatement)dslNode));
				break;
			case DSLNode.NODE_TABLE_COLUMN:
				handleTableColumn(building, (TableColumn)dslNode);
				break;
		}
	}
	
	private void handleJoinedTable(From from, JoinedTable joinedTable) {
		if (joinedTable.isNestedJoin()) {
			from.leftParens();
		}
		
		handleTableReference(from, joinedTable.getLeftTable());
		
		switch (joinedTable.getJoinType()) {
			case FULL: from.fullOuterJoin(); break;
			case INNER: from.innerJoin(); break;
			case LEFT: from.leftOuterJoin(); break;
			case RIGHT: from.rightOuterJoin(); break;
		}
		
		handleTableReference(from, joinedTable.getRightTable());
		
		com.github.javalbert.sqlbuilder.Condition condition = new com.github.javalbert.sqlbuilder.Condition();
		from.on(condition);
		handleCondition(condition, joinedTable.getJoinCondition());
		
		if (joinedTable.isNestedJoin()) {
			from.rightParens();
		}
	}

	private void handleSetOperations(
			Select select,
			SelectStatement stmt) {
		for (SetOperation setOperation : stmt.getSetOperations()) {
			if (setOperation.getOperator() == SetOperator.EXCEPT) {
				select.except();
			} else if (setOperation.getOperator() == SetOperator.INTERSECT) {
				select.intersect();
			} else if (setOperation.getOperator() == SetOperator.UNION) {
				select.union();
			} else if (setOperation.getOperator() == SetOperator.UNION_ALL) {
				select.unionAll();
			}
			select.query(buildSelect(setOperation.getQuery()));
		}
	}
	
	private void handleTableColumn(
			ColumnBuilder<?> building,
			TableColumn tableColumn) {
		if (tableColumn.getTableAlias() != null) {
			building.tableAlias(tableColumn.getTableAlias().getAlias());
		} else if (!Strings.isNullOrEmpty(tableColumn.getTableName())) {
			building.tableName(tableColumn.getTableName());
		}
		
		building.column(tableColumn.getName());
	}

	private void handleTableReference(From from, TableReference tableReference) {
		if (tableReference.getTableType() == TableReference.TABLE_TABLE) {
			Table table = (Table)tableReference;
			from.tableName(table.getName());
			
			if (table.getTableAlias() != null) {
				from.as(table.getTableAlias().getAlias());
			}
		} else if (tableReference.getTableType() == TableReference.TABLE_JOINED_TABLE) {
			handleJoinedTable(from, (JoinedTable)tableReference);
		} else if (tableReference.getTableType() == TableReference.TABLE_INLINE_VIEW) {
			SelectStatement inlineView = (SelectStatement)tableReference;
			from.inlineView(buildSelect(inlineView));
			
			if (inlineView.getTableAlias() != null) {
				from.as(inlineView.getTableAlias().getAlias());
			}
		}
	}
	
	private com.github.javalbert.sqlbuilder.Predicate newPredicate(Predicate dslPredicate) {
		com.github.javalbert.sqlbuilder.Predicate predicate = new com.github.javalbert.sqlbuilder.Predicate();
		handleExpressionBuilding(predicate, dslPredicate.getLeftPredicand());
		return predicate;
	}
	
	/* END Private methods */
}
