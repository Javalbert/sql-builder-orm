/*******************************************************************************
 * Copyright 2016 Albert Shun-Dat Chan
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
package com.github.javalbert.sqlbuilder.parser;

import java.util.ArrayList;
import java.util.List;
import java.util.Set;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import com.github.javalbert.sqlbuilder.AggregateFunction;
import com.github.javalbert.sqlbuilder.Aliasable;
import com.github.javalbert.sqlbuilder.ArithmeticOperator;
import com.github.javalbert.sqlbuilder.BinaryOperator;
import com.github.javalbert.sqlbuilder.Case;
import com.github.javalbert.sqlbuilder.CastFunction;
import com.github.javalbert.sqlbuilder.ColumnBuilder;
import com.github.javalbert.sqlbuilder.ColumnList;
import com.github.javalbert.sqlbuilder.ColumnValues;
import com.github.javalbert.sqlbuilder.Condition;
import com.github.javalbert.sqlbuilder.Delete;
import com.github.javalbert.sqlbuilder.Expression;
import com.github.javalbert.sqlbuilder.ExpressionBuilding;
import com.github.javalbert.sqlbuilder.From;
import com.github.javalbert.sqlbuilder.Function;
import com.github.javalbert.sqlbuilder.GroupBy;
import com.github.javalbert.sqlbuilder.Having;
import com.github.javalbert.sqlbuilder.InValues;
import com.github.javalbert.sqlbuilder.Insert;
import com.github.javalbert.sqlbuilder.Keywords;
import com.github.javalbert.sqlbuilder.Merge;
import com.github.javalbert.sqlbuilder.Node;
import com.github.javalbert.sqlbuilder.NodeHolder;
import com.github.javalbert.sqlbuilder.OrderBy;
import com.github.javalbert.sqlbuilder.Predicate;
import com.github.javalbert.sqlbuilder.RelationalOperator;
import com.github.javalbert.sqlbuilder.Select;
import com.github.javalbert.sqlbuilder.SelectList;
import com.github.javalbert.sqlbuilder.SetValue;
import com.github.javalbert.sqlbuilder.SetValues;
import com.github.javalbert.sqlbuilder.SqlStatement;
import com.github.javalbert.sqlbuilder.TableNameSpecifier;
import com.github.javalbert.sqlbuilder.Update;
import com.github.javalbert.sqlbuilder.Where;
import com.github.javalbert.sqlbuilder.With;
import com.github.javalbert.utils.collections.CollectionUtils;

public class SqlParser {
	public static final String REGEX_SPLIT = "\\s|%|'|\\(|\\)|\\*|\\+|,|-|\\.|\\/|<=|<>|<|=|>=|>";

	private static final Pattern PATTERN = Pattern.compile(REGEX_SPLIT);

	private static final int PARENTHESES_MODE_INSERT_COLUMNS = 1;
	private static final int PARENTHESES_MODE_INSERT_VALUES = 2;
	
	private static final Set<String> TOKEN_SET_AFTER_CASE = CollectionUtils.immutableHashSet(
			Keywords.WHEN
			);
	private static final Set<String> TOKEN_SET_AFTER_SET_OPERATION_QUERY = CollectionUtils.immutableHashSet(
			Keywords.EXCEPT,
			Keywords.INTERSECT,
			Keywords.ORDER_BY,
			Keywords.UNION,
			Keywords.UNION_ALL
			);
	private static final Set<String> TOKEN_SET_AFTER_THEN = CollectionUtils.immutableHashSet(
			Keywords.ELSE,
			Keywords.END,
			Keywords.WHEN
			);
	private static final Set<String> TOKEN_SET_AFTER_WHEN = CollectionUtils.immutableHashSet(
			Keywords.THEN
			);
	private static final Set<String> TOKEN_SET_EXPRESSION_END = CollectionUtils.immutableHashSet(
			",",
			Keywords.AND,
			Keywords.AS,
			Keywords.BETWEEN,
			Keywords.CASE,
			Keywords.ELSE,
			Keywords.END,
			RelationalOperator.EQ,
			Keywords.EXISTS,
			Keywords.FROM,
			Keywords.GROUP_BY,
			RelationalOperator.GT,
			RelationalOperator.GT_EQ,
			Keywords.HAVING,
			Keywords.IN,
			Keywords.IS_NOT_NULL,
			Keywords.IS_NULL,
			Keywords.LIKE,
			RelationalOperator.LT,
			RelationalOperator.LT_EQ,
			Keywords.NOT_BETWEEN,
			RelationalOperator.NOT_EQ,
			Keywords.NOT_EXISTS,
			Keywords.NOT_IN,
			Keywords.NOT_LIKE,
			Keywords.OR,
			Keywords.ORDER_BY,
			Keywords.THEN,
			Keywords.WHEN,
			Keywords.WHERE
			);
	private static final Set<String> TOKEN_SET_JOINS = CollectionUtils.immutableHashSet(
			Keywords.FULL_OUTER_JOIN,
			Keywords.INNER_JOIN,
			Keywords.LEFT_OUTER_JOIN,
			Keywords.ON,
			Keywords.RIGHT_OUTER_JOIN
			);
	private static final Set<String> TOKEN_SET_SET_VALUE_TERMINATOR = CollectionUtils.immutableHashSet(
			",",
			Keywords.WHERE
			);
	
	/* BEGIN Static methods */
	
	private static int addConditionOrPredicate(Case sqlCase, List<ParseToken> nodes, final int start, Set<String> endTokens) {
		final int conditionIndex = getConditionIndex(nodes, start, endTokens);
		final int predicateIndex = getPredicateIndex(nodes, start, endTokens);

		int nextTokenIndex = start;
		final int parseStart = start + 1;
		
		if (conditionIndex > -1) {
			Condition condition = new Condition();
			sqlCase.condition(condition);
			
			int conditionEnd = getFirstIndex(nodes, parseStart, endTokens);
			nextTokenIndex = parseCondition(condition, nodes, parseStart, conditionEnd);
			nextTokenIndex--;
		} else if (predicateIndex > -1) {
			Predicate predicate = new Predicate();
			sqlCase.predicate(predicate);
			
			int predicateEnd = getFirstIndex(nodes, parseStart, endTokens);
			nextTokenIndex = parsePredicate(predicate, nodes, parseStart, predicateEnd);
			nextTokenIndex--;
		}
		
		return nextTokenIndex;
	}

	private static void addPendingTable(TableNameSpecifier<?> specifier, StringBuilder tableName) {
		if (tableName.length() > 0) {
			specifier.tableName(tableName.toString());
			tableName.setLength(0);
		}
	}
	
	/**
	 * Combines the <b>parts</b> with <b>.</b> excluding last part which is the column name
	 * @param parts
	 * @return
	 */
	private static String combineTableNameParts(String[] parts) {
		StringBuilder builder = new StringBuilder();
		for (int i = 0; i < parts.length - 1; i++) {
			builder.append((i > 0 ? "." : "") + parts[i]);
		}
		return builder.toString();
	}
	
	private static int getConditionIndex(List<ParseToken> nodes, int start) {
		return getConditionIndex(nodes, start, null);
	}
	
	private static int getConditionIndex(List<ParseToken> nodes, int start, Set<String> endTokens) {
		boolean skipBetweenAnd = false;
		
		for (int i = start; i < nodes.size(); i++) {
			ParseToken node = nodes.get(i);
			String tokenUpperCase = node.getToken().toUpperCase();
			
			if (endTokens != null && endTokens.contains(tokenUpperCase)) {
				break;
			}
			
			switch (tokenUpperCase) {
				case Keywords.BETWEEN:
					skipBetweenAnd = true;
					break;
				case Keywords.AND:
					if (skipBetweenAnd) {
						skipBetweenAnd = false;
						break;
					}
					// Fall through to OR
				case Keywords.OR:
					return i;
			}
		}
		return -1;
	}
	
	private static int getFirstExpressionIndex(List<ParseToken> nodes, int start, int end) {
		for (int i = start; i < end; i++) {
			ParseToken node = nodes.get(i);
			
			switch (node.getToken()) {
				case BinaryOperator.STRING_CONCAT:
				case ArithmeticOperator.STRING_DIVIDE:
				case ArithmeticOperator.STRING_MINUS:
				case ArithmeticOperator.STRING_MOD:
				case ArithmeticOperator.STRING_MULTIPLY:
				case ArithmeticOperator.STRING_PLUS:
					return i;
			}
		}
		return -1;
	}
	
	/**
	 * 
	 * @param nodes
	 * @param tokens
	 * @return the index of any nodes' token that matches any in tokens Set<br>
	 * or the size of nodes if there are no matches
	 */
	private static int getFirstIndex(List<ParseToken> nodes, int start, Set<String> tokens) {
		for (int i = start; i < nodes.size(); i++) {
			ParseToken node = nodes.get(i);
			
			if (tokens.contains(node.getToken().toUpperCase())) {
				return i;
			}
		}
		return nodes.size();
	}
	
	private static int getFirstIndex(List<ParseToken> nodes, int start, String token) {
		for (int i = start; i < nodes.size(); i++) {
			ParseToken node = nodes.get(i);
			
			if (token.equals(node.getToken().toUpperCase())) {
				return i;
			}
		}
		return nodes.size();
	}
	
	private static int getLastExpressionIndex(List<ParseToken> nodes, int start, int end) {
		for (int i = end - 1; i >= start; i--) {
			ParseToken node = nodes.get(i);
			
			switch (node.getToken()) {
				case BinaryOperator.STRING_CONCAT:
				case ArithmeticOperator.STRING_DIVIDE:
				case ArithmeticOperator.STRING_MINUS:
				case ArithmeticOperator.STRING_MOD:
				case ArithmeticOperator.STRING_MULTIPLY:
				case ArithmeticOperator.STRING_PLUS:
					return i;
			}
		}
		return -1;
	}

	private static ParseToken getNextNode(List<ParseToken> nodes, int i) {
		int nextIndex = i + 1;
		return nextIndex < nodes.size() ? nodes.get(nextIndex) : null;
	}
	
	private static String getPendingString(StringBuilder pendingString) {
		String str = pendingString.toString();
		pendingString.setLength(0);
		return str;
	}
	
	private static int getPredicateIndex(List<ParseToken> nodes, int start) {
		return getPredicateIndex(nodes, start, null);
	}
	
	private static int getPredicateIndex(List<ParseToken> nodes, int start, Set<String> endTokens) {
		for (int i = start; i < nodes.size(); i++) {
			ParseToken node = nodes.get(i);
			String tokenUpperCase = node.getToken().toUpperCase();
			
			if (endTokens != null && endTokens.contains(tokenUpperCase)) {
				break;
			}
			
			switch (tokenUpperCase) {
				case Keywords.BETWEEN:
				case RelationalOperator.EQ:
				case Keywords.EXISTS:
				case RelationalOperator.GT:
				case RelationalOperator.GT_EQ:
				case Keywords.IN:
				case Keywords.IS_NOT_NULL:
				case Keywords.IS_NULL:
				case Keywords.LIKE:
				case RelationalOperator.LT:
				case RelationalOperator.LT_EQ:
				case Keywords.NOT_BETWEEN:
				case RelationalOperator.NOT_EQ:
				case Keywords.NOT_EXISTS:
				case Keywords.NOT_IN:
				case Keywords.NOT_LIKE:
					return i;
			}
		}
		return -1;
	}
	
	private static int getSetOperationEndIndex(List<ParseToken> nodes, int start) {
		for (int i = start; i < nodes.size(); i++) {
			ParseToken node = nodes.get(i);
			String tokenUpperCase = node.getToken().toUpperCase();
			
			if (TOKEN_SET_AFTER_SET_OPERATION_QUERY.contains(tokenUpperCase)) {
				return i;
			}
		}
		return nodes.size();
	}
	
	private static void parseCase(Case sqlCase, List<ParseToken> nodes) {
		ExpressionCaseHelper helper = new ExpressionCaseHelper(sqlCase);
		
		int start = addConditionOrPredicate(sqlCase, nodes, 0, TOKEN_SET_AFTER_CASE);
		
		for (int i = start; i < nodes.size(); i++) {
			i = parseCaseNode(sqlCase, nodes, i, helper);
		}
		helper.addLastColumn();
	}
	
	private static int parseCaseNode(
			final Case sqlCase,
			final List<ParseToken> nodes,
			int i,
			final ExpressionCaseHelper helper) {
		int expressionEndIndex = parseExpression(sqlCase, nodes, i);
		
		if (expressionEndIndex != i) {
			i = expressionEndIndex;

			if (i >= nodes.size()) {
				return nodes.size();
			}
		}
		
		ParseToken node = nodes.get(i);
		final String token = node.getToken();
		
		switch (token.toUpperCase()) {
			case Keywords.CASE:
				helper.caseCase(node);
				break;
			case Keywords.ELSE:
				helper.caseComma();
				sqlCase.ifElse();
				break;
			case Keywords.END:
				helper.caseComma();
				sqlCase.end();
				return nodes.size();
			case Keywords.THEN:
				helper.caseComma();
				sqlCase.then();
				i = addConditionOrPredicate(sqlCase, nodes, i, TOKEN_SET_AFTER_THEN);
				break;
			case ParseTree.TOKEN_PARENTHESES_GROUP:
				helper.caseParenthesesGroup(node);
				break;
			case Keywords.WHEN:
				helper.caseComma();
				sqlCase.when();
				i = addConditionOrPredicate(sqlCase, nodes, i, TOKEN_SET_AFTER_WHEN);
				break;
			default:
				helper.caseDefault(node, i);
				break;
		}
		return i;
	}

	private static void parseColumnList(ColumnList columns, List<ParseToken> nodes) {
		for (ParseToken node : nodes) {
			if (!",".equals(node.getToken())) {
				columns.column(node.getToken());
			}
		}
	}
	
	private static void parseColumnParenthesesGroup(ParseToken groupToken, ExpressionBuilding<?> parent) {
		if (!groupToken.getToken().equals(ParseTree.TOKEN_PARENTHESES_GROUP)) {
			throw new IllegalArgumentException("groupToken's token must be " + ParseTree.TOKEN_PARENTHESES_GROUP);
		}
		
		List<ParseToken> nodes = groupToken.getNodes();
		ExpressionCaseHelper helper = new ExpressionCaseHelper(parent);
		
		for (int i = 0; i < nodes.size(); i++) {
			int expressionEndIndex = parseExpression(parent, nodes, i);
			
			if (expressionEndIndex != i) {
				i = expressionEndIndex;

				if (i >= nodes.size()) {
					break;
				}
			}
			
			ParseToken node = nodes.get(i);
			final String token = node.getToken();
			
			switch (token.toUpperCase()) {
				case ",":
					helper.caseComma();
					break;
				case Keywords.AS:
					if (parent instanceof CastFunction) {
						helper.as();
						ParseToken nextNode = getNextNode(nodes, i);
						((CastFunction)parent).as(nextNode.getToken());
						i++;
					}
					break;
				case Keywords.CASE:
					helper.caseCase(node);
					break;
				case ParseTree.TOKEN_PARENTHESES_GROUP:
					helper.caseParenthesesGroup(node);
					break;
				case Keywords.SELECT:
					Select select = new Select();
					parent.subquery(select);
					i = parseSelectTree(select, nodes, i);
					break;
				default:
					helper.caseDefault(node, i);
					break;
			}
		}
		helper.addLastColumn();
	}

	private static int parseCondition(Condition condition, List<ParseToken> nodes, int start, int end) {
		int i = start;
		for (; i < end; i++) {
			i = parseConditionNode(condition, nodes, i, end);
		}
		return i;
	}
	
	private static int parseConditionNode(
			final Condition condition,
			final List<ParseToken> nodes,
			int i,
			final int end) {
		ParseToken node = nodes.get(i);
		
		Condition conditionGroup = condition;
		final String token = node.getToken();
		
		switch (token.toUpperCase()) {
			case Keywords.AND:
				condition.and();
				break;
			case Keywords.OR:
				condition.or();
				break;
			case ParseTree.TOKEN_PARENTHESES_GROUP:
				if (getConditionIndex(node.getNodes(), 0) > -1) {
					conditionGroup = new Condition();
					condition.group(conditionGroup);
				}
				parseCondition(conditionGroup, node.getNodes(), 0, node.getNodes().size());
				break;
			default:
				i = parseConditionPredicate(condition, nodes, i, end);
				break;
		}
		return i;
	}
	
	private static int parseConditionPredicate(Condition condition, List<ParseToken> nodes, int start, int end) {
		int predicateIndex = getPredicateIndex(nodes, start);
		
		if (predicateIndex > 0) {
			Predicate predicate = new Predicate();
			int conditionIndex = getConditionIndex(nodes, start);
			int predicateEnd = conditionIndex < 0 || end < conditionIndex 
					? end : conditionIndex;
			
			condition.predicate(predicate);
			start = parsePredicate(predicate, nodes, start, predicateEnd);
			start--;
		}
		return start;
	}

	private static int parseDelete(Delete delete, List<ParseToken> nodes, int start) {
		StringBuilder tableName = new StringBuilder();
		
		int i = start;
		for (; i < nodes.size(); i++) {
			parseDeleteNode(delete, nodes.get(i), tableName);
		}
		return i - 1;
	}
	
	private static void parseDeleteNode(final Delete delete, final ParseToken node, final StringBuilder tableName) {
		final String token = node.getToken();
		
		switch (token.toUpperCase()) {
			case Keywords.DELETE:
				break;
			case Keywords.FROM:
				for (ParseToken tablePart : node.getNodes()) {
					tableName.append(tablePart.getToken());
				}
				addPendingTable(delete, tableName);
				
				/* DELETE FROM JOIN syntax not ANSI standard */
				
//				if (tableName.length() > 0) {
//					delete.tableAlias(tableName.toString());
//					tableName.setLength(0);
//				}
//				
//				From from = new From();
//				delete.from(from);
//				parseFrom(from, node.getNodes());
				break;
			case Keywords.WHERE:
				addPendingTable(delete, tableName);
				
				Where where = new Where();
				delete.where(where);
				parseCondition(where, node.getNodes(), 0, node.getNodes().size());
				break;
			default:
				tableName.append(token);
				break;
		}
	}
	
	private static int parseExpression(ExpressionBuilding<?> builder, List<ParseToken> nodes, int i) {
		int expressionEndIndex = getFirstIndex(nodes, i, TOKEN_SET_EXPRESSION_END);
		int lastExpressionIndex = getLastExpressionIndex(nodes, i, expressionEndIndex);
		if (lastExpressionIndex < 0 
				/* Solves the "SELECT *" (select all) problem where the asterisk is treated as multiplication */
				|| lastExpressionIndex + 1 >= expressionEndIndex) {
			return i;
		}
		
		Expression expression = new Expression();
		builder.expression(expression);
		
		ExpressionCaseHelper helper = new ExpressionCaseHelper(expression);
		
		for (; i < expressionEndIndex; i++) {
			ParseToken node = nodes.get(i);
			
  			final String token = node.getToken();
			
			switch (token.toUpperCase()) {
				case Keywords.CASE:
					helper.caseCase(node);
					break;
				case BinaryOperator.STRING_CONCAT:
					helper.caseComma();
					expression.concat();
					break;
				case ArithmeticOperator.STRING_DIVIDE:
					helper.caseComma();
					expression.divide();
					break;
				case ArithmeticOperator.STRING_MINUS:
					helper.caseComma();
					expression.minus();
					break;
				case ArithmeticOperator.STRING_MOD:
					helper.caseComma();
					expression.mod();
					break;
				case ArithmeticOperator.STRING_MULTIPLY:
					helper.caseComma();
					expression.multiply();
					break;
				case ArithmeticOperator.STRING_PLUS:
					helper.caseComma();
					expression.plus();
					break;
				case ParseTree.TOKEN_PARENTHESES_GROUP:
					helper.caseParenthesesGroup(node);
					break;
				default:
					helper.caseDefault(node, i);
					break;
			}
		}
		helper.addLastColumn();
		
		return i;
	}
	
	private static void parseFrom(From from, List<ParseToken> nodes) {
		FromHelper helper = new FromHelper(from);
		
		for (int i = 0; i < nodes.size(); i++) {
			i = parseFromNode(from, nodes, i, helper);
		}
		helper.addPendingTable();
	}
	
	private static int parseFromNode(
			final From from,
			final List<ParseToken> nodes,
			int i,
			final FromHelper helper) {
		ParseToken node = nodes.get(i);
		
		final String token = node.getToken();
		
		switch (token.toUpperCase()) {
			case ",":
				break;
			case Keywords.AS:
				helper.addPendingTable();
				ParseToken nextNode = getNextNode(nodes, i);
				helper.addAlias(nextNode.getToken());
				i++;
				break;
			case Keywords.FULL_OUTER_JOIN:
				helper.addPendingTable();
				from.fullOuterJoin();
				break;
			case Keywords.INNER_JOIN:
				helper.addPendingTable();
				from.innerJoin();
				break;
			case Keywords.LEFT_OUTER_JOIN:
				helper.addPendingTable();
				from.leftOuterJoin();
				break;
			case Keywords.ON:
				helper.addPendingTable();
				
				Condition joinCondition = new Condition();
				from.on(joinCondition);
				
				int start = i + 1;
				i = parseCondition(joinCondition, nodes, start, getFirstIndex(nodes, start, TOKEN_SET_JOINS));
				i--;
				break;
			case Keywords.RIGHT_OUTER_JOIN:
				helper.addPendingTable();
				from.rightOuterJoin();
				break;
			case ParseTree.TOKEN_PARENTHESES_GROUP:
				parseFromParenthesesGroup(from, node.getNodes());
				break;
			default:
				helper.caseDefault(token);
				break;
		}
		return i;
	}

	private static void parseFromParenthesesGroup(From from, List<ParseToken> nodes) {
		switch (nodes.get(0).getToken().toUpperCase()) {
			case Keywords.SELECT:
				Select select = new Select();
				from.inlineView(select);
				parseSelectTree(select, nodes, 0);
				break;
			default: // Should be nested JOIN syntax
				from.leftParens();
				parseFrom(from, nodes);
				from.rightParens();
				break;
		}
	}
	
	private static void parseGroupBy(GroupBy groupBy, List<ParseToken> nodes) {
		StringBuilder column = new StringBuilder();
		
		for (int i = 0; i < nodes.size(); i++) {
			parseGroupByNode(groupBy, nodes.get(i), column);
		}
		parsePendingColumn(column, groupBy);
	}
	
	private static void parseGroupByNode(
			final GroupBy groupBy,
			final ParseToken node,
			final StringBuilder column) {
		final String token = node.getToken();
		
		switch (token.toUpperCase()) {
			case ",":
				parsePendingColumn(column, groupBy);
				break;
			default:
				column.append(node.getToken());
				break;
		}
	}

	private static int parseInsert(Insert insert, List<ParseToken> nodes, int start) {
		StringBuilder table = new StringBuilder();
		int i = start;
		
		int parenthesesMode = PARENTHESES_MODE_INSERT_COLUMNS;
		
		for (; i < nodes.size(); i++) {
			ParseToken node = nodes.get(i); 
			
			final String token = node.getToken();
			
			switch (token.toUpperCase()) {
				case ",":
				case Keywords.INSERT:
				case Keywords.INTO:
					break;
				case Keywords.SELECT:
					if (table.length() > 0) {
						insert.into(table.toString());
						table.setLength(0);
					}
					
					Select select = new Select();
					insert.subselect(select);
					i = parseSelectTree(select, nodes, i);
					break;
				case Keywords.VALUES:
					parenthesesMode = PARENTHESES_MODE_INSERT_VALUES;
					break;
				case ParseTree.TOKEN_PARENTHESES_GROUP:
					if (table.length() > 0) {
						insert.into(table.toString());
						table.setLength(0);
					}
					
					switch (parenthesesMode) {
						case PARENTHESES_MODE_INSERT_COLUMNS:
							ColumnList columns = new ColumnList();
							insert.columns(columns);
							
							parseColumnList(columns, node.getNodes());
							parenthesesMode = PARENTHESES_MODE_INSERT_VALUES;
							break;
						case PARENTHESES_MODE_INSERT_VALUES:
							ColumnValues values = new ColumnValues();
							insert.values(values);
							
							parseInsertValues(values, node.getNodes());
							break;
					}
					break;
				default:
					table.append(token);
					break;
			}
		}
		return i - 1;
	}
	
	private static void parseInsertValues(ColumnValues values, List<ParseToken> nodes) {
		ExpressionCaseHelper helper = new ExpressionCaseHelper(values);
		
		for (int i = 0; i < nodes.size(); i++) {
			parseInsertValue(values, nodes.get(i), i, helper);
		}
		helper.addLastColumn();
	}
	
	private static void parseInsertValue(
			final ColumnValues values,
			final ParseToken node,
			final int i,
			final ExpressionCaseHelper helper) {
		switch (node.getToken().toUpperCase()) {
			case ",":
				helper.caseComma();
				break;
			case Keywords.DEFAULT:
				values.sqlDefault();
				break;
			default:
				helper.caseDefault(node, i);
				break;
		}
	}
	
	private static void parseInValues(InValues values, List<ParseToken> valueNodes) {
		ExpressionCaseHelper helper = new ExpressionCaseHelper(values);
		
		for (int i = 0; i < valueNodes.size(); i++) {
			ParseToken valueNode = valueNodes.get(i);
			
			switch (valueNode.getToken().toUpperCase()) {
				case ",":
					helper.caseComma();
					break;
				case ParseTree.TOKEN_PARENTHESES_GROUP:
					helper.caseParenthesesGroup(valueNode);
					break;
				default:
					helper.caseDefault(valueNode, i);
					break;
			}
		}
		helper.addLastColumn();
	}
	
	private static int parseMerge(Merge merge, List<ParseToken> nodes, int i) {
		final MergeHelper helper = new MergeHelper(merge);
		
		for (; i < nodes.size(); i++) {
			i = parseMergeNode(merge, nodes, i, helper);
		}
		return i - 1;
	}
	
	private static int parseMergeNode(
			final Merge merge,
			final List<ParseToken> nodes,
			int i,
			final MergeHelper helper) {
		final ParseToken node = nodes.get(i);
		final String token = node.getToken();
		
		// Variables for table reference and WHEN clause search conditions
		//
		Condition searchCondition;
		int start;
		
		switch (token.toUpperCase()) {
			case Keywords.AS:
				helper.addPendingTable();
				ParseToken nextNode = getNextNode(nodes, i);
				helper.addAlias(nextNode.getToken());
				i++;
				break;
			case Keywords.DELETE:
				merge.delete();
				break;
			case Keywords.INSERT:
				Insert insert = new Insert();
				merge.insert(insert);
				i = parseInsert(insert, nodes, i + 1);
				break;
			case Keywords.INTO:
			case Keywords.MATCHED:
			case Keywords.MERGE:
			case Keywords.NOT:
				// Don't do anything for any of these cases
				break;
			case Keywords.ON:
				helper.addPendingTable();
				
				searchCondition = new Condition();
				merge.on(searchCondition);
				
				start = i + 1;
				i = parseCondition(searchCondition, nodes, start, getFirstIndex(nodes, start, Keywords.WHEN));
				i--;
				break;
			case Keywords.AND:
				searchCondition = new Condition();
				merge.on(searchCondition);
				
				start = i + 1;
				i = parseCondition(searchCondition, nodes, start, getFirstIndex(nodes, start, Keywords.THEN));
				i--;
				break;
			case Keywords.THEN:
				merge.then();
				break;
			case ParseTree.TOKEN_PARENTHESES_GROUP:
				Select tableReference = new Select();
				merge.using(tableReference);
				parseSelectTree(tableReference, nodes, i);
				break;
			case Keywords.UPDATE:
				Update update = new Update();
				merge.update(update);
				i = parseUpdate(update, nodes, i + 1);
				break;
			case Keywords.USING:
				helper.addPendingTable();
				break;
			case Keywords.WHEN:
				ParseToken matchedNode = getNextNode(nodes, i);
				String matchedNodeToken = matchedNode.getToken().toUpperCase();
				
				if (matchedNodeToken.equals(Keywords.MATCHED)) {
					merge.whenMatched();
					i++;
				} else if (matchedNodeToken.equals(Keywords.NOT)
						/*&& getNextNode(nodes, i + 2).getToken().toUpperCase().equals(Keywords.MATCHED)*/) {
					merge.whenNotMatched();
					i += 2;
				}
				break;
			default:
				helper.caseDefault(token);
				break;
		}
		return i;
	}

	private static int parseOffsetOrFetch(
			Select select, 
			List<ParseToken> nodes, 
			int i, 
			String keyword) {
		try {
			ParseToken intNode = getNextNode(nodes, i);
			int count = Integer.parseInt(intNode != null ? intNode.getToken() : null);
			
			switch (keyword) {
				case Keywords.FETCH: select.fetch(count); break;
				case Keywords.OFFSET: select.offset(count); break;
			}
		} catch (NumberFormatException e) {
			throw new IllegalArgumentException("must be an integer after " + keyword + " keyword", e);
		}
		i++;
		
		ParseToken rowsNode = getNextNode(nodes, i);
		String rows = rowsNode != null ? rowsNode.getToken().toUpperCase() : null;
		if (rowsNode == null 
				|| !rows.equals(Keywords.ROWS) && !rows.equals(Keywords.ROW)) {
			throw new IllegalArgumentException("expected {" + Keywords.ROWS 
					+ " | " + Keywords.ROW + "} after " + keyword + " skip count");
		}
		i++;
		
		return i;
	}
	
	private static void parseOrderBy(OrderBy orderBy, List<ParseToken> nodes) {
		StringBuilder column = new StringBuilder();
		
		for (int i = 0; i < nodes.size(); i++) {
			i = parseOrderByNode(orderBy, nodes, i, column);
		}
		parsePendingColumn(column, orderBy);
	}
	
	private static int parseOrderByNode(
			final OrderBy orderBy,
			final List<ParseToken> nodes,
			int i,
			final StringBuilder column) {
		ParseToken node = nodes.get(i);
		
		final String token = node.getToken();
		
		switch (token.toUpperCase()) {
			case ",":
				parsePendingColumn(column, orderBy);
				break;
			case Keywords.ASC:
				parsePendingColumn(column, orderBy);
				orderBy.asc();
				break;
			case Keywords.DESC:
				parsePendingColumn(column, orderBy);
				orderBy.desc();
				break;
			default:
				if (node instanceof StringLiteralParseToken) {
					StringLiteralParseToken literal = (StringLiteralParseToken)node;
					orderBy.alias(literal.getValue());
				} else {
					column.append(node.getToken());
				}
				break;
		}
		return i;
	}

	private static int parsePredicate(Predicate predicate, List<ParseToken> nodes, int start, int end) {
		ExpressionCaseHelper helper = new ExpressionCaseHelper(predicate);
		boolean checkForInValues = false;
		
		int i = start;
		for (; i < end; i++) {
			int expressionEndIndex = parseExpression(predicate, nodes, i);
			
			if (expressionEndIndex != i) {
				i = expressionEndIndex;

				if (i >= end) {
					break;
				}
			}
			
			ParseToken node = nodes.get(i);
			
			ExpressionBuilding<?> function = null;
			final String token = node.getToken();
			
			switch (token.toUpperCase()) {
				case Keywords.AND:
					helper.caseComma();
					predicate.and();
					break;
				case Keywords.BETWEEN:
					helper.caseComma();
					predicate.between();
					break;
				case Keywords.CASE:
					helper.caseCase(node);
					break;
				case RelationalOperator.EQ:
					helper.caseComma();
					predicate.eq();
					break;
				case Keywords.EXISTS:
					predicate.exists();
					break;
				case RelationalOperator.GT:
					helper.caseComma();
					predicate.gt();
					break;
				case RelationalOperator.GT_EQ:
					helper.caseComma();
					predicate.gteq();
					break;
				case Keywords.IN:
					helper.caseComma();
					predicate.in();
					checkForInValues = true;
					break;
				case Keywords.IS_NOT_NULL:
					helper.caseComma();
					predicate.isNotNull();
					break;
				case Keywords.IS_NULL:
					helper.caseComma();
					predicate.isNull();
					break;
				case Keywords.LIKE:
					helper.caseComma();
					predicate.like();
					break;
				case RelationalOperator.LT:
					helper.caseComma();
					predicate.lt();
					break;
				case RelationalOperator.LT_EQ:
					helper.caseComma();
					predicate.lteq();
					break;
				case Keywords.NOT_BETWEEN:
					helper.caseComma();
					predicate.notBetween();
					break;
				case RelationalOperator.NOT_EQ:
					helper.caseComma();
					predicate.noteq();
					break;
				case Keywords.NOT_EXISTS:
					helper.caseComma();
					predicate.notExists();
					break;
				case Keywords.NOT_IN:
					helper.caseComma();
					predicate.notIn();
					checkForInValues = true;
					break;
				case Keywords.NOT_LIKE:
					helper.caseComma();
					predicate.notLike();
					break;
				case ParseTree.TOKEN_PARENTHESES_GROUP:
					function = helper.createFunction();
					
					if (function instanceof Function) {
						parseColumnParenthesesGroup(node, function);
					} else if (getPredicateIndex(node.getNodes(), 0) > -1) {
						parsePredicate(predicate, node.getNodes(), 0, node.getNodes().size());
					} else {
						boolean parseColumnParenthesesGroup = true;
						
						if (checkForInValues) {
							checkForInValues = false;
							
							List<ParseToken> inValueNodes = node.getNodes();
							ParseToken inValueNode = inValueNodes.get(0);
							String inValueUpperCase = inValueNode.getToken().toUpperCase();
							
							parseColumnParenthesesGroup = inValueUpperCase.equals(Keywords.SELECT) 
									|| inValueUpperCase.startsWith(":"); /* : for Param object */
						}
						
						if (parseColumnParenthesesGroup) {
							parseColumnParenthesesGroup(node, predicate);
						} else {
							InValues values = new InValues();
							predicate.values(values);
							parseInValues(values, node.getNodes());
						}
					}
					break;
				default:
					helper.caseDefault(node, i);
					break;
			}
		}
		
		helper.addLastColumn();
		return i;
	}
	
	private static boolean parsePendingColumn(ExpressionCaseHelper helper, ColumnBuilder<?> builder) {
		return parsePendingColumn(helper.pendingString, builder);
	}
	
	private static boolean parsePendingColumn(StringBuilder pendingString, ColumnBuilder<?> builder) {
		if (pendingString.length() <= 0) {
			return false;
		}
		
		String str = getPendingString(pendingString);
		if (str.startsWith("'") && builder instanceof OrderBy) {
			((OrderBy)builder).alias(str);
		} else {
			String[] parts = str.split("\\.");
			
			if (parts.length > 2) {
				builder.tableName(combineTableNameParts(parts));
			} else if (parts.length > 1) {
				builder.tableAlias(parts[0]);
			}
			builder.column(parts[parts.length - 1]);
		}
		
		return true;
	}
	
	private static void parseSelectList(SelectList list, List<ParseToken> nodes) {
		ExpressionCaseHelper helper = new ExpressionCaseHelper(list);
		helper.setAlwaysAppendColumn(true);
		
		for (int i = 0; i < nodes.size(); i++) {
			i = parseSelectListNode(list, nodes, i, helper);
		}
		helper.addLastColumn();
	}
	
	private static int parseSelectListNode(
			final SelectList list,
			final List<ParseToken> nodes,
			int i,
			final ExpressionCaseHelper helper) {
		int expressionEndIndex = parseExpression(list, nodes, i);
		
		if (expressionEndIndex != i) {
			i = expressionEndIndex;

			if (i >= nodes.size()) {
				return nodes.size();
			}
		}
		
		final ParseToken node = nodes.get(i);
		
		ParseToken nextNode = null;
		final String token = node.getToken();
		
		switch (token.toUpperCase()) {
			case ",":
				helper.caseComma();
				break;
			case Keywords.AS:
				parsePendingColumn(helper, list);
				nextNode = getNextNode(nodes, i);
				
				if (nextNode instanceof StringLiteralParseToken) {
					StringLiteralParseToken literal = (StringLiteralParseToken)nextNode;
					list.as(literal.getValue());
				} else {
					list.as(nextNode.getToken());
				}
				
				i++;
				break;
			case Keywords.CASE:
				helper.caseCase(node);
				break;
			case Keywords.DISTINCT:
				list.distinct();
				break;
			case ParseTree.TOKEN_PARENTHESES_GROUP:
				helper.caseParenthesesGroup(node);
				break;
			default:
				helper.caseDefault(node, i);
				break;
		}
		return i;
	}

	private static int parseSelectTree(Select select, List<ParseToken> nodes, int start) {
		return parseSelectTree(select, nodes, start, nodes.size());
	}

	private static int parseSelectTree(Select select, List<ParseToken> nodes, int start, int end) {
		SelectTreeHelper helper = new SelectTreeHelper(select, nodes, start, end);
		helper.parseTokens();
		return helper.i - 1;
	}

	private static int parseSetValue(SetValue value, List<ParseToken> nodes, int start) {
		int end = getFirstIndex(nodes, start + 1, TOKEN_SET_SET_VALUE_TERMINATOR);
		int operatorIndex = getFirstExpressionIndex(nodes, start, end);
		int expressionIndex = getFirstIndex(nodes, start, "=") + 1;
		
		ExpressionCaseHelper helper = new ExpressionCaseHelper(value);
		helper.setAlwaysAppendColumn(true);
		
		int i = start;
		for (; i < end; i++) {
			ParseToken node = nodes.get(i);
			
			final String token = node.getToken();
			
			switch (token.toUpperCase()) {
				case ",":
					break;
				case "=":
					helper.caseComma();
					break;
				case ParseTree.TOKEN_PARENTHESES_GROUP:
					parseColumnParenthesesGroup(node, value);
					break;
				default:
					if (operatorIndex > 0 && expressionIndex == i) {
						i = parseExpression(value, nodes, i);
					} else {
						helper.caseDefault(node, i);
					}
					break;
			}
		}
		return i;
	}
	
	private static int parseSetValue(SetValues values, List<ParseToken> nodes, int i) {
		SetValue value = new SetValue();
		values.add(value);
		
		return parseSetValue(value, nodes, i);
	}
	
	private static void parseSetValues(SetValues values, List<ParseToken> nodes) {
		for (int i = 0; i < nodes.size(); i++) {
			i = parseSetValue(values, nodes, i);
		}
	}

	private static int parseUpdate(Update update, List<ParseToken> nodes, int start) {
		StringBuilder tableName = new StringBuilder();
		
		int i = start;
		for (; i < nodes.size(); i++) {
			parseUpdateNode(update, nodes.get(i), tableName);
		}
		return i - 1;
	}
	
	private static void parseUpdateNode(final Update update, final ParseToken node, final StringBuilder tableName) {
		final String token = node.getToken();
		
		switch (token.toUpperCase()) {
			case Keywords.SET:
				addPendingTable(update, tableName);
				
				SetValues values = new SetValues();
				update.set(values);
				parseSetValues(values, node.getNodes());
				break;
			case Keywords.UPDATE:
				break;
			case Keywords.WHERE:
				Where where = new Where();
				update.where(where);
				parseCondition(where, node.getNodes(), 0, node.getNodes().size());
				break;
			default:
				tableName.append(token);
				break;
		}
	}
	
	private static int parseWith(With with, List<ParseToken> nodes, int i) {
		WithHelper helper = new WithHelper(with, nodes, i);
		while (helper.parseNextToken());
		
		if (helper.terminalClauseFound) {
			return helper.i;
		} else {
			throw new IllegalArgumentException("could not find " + Keywords.SELECT + " keyword after common table expression(s)");
		}
	}
	
	/* END Static methods */
	
	/* START Static classes */
	
	/**
	 * A helper class that is used by multiple switch statements in {@link SqlParser}
	 * to provide common functionality. The methods are called in the case statements of those switch statements.
	 * @author Albert
	 *
	 */
	private static class ExpressionCaseHelper {
		@SuppressWarnings("rawtypes")
		private final ExpressionBuilding builder;
		private boolean expectingDot;
		private final StringBuilder pendingString;
		private boolean alwaysAppendColumn;

		public void setAlwaysAppendColumn(boolean alwaysAppendColumn) {
			this.alwaysAppendColumn = alwaysAppendColumn;
		}
		
		public ExpressionCaseHelper(ExpressionBuilding<?> builder) {
			this.builder = builder;
			pendingString = new StringBuilder();
		}

		public void addLastColumn() {
			caseComma();
		}
		
		public void as() {
			caseComma();
		}

		public void caseCase(ParseToken node) {
			appendPendingString();
			
			Case sqlCase = new Case();
			builder.sqlCase(sqlCase);
			parseCase(sqlCase, node.getNodes());
		}

		public void caseComma() {
			if (pendingString.toString().contains(".") || alwaysAppendColumn) {
				parsePendingColumn(pendingString, builder);
				expectingDot = false;
			} else {
				appendPendingString(false);
			}
		}

		@SuppressWarnings("unchecked")
		public void caseDefault(ParseToken node, int i) {
			String token = node.getToken();
			
			if (node instanceof LiteralParseToken) {
				LiteralParseToken literal = (LiteralParseToken)node;
				literal.toExpression(builder);
				return;
			} else if (token.startsWith(":")) {
				appendPendingString();
				builder.param(token.substring(1));
				return;
			} else if (pendingString.length() > 0 && expectingDot && !token.equals(".")) {
				appendPendingString();
			}
			
			pendingString.append(token);
			expectingDot = !expectingDot;
		}
		
		public void caseParenthesesGroup(ParseToken node) {
			ExpressionBuilding<?> parenthesesParent = createFunction();
			parseColumnParenthesesGroup(node, parenthesesParent);
		}
		
		/**
		 * Should call <pre>{@code parseColumnParenthesesGroup(ParseToken, ExpressionBuilding)}</pre>
		 * after calling this
		 * @return either a Function object if pendingString is not empty, or just the builder
		 */
		@SuppressWarnings("rawtypes")
		public ExpressionBuilding createFunction() {
			if (pendingString.length() == 0) {
				return builder;
			}
			
			String name = getPendingString(pendingString);
			expectingDot = false;
			
			Function function = null;
			switch (name.toUpperCase()) {
				case Keywords.AVG: function = AggregateFunction.avg(); break;
				case Keywords.CAST: function = new CastFunction(); break;
				case Keywords.COUNT: function = AggregateFunction.count(); break;
				case Keywords.MAX: function = AggregateFunction.max(); break;
				case Keywords.MIN: function = AggregateFunction.min(); break;
				case Keywords.SUM: function = AggregateFunction.sum(); break;
				default: function = new Function(name); break;
			}
			
			builder.function(function);
			return function;
		}

		private void appendPendingString() {
			appendPendingString(true);
		}
		
		private void appendPendingString(boolean isNextNodeAnExpression) {
			if (pendingString.length() > 0) {
				builder.append(getPendingString(pendingString), isNextNodeAnExpression);
				expectingDot = false;
			}
		}
	}
	
	/**
	 * Helper class that makes unit testing parsing of FROM clause easier, 
	 * with the slight overhead of creating this object
	 * @author Albert
	 *
	 */
	private static class FromHelper extends TableNameHelper<From> {
		public FromHelper(From from) {
			super(from);
		}
		
		@Override
		public void addAlias(String alias) {
			node.as(alias);
		}
	}
	
	private static class MergeHelper extends TableNameHelper<Merge> {
		public MergeHelper(Merge node) {
			super(node);
		}

		@Override
		public void addAlias(String alias) {
			node.as(alias);
		}
	}

	private static class SelectTreeHelper {
		private final int end;
		private int i;
		private final List<ParseToken> nodes;
		private final Select select;
		private boolean setOperation;
		
		public SelectTreeHelper(Select select, List<ParseToken> nodes, int start, int end) {
			this.end = end < 0 ? nodes.size() : end;
			i = start;
			this.nodes = nodes;
			this.select = select;
		}
		
		public void incrementTokenIndex() {
			i++;
		}
		
		public void parseCurrentToken() {
			ParseToken node = nodes.get(i);
			
			final String token = node.getToken();
			final String tokenUpperCase = token.toUpperCase();
			
			switch (tokenUpperCase) {
				case Keywords.FETCH:
					ParseToken firstNode = getNextNode(nodes, i);
					String firstNodeToken = firstNode.getToken().toUpperCase();
					if (firstNode == null
							|| !firstNodeToken.equals(Keywords.FIRST)
							&& !firstNodeToken.equals(Keywords.NEXT)) {
						throw new IllegalArgumentException("expected {" + Keywords.FIRST + " | " 
							+ Keywords.NEXT + "} after " + Keywords.FETCH);
					}
					i++;
	
					i = parseOffsetOrFetch(select, nodes, i, Keywords.FETCH);
					
					ParseToken onlyNode = getNextNode(nodes, i);
					if (onlyNode == null || !onlyNode.getToken().toUpperCase().equals(Keywords.ONLY)) {
						throw new IllegalArgumentException("expected " + Keywords.ONLY 
								+ " after {" + Keywords.ROWS + " | " + Keywords.ROW + "}");
					}
					i++;
					break;
				case Keywords.FROM:
					From from = new From();
					select.from(from);
					parseFrom(from, node.getNodes());
					break;
				case Keywords.GROUP_BY:
					GroupBy groupBy = new GroupBy();
					select.groupBy(groupBy);
					parseGroupBy(groupBy, node.getNodes());
					break;
				case Keywords.OFFSET:
					i = parseOffsetOrFetch(select, nodes, i, Keywords.OFFSET);
					break;
				case Keywords.ORDER_BY:
					OrderBy orderBy = new OrderBy();
					select.orderBy(orderBy);
					parseOrderBy(orderBy, node.getNodes());
					break;
				case Keywords.SELECT:
					if (setOperation) {
						Select query = new Select();
						select.query(query);
						i = parseSelectTree(query, nodes, i, getSetOperationEndIndex(nodes, i));
					} else if (!node.getNodes().isEmpty()) {
						SelectList list = new SelectList();
						select.list(list);
						parseSelectList(list, node.getNodes());
					}
					break;
				case Keywords.EXCEPT:
				case Keywords.INTERSECT:
				case Keywords.UNION:
				case Keywords.UNION_ALL:
					ParseToken nextNode = getNextNode(nodes, i);
					boolean parenthesizedQuery = nextNode != null && nextNode.getToken().equals(ParseTree.TOKEN_PARENTHESES_GROUP);
					if (nextNode == null 
							|| !nextNode.getToken().toUpperCase().equals(Keywords.SELECT) 
							&& !parenthesizedQuery) {
						throw new IllegalArgumentException("Expected " + Keywords.SELECT + " statement after " + tokenUpperCase);
					}
					
					setOperation = true;
					
					if (parenthesizedQuery) {
						Select query = new Select();
						parseSelectTree(query, nextNode.getNodes(), 0);
						
						switch (tokenUpperCase) {
							case Keywords.EXCEPT: select.except(query); break;
							case Keywords.INTERSECT: select.intersect(query); break;
							case Keywords.UNION: select.union(query); break;
							case Keywords.UNION_ALL: select.unionAll(query); break;
						}
						
						i++;
					} else {
						switch (tokenUpperCase) {
							case Keywords.EXCEPT: select.except(); break;
							case Keywords.INTERSECT: select.intersect(); break;
							case Keywords.UNION: select.union(); break;
							case Keywords.UNION_ALL: select.unionAll(); break;
						}
					}
					break;
				case Keywords.HAVING:
				case Keywords.WHERE:
					Condition condition = null;
					
					switch (tokenUpperCase) {
						case Keywords.HAVING:
							condition = new Having();
							select.having((Having)condition);
							break;
						case Keywords.WHERE:
							condition = new Where();
							select.where((Where)condition);
							break;
					}
					parseCondition(condition, node.getNodes(), 0, node.getNodes().size());
					break;
			}
		}
		
		public void parseTokens() {
			while (i < end) {
				parseCurrentToken();
				incrementTokenIndex();
			}
		}
	}
	
	/**
	 * Logic was extracted from {@link FromHelper} class for more general use and
	 * the only changes where "<code>private final T node;</code>" instead of "<code>private final From from;</code>"
	 * <br>There is no interface to set the last node's alias, not to be
	 * confused with {@link Aliasable} interface that defines the object that implements it
	 * to be able to have an alias on itself
	 * @author Albert
	 *
	 * @param <T> object that implements TableNameSpecifier and NodeHolder
	 */
	@SuppressWarnings("rawtypes")
	private static abstract class TableNameHelper<T extends TableNameSpecifier & NodeHolder> {
		protected boolean expectingDot;
		protected final T node;
		protected final StringBuilder tableName = new StringBuilder();
		
		public TableNameHelper(T node) {
			this.node = node;
		}
		
		public void addPendingTable() {
			handleSubqueryAlias();
			addTableNameInNode();
			expectingDot = false;
		}

		public void caseDefault(String token) {
			if (tableName.length() > 0 && expectingDot && !".".equals(token)) {
				addPendingTable();
				addAlias(token);
				expectingDot = false;
			} else {
				tableName.append(token);
				expectingDot = !expectingDot;
			}
		}
		
		public void handleSubqueryAlias() {
			if (tableName.length() > 0 && !node.getNodes().isEmpty()) {
				Node<?> lastNode = node.getNodes().get(node.getNodes().size() - 1);
				
				if (lastNode instanceof Select) {
					addAlias(tableName.toString());
					tableName.setLength(0);
				}
			}
		}
		
		public abstract void addAlias(String alias);
		
		protected void addTableNameInNode() {
			SqlParser.addPendingTable(node, tableName);
		}
	}
	
	/**
	 * Encapsulates the parsing of WITH clause, 
	 * makes it more testable
	 * @author Albert
	 *
	 */
	private static class WithHelper {
		private boolean completed;
		private int i;
		private final List<ParseToken> nodes;
		private boolean terminalClauseFound;
		private final With with;
		
		public WithHelper(With with, List<ParseToken> nodes, int i) {
			this.i = i;
			this.nodes = nodes;
			this.with = with;
		}
		
		public boolean parseNextToken() {
			if (completed || (completed = i >= nodes.size())) {
				return false;
			}
			
			ParseToken node = nodes.get(i);

			final String token = node.getToken();
			final String upperCasedToken = token.toUpperCase();
			
			switch (upperCasedToken) {
				case ",":
					break;
				case Keywords.AS:
					ParseToken selectNode = nodes.get(++i);
					if (!selectNode.getToken().equals(ParseTree.TOKEN_PARENTHESES_GROUP)) {
						throw new IllegalArgumentException("expected parentheses for common table expression query");
					}
					
					Select cteQuery = new Select();
					with.as(cteQuery);
					parseSelectTree(cteQuery, selectNode.getNodes(), 0);
					break;
				case Keywords.INSERT:
				case Keywords.SELECT:
					i--;
					terminalClauseFound = true;
					completed = true;
					return false;
				case Keywords.WITH:
					break;
				case ParseTree.TOKEN_PARENTHESES_GROUP:
					List<ParseToken> columns = node.getNodes();
					
					for (int j = 0; j < columns.size(); j++) {
						String column = columns.get(j).getToken();
						
						if (!column.equals(",")) {
							with.column(column);
						}
					}
					break;
				default:
					with.name(token);
					break;
			}
			
			i++;
			return true;
		}
	}
	
	/* END Static classes */
	
	@SuppressWarnings("rawtypes")
	private SqlStatement sqlStatement;
	private int statementType;
	private final List<String> tokens = new ArrayList<>();

	@SuppressWarnings("rawtypes")
	public SqlStatement getSqlStatement() { return sqlStatement; }
	public int getStatementType() { return statementType; }
	
	public SqlParser parse(String sql) {
		sqlStatement = null;
		statementType = 0;
		
		parseTree(sqlToParseTree(sql));
		return this;
	}

	/* START Protected methods */
	
	List<String> tokenize(String sql) {
		tokens.clear();
		
		Matcher matcher = PATTERN.matcher(sql);

		int index = 0;
		int quoteCount = 0;
		boolean stringLiteral = false;
		
		while (matcher.find()) {
			String token = sql.substring(index, matcher.start()).trim();
			if (!token.isEmpty()) {
				tokens.add(token);
			}
			
			String delimiter = sql.substring(matcher.start(), matcher.end());
			if (!delimiter.isEmpty()) {
				if (delimiter.equals("'")) {
					int nextSingleQuote = matcher.end() + 1;
					
					if (stringLiteral 
							&& quoteCount <= 0
							&& nextSingleQuote < sql.length() 
							&& sql.subSequence(matcher.start(), nextSingleQuote).equals("''")) {
						quoteCount = 2;
					}

					if (quoteCount <= 0) {
						stringLiteral = !stringLiteral;
					}
					quoteCount--;
				}
				
				if (!stringLiteral) {
					delimiter = delimiter.trim();
				}
				if (!delimiter.isEmpty()) {
					tokens.add(delimiter);
				}
			}
			
			index = matcher.end();
		}
		
		String lastToken = sql.substring(index);
		if (!lastToken.isEmpty()) {
			tokens.add(lastToken);
		}
		return tokens;
	}
	
	/* END Protected methods */
	
	/* BEGIN Private methods */

	private void parseTree(ParseToken rootToken) {
		List<ParseToken> nodes = rootToken.getNodes();
		
		With with = null;
		
		for (int i = 0; i < nodes.size(); i++) {
			ParseToken node = nodes.get(i);

			final String token = node.getToken();
			
			switch (token.toUpperCase()) {
				case Keywords.DELETE:
					Delete delete = new Delete();
					
					if (with != null) {
						delete.with(with);
					}
					i = parseDelete(delete, nodes, i + 1);
					
					sqlStatement = delete;
					statementType = Node.TYPE_DELETE;
					break;
				case Keywords.INSERT:
					Insert insert = new Insert();
					
					if (with != null) {
						insert.with(with);
					}
					i = parseInsert(insert, nodes, i + 1);
					
					sqlStatement = insert;
					statementType = Node.TYPE_INSERT;
					break;
				case Keywords.MERGE:
					Merge merge = new Merge();
					
					i = parseMerge(merge, nodes, i + 1);
					
					sqlStatement = merge;
					statementType = Node.TYPE_MERGE;
					break;
				case Keywords.SELECT:
					Select select = new Select();
					
					if (with != null) {
						select.with(with);
					}
					i = parseSelectTree(select, nodes, i);
					
					sqlStatement = select;
					statementType = Node.TYPE_SELECT;
					break;
				case Keywords.UPDATE:
					Update update = new Update();
					
					if (with != null) {
						update.with(with);
					}
					i = parseUpdate(update, nodes, i + 1);
					
					sqlStatement = update;
					statementType = Node.TYPE_UPDATE;
					break;
				case Keywords.WITH:
					with = new With();
					i = parseWith(with, nodes, i + 1);
					break;
			}
		}
	}

	private ParseToken sqlToParseTree(String sql) {
		return new ParseTree(tokenize(sql)).parseTokens();
	}
	
	/* END Private methods */
}