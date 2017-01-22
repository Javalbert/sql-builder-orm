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
package chan.shundat.albert.sqlbuilder;

import java.util.ArrayList;
import java.util.List;
import java.util.Set;
import java.util.Stack;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import chan.shundat.albert.utils.collections.CollectionUtils;

@SuppressWarnings({ "rawtypes", "unchecked" })
public class SqlParser {
	private static final Set<String> BEFORE_FROM = CollectionUtils.immutableHashSet(
			Keywords.SELECT
			);
	private static final Set<String> BEFORE_GROUP_BY = CollectionUtils.immutableHashSet(
			Keywords.FROM,
			Keywords.SELECT,
			Keywords.WHERE
			);
	private static final Set<String> BEFORE_HAVING = CollectionUtils.immutableHashSet(
			Keywords.FROM,
			Keywords.GROUP_BY,
			Keywords.SELECT,
			Keywords.WHERE
			);
	private static final Set<String> BEFORE_ORDER_BY = CollectionUtils.immutableHashSet(
			Keywords.GROUP_BY,
			Keywords.FROM,
			Keywords.HAVING,
			Keywords.SELECT,
			Keywords.WHERE
			);
	private static final Set<String> BEFORE_WHERE = CollectionUtils.immutableHashSet(
			Keywords.FROM,
			Keywords.SELECT
			);

	private static final String REGEX_SPLIT = "\\s|%|'|\\(|\\)|\\*|\\+|,|-|\\.|\\/|<=|<>|<|=|>=|>";
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
			Keywords.RIGHT_OUTER_JOIN
			);
	private static final Set<String> TOKEN_SET_SET_VALUE_TERMINATOR = CollectionUtils.immutableHashSet(
			",",
			Keywords.WHERE
			);
	
	/* BEGIN Static methods */
	
	private static void addPendingTable(TableNameSpecifier specifier, StringBuilder tableName) {
		if (tableName.length() == 0) {
			return;
		}
		
		specifier.tableName(tableName.toString());
		tableName.setLength(0);
	}
	
	private static void assertAhead(List<String> tokens, int i, String tokenAhead) {
		int nextIndex = i + 1;
		String token = nextIndex < tokens.size() ? tokens.get(nextIndex) : null;
		
		if (token == null || !token.toUpperCase().equals(tokenAhead)) {
			throwExpectTokenException(tokenAhead, tokens.get(i));
		}
	}
	
	private static String combineTableNameParts(String[] parts) {
		StringBuilder builder = new StringBuilder();
		for (int i = 0; i < parts.length - 1; i++) {
			String part = parts[i];
			builder.append((i > 0 ? "." : "") + part);
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
	
	private static int getExpressionIndex(List<ParseToken> nodes, int start, int end) {
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

	private int getSetOperationEndIndex(List<ParseToken> nodes, int start) {
		for (int i = start; i < nodes.size(); i++) {
			ParseToken node = nodes.get(i);
			String tokenUpperCase = node.getToken().toUpperCase();
			
			if (TOKEN_SET_AFTER_SET_OPERATION_QUERY.contains(tokenUpperCase)) {
				return i;
			}
		}
		return nodes.size();
	}

	private static int parseOffsetOrFetch(OrderBy orderBy, 
			List<ParseToken> nodes, 
			int i, 
			String keyword) {
		try {
			ParseToken intNode = getNextNode(nodes, i);
			int count = Integer.parseInt(intNode != null ? intNode.getToken() : null);
			
			switch (keyword) {
				case Keywords.FETCH: orderBy.fetch(count); break;
				case Keywords.OFFSET: orderBy.offset(count); break;
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
	
	private static boolean parsePendingColumn(StringBuilder pendingString, ColumnBuilder builder) {
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
	
	private static void throwExpectTokenException(String expectedToken, String tokenBeforeExpectedToken) {
		throw new IllegalArgumentException("Expecting a " + expectedToken + " after " + tokenBeforeExpectedToken);
	}

	private static boolean tokenAhead(List<String> tokens, int i, String tokenAhead) {
		int nextIndex = i + 1;
		String token = nextIndex < tokens.size() ? tokens.get(nextIndex) : null;
		return tokenAhead.equals(token);
	}
	
	/* END Static methods */
	
	private SqlStatement sqlStatement;
	private int statementType;
	private final List<String> tokens = new ArrayList<>();

	public <T extends SqlStatement> T getSqlStatement() { return (T)sqlStatement; }
	public int getStatementType() { return statementType; }
	
	public SqlParser parse(String sql) {
		sqlStatement = null;
		statementType = 0;
		
		tokenize(sql);
		parseTree(new Parser().parseTokens());
		return this;
	}

	/* BEGIN Private methods */
	
	private int addConditionOrPredicate(Case sqlCase, List<ParseToken> nodes, final int start, Set<String> endTokens) {
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

	private void parseCase(Case sqlCase, List<ParseToken> nodes) {
		StringBuilder pendingString = new StringBuilder();

		ExpressionCaseHelper helper = new ExpressionCaseHelper(sqlCase, pendingString);
		
		int start = addConditionOrPredicate(sqlCase, nodes, 0, TOKEN_SET_AFTER_CASE);
		
		for (int i = start; i < nodes.size(); i++) {
			int expressionEndIndex = parseExpression(sqlCase, nodes, i);
			
			if (expressionEndIndex != i) {
				i = expressionEndIndex;

				if (i >= nodes.size()) {
					break;
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
					return;
				case Keywords.THEN:
					helper.caseComma();
					sqlCase.then();
					i = addConditionOrPredicate(sqlCase, nodes, i, TOKEN_SET_AFTER_THEN);
					break;
				case Parser.TOKEN_PARENTHESES_GROUP:
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
		}
		helper.addLastColumn();
	}

	private void parseColumnList(ColumnList columns, List<ParseToken> nodes) {
		for (ParseToken node : nodes) {
			if (node.getToken().equals(",")) {
				continue;
			}
			columns.column(node.getToken());
		}
	}
	
	private void parseColumnParenthesesGroup(ParseToken groupToken, ExpressionBuilding parent) {
		if (!groupToken.getToken().equals(Parser.TOKEN_PARENTHESES_GROUP)) {
			throw new IllegalArgumentException("groupToken's token must be " + Parser.TOKEN_PARENTHESES_GROUP);
		}
		
		List<ParseToken> nodes = groupToken.getNodes();
		StringBuilder pendingString = new StringBuilder();
		ExpressionCaseHelper helper = new ExpressionCaseHelper(parent, pendingString);
		
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
				case Parser.TOKEN_PARENTHESES_GROUP:
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

	private int parseCondition(Condition condition, List<ParseToken> nodes, int start, int end) {
		int i = start;
		for (; i < end; i++) {
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
				case Parser.TOKEN_PARENTHESES_GROUP:
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
		}
		return i;
	}
	
	private int parseConditionPredicate(Condition condition, List<ParseToken> nodes, int start, int end) {
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

	private int parseDelete(Delete delete, List<ParseToken> nodes, int start) {
		StringBuilder tableName = new StringBuilder();
		
		int i = start;
		for (; i < nodes.size(); i++) {
			ParseToken node = nodes.get(i);

			final String token = node.getToken();
			
			switch (token.toUpperCase()) {
				case Keywords.DELETE:
					break;
				case Keywords.FROM:
					for (ParseToken tablePart : node.getNodes()) {
						tableName.append(tablePart.getToken());
					}
					addPendingTable(delete, tableName);
					
					/* DELETE FROM syntax not ANSI standard */
					
//					if (tableName.length() > 0) {
//						delete.tableAlias(tableName.toString());
//						tableName.setLength(0);
//					}
//					
//					From from = new From();
//					delete.from(from);
//					parseFrom(from, node.getNodes());
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
		return i - 1;
	}
	
	private int parseExpression(ExpressionBuilding builder, List<ParseToken> nodes, int i) {
		int expressionEndIndex = getFirstIndex(nodes, i, TOKEN_SET_EXPRESSION_END);
		int expressionIndex = getExpressionIndex(nodes, i, expressionEndIndex);
		if (expressionIndex < 0) {
			return i;
		}
		
		Expression expression = new Expression();
		builder.expression(expression);
		
		StringBuilder pendingString = new StringBuilder();
		ExpressionCaseHelper helper = new ExpressionCaseHelper(expression, pendingString);
		
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
				case Parser.TOKEN_PARENTHESES_GROUP:
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
	
	private void parseFrom(From from, List<ParseToken> nodes) {
		StringBuilder tableName = new StringBuilder();
		boolean expectingDot = false;
		
		for (int i = 0; i < nodes.size(); i++) {
			ParseToken node = nodes.get(i);
			
			final String token = node.getToken();
			
			switch (token.toUpperCase()) {
				case ",":
					break;
				case Keywords.AS:
					ParseToken nextNode = getNextNode(nodes, i);
					from.as(nextNode.getToken());
					i++;
					break;
				case Keywords.FULL_OUTER_JOIN:
					addPendingTable(from, tableName);
					from.fullOuterJoin();
					break;
				case Keywords.INNER_JOIN:
					addPendingTable(from, tableName);
					from.innerJoin();
					break;
				case Keywords.LEFT_OUTER_JOIN:
					addPendingTable(from, tableName);
					from.leftOuterJoin();
					break;
				case Keywords.ON:
					addPendingTable(from, tableName);
					
					Condition joinCondition = new Condition();
					from.on(joinCondition);
					
					int start = i + 1;
					i = parseCondition(joinCondition, nodes, start, getFirstIndex(nodes, start, TOKEN_SET_JOINS));
					i--;
					break;
				case Keywords.RIGHT_OUTER_JOIN:
					addPendingTable(from, tableName);
					from.rightOuterJoin();
					break;
				case Parser.TOKEN_PARENTHESES_GROUP:
					parseFromParenthesesGroup(from, node.getNodes());
					break;
				default:
					if (tableName.length() > 0 && expectingDot && !token.equals(".")) {
						addPendingTable(from, tableName);
						from.as(token);
						expectingDot = false;
					} else {
						tableName.append(token);
						expectingDot = !expectingDot;
					}
					break;
			}
		}
		addPendingTable(from, tableName);
	}

	private void parseFromParenthesesGroup(From from, List<ParseToken> nodes) {
		for (int i = 0; i < nodes.size(); i++) {
			ParseToken node = nodes.get(i);
			
			final String token = node.getToken();
			
			switch (token.toUpperCase()) {
				case Keywords.SELECT:
					Select select = new Select();
					from.inlineView(select);
					parseSelectTree(select, nodes, 0);
					i = nodes.size();
					break;
			}
		}
	}
	
	private void parseGroupBy(GroupBy groupBy, List<ParseToken> nodes) {
		StringBuilder column = new StringBuilder();
		
		for (int i = 0; i < nodes.size(); i++) {
			ParseToken node = nodes.get(i);
			
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
		parsePendingColumn(column, groupBy);
	}

	private int parseInsert(Insert insert, List<ParseToken> nodes, int start) {
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
					Select select = new Select();
					insert.subselect(select);
					i = parseSelectTree(select, nodes, i);
					break;
				case Keywords.VALUES:
					parenthesesMode = PARENTHESES_MODE_INSERT_VALUES;
					break;
				case Parser.TOKEN_PARENTHESES_GROUP:
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
	
	private void parseInsertValues(ColumnValues values, List<ParseToken> nodes) {
		StringBuilder pendingString = new StringBuilder();
		ExpressionCaseHelper helper = new ExpressionCaseHelper(values, pendingString);
		
		for (int i = 0; i < nodes.size(); i++) {
			ParseToken node = nodes.get(i);
			
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
		helper.addLastColumn();
	}
	
	private void parseOrderBy(OrderBy orderBy, List<ParseToken> nodes) {
		StringBuilder column = new StringBuilder();
		
		for (int i = 0; i < nodes.size(); i++) {
			ParseToken node = nodes.get(i);
			
			final String token = node.getToken();
			
			switch (token.toUpperCase()) {
				case Keywords.ASC:
					parsePendingColumn(column, orderBy);
					orderBy.asc();
					break;
				case Keywords.DESC:
					parsePendingColumn(column, orderBy);
					orderBy.desc();
					break;
				case Keywords.FETCH:
					ParseToken firstNode = getNextNode(nodes, i);
					if (firstNode == null || !firstNode.getToken().toUpperCase().equals(Keywords.FIRST)) {
						throw new IllegalArgumentException("expected " + Keywords.FIRST + " after " + Keywords.FETCH);
					}
					i++;

					i = parseOffsetOrFetch(orderBy, nodes, i, Keywords.FETCH);
					
					ParseToken onlyNode = getNextNode(nodes, i);
					if (onlyNode == null || !onlyNode.getToken().toUpperCase().equals(Keywords.ONLY)) {
						throw new IllegalArgumentException("expected " + Keywords.ONLY 
								+ " after {" + Keywords.ROWS + " | " + Keywords.ROW + "}");
					}
					i++;
					break;
				case Keywords.OFFSET:
					parsePendingColumn(column, orderBy);
					i = parseOffsetOrFetch(orderBy, nodes, i, Keywords.OFFSET);
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
		}
		parsePendingColumn(column, orderBy);
	}

	private int parsePredicate(Predicate predicate, List<ParseToken> nodes, int start, int end) {
		StringBuilder pendingString = new StringBuilder();
		
		ExpressionCaseHelper helper = new ExpressionCaseHelper(predicate, pendingString);
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
			
			ExpressionBuilding function = null;
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
				case Parser.TOKEN_PARENTHESES_GROUP:
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
									|| inValueUpperCase.startsWith(":");
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
	
	private void parseInValues(InValues values, List<ParseToken> valueNodes) {
		StringBuilder pendingString = new StringBuilder();
		ExpressionCaseHelper helper = new ExpressionCaseHelper(values, pendingString);
		
		for (int i = 0; i < valueNodes.size(); i++) {
			ParseToken valueNode = valueNodes.get(i);
			
			switch (valueNode.getToken().toUpperCase()) {
				case ",":
					helper.caseComma();
					break;
				case Parser.TOKEN_PARENTHESES_GROUP:
					helper.caseParenthesesGroup(valueNode);
					break;
				default:
					helper.caseDefault(valueNode, i);
					break;
			}
		}
		helper.addLastColumn();
	}
	
	private void parseSelectList(SelectList list, List<ParseToken> nodes) {
		StringBuilder pendingString = new StringBuilder();
		
		ExpressionCaseHelper helper = new ExpressionCaseHelper(list, pendingString);
		helper.setAlwaysAppendColumn(true);
		
		for (int i = 0; i < nodes.size(); i++) {
			int expressionEndIndex = parseExpression(list, nodes, i);
			
			if (expressionEndIndex != i) {
				i = expressionEndIndex;

				if (i >= nodes.size()) {
					break;
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
					parsePendingColumn(pendingString, list);
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
				case Parser.TOKEN_PARENTHESES_GROUP:
					helper.caseParenthesesGroup(node);
					break;
				default:
					helper.caseDefault(node, i);
					break;
			}
		}
		helper.addLastColumn();
	}

	private int parseSelectTree(Select select, List<ParseToken> nodes, int start) {
		return parseSelectTree(select, nodes, start, nodes.size());
	}

	private int parseSelectTree(Select select, List<ParseToken> nodes, int start, int end) {
		int i = start;
		boolean setOperation = false;
		
		if (end < 0) {
			end = nodes.size();
		}
		
		for (; i < end; i++) {
			ParseToken node = nodes.get(i);
			
			final String token = node.getToken();
			final String tokenUpperCase = token.toUpperCase();
			
			switch (tokenUpperCase) {
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
					boolean parenthesizedQuery = nextNode != null && nextNode.getToken().equals(Parser.TOKEN_PARENTHESES_GROUP);
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
		return i - 1;
	}

	private int parseSetValue(SetValue value, List<ParseToken> nodes, int start, int end) {
		StringBuilder pendingString = new StringBuilder();
		
		int operatorIndex = getExpressionIndex(nodes, start, end);
		int expressionIndex = getFirstIndex(nodes, start, "=") + 1;
		
		ExpressionCaseHelper helper = new ExpressionCaseHelper(value, pendingString);
		helper.setAlwaysAppendColumn(true);
		
		int i = start;
		for (; i < end; i++) {
			ParseToken node = nodes.get(i);
			
			final String token = node.getToken();
			
			switch (token.toUpperCase()) {
				case "=":
					helper.caseComma();
					break;
				case Parser.TOKEN_PARENTHESES_GROUP:
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
	
	private void parseSetValues(SetValues values, List<ParseToken> nodes) {
		for (int i = 0; i < nodes.size(); i++) {
			SetValue value = new SetValue();
			values.add(value);
			
			int setValueEndIndex = getFirstIndex(nodes, i + 1, TOKEN_SET_SET_VALUE_TERMINATOR);
			i = parseSetValue(value, nodes, i, setValueEndIndex);
		}
	}
	
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

	private int parseUpdate(Update update, List<ParseToken> nodes, int start) {
		StringBuilder tableName = new StringBuilder();
		
		int i = start;
		for (; i < nodes.size(); i++) {
			ParseToken node = nodes.get(i);
			
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
		return i - 1;
	}

	private int parseWith(With with, List<ParseToken> nodes, int i) {
		for (; i < nodes.size(); i++) {
			ParseToken node = nodes.get(i);

			final String token = node.getToken();
			
			switch (token.toUpperCase()) {
				case ",":
					break;
				case Keywords.AS:
					ParseToken selectNode = nodes.get(++i);
					if (!selectNode.getToken().equals(Parser.TOKEN_PARENTHESES_GROUP)) {
						throw new IllegalArgumentException("expected parentheses for common table expression query");
					}
					
					Select cteQuery = new Select();
					with.as(cteQuery);
					parseSelectTree(cteQuery, selectNode.getNodes(), 0);
					break;
				case Keywords.INSERT:
				case Keywords.SELECT:
					return i - 1;
				case Keywords.WITH:
					break;
				case Parser.TOKEN_PARENTHESES_GROUP:
					List<ParseToken> columns = node.getNodes();
					
					for (int j = 0; j < columns.size(); j++) {
						String column = columns.get(j).getToken();
						
						if (column.equals(",")) {
							continue;
						}
						with.column(column);
					}
					break;
				default:
					with.name(token);
					break;
			}
		}
		throw new IllegalArgumentException("could not find " + Keywords.SELECT + " keyword after common table expression(s)");
	}
	
	private void tokenize(String sql) {
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
	}
	
	/* END Private methods */

	/* BEGIN Classes */
	
	private static class ParseToken {
		private final List<ParseToken> nodes = new ArrayList<>();
		private final String token;
		
		public List<ParseToken> getNodes() { return nodes; }
 		public String getToken() { return token; }
		
		public ParseToken(String token) {
			this(token, false);
		}
		
		public ParseToken(String token, boolean keyword) {
			this.token = keyword ? token.toUpperCase() : token;
		}
		
		public void addNode(ParseToken node) {
			nodes.add(node);
		}
		
		@Override
		public String toString() {
			return token + (nodes != null && !nodes.isEmpty() 
					? " (" + nodes.get(nodes.size() - 1).getToken() + ")" : "");
		}
	}

	private class ExpressionCaseHelper {
		private final ExpressionBuilding builder;
		private boolean expectingDot;
		private final StringBuilder pendingString;
		private boolean alwaysAppendColumn;

		public void setAlwaysAppendColumn(boolean alwaysAppendColumn) {
			this.alwaysAppendColumn = alwaysAppendColumn;
		}
		
		public ExpressionCaseHelper(ExpressionBuilding builder, StringBuilder pendingString) {
			this.builder = builder;
			this.pendingString = pendingString;
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

		public void caseDefault(ParseToken node, int i) {
			String token = node.getToken();
			
			if (node instanceof StringLiteralParseToken) {
				StringLiteralParseToken literal = (StringLiteralParseToken)node;
				builder.literal(literal.getValue());
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
			ExpressionBuilding parenthesesParent = createFunction();
			parseColumnParenthesesGroup(node, parenthesesParent);
		}
		
		/**
		 * Should call <pre>{@code parseColumnParenthesesGroup(ParseToken, ExpressionBuilding)}</pre>
		 * after calling this
		 * @return either a Function object if pendingString is not empty, or just the builder
		 */
		public ExpressionBuilding createFunction() {
			if (pendingString.length() > 0) {
				String name = getPendingString(pendingString);
				
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
			
			return builder;
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
	
	private class Parser {
		public static final String TOKEN_PARENTHESES_GROUP = "SQL PARENTHESES GROUP";
		public static final String TOKEN_ROOT = "SQL ROOT";
		
		private ParseToken currentToken;
		private final Stack<Integer> parenthesesStack = new Stack<>();
		private StringBuilder stringLiteral;
		private final Stack<ParseToken> tokenStack = new Stack<>();
		
		public Parser() {
			currentToken = new ParseToken(TOKEN_ROOT);
			tokenStack.push(currentToken);
		}
		
		public ParseToken parseTokens() {
			for (int i = 0; i < tokens.size(); i++) {
				currentToken = tokenStack.peek();

 				ParseToken parseToken = null;
				final String token = tokens.get(i);
				
				if (stringLiteral != null) {
					String nextToken = i + 1 < tokens.size() ? tokens.get(i + 1) : null;
					
					if (token.equals("'")) {
						if ("'".equals(nextToken)) {
							stringLiteral.append("'");
							i++;
						} else {
							parseToken = new StringLiteralParseToken(stringLiteral.toString());
							currentToken.addNode(parseToken);
							stringLiteral = null;
						}
					} else {
						stringLiteral.append(token);
					}
					
					continue;
				}
				
				final String tokenUpperCase = token.toUpperCase();
	  			switch (tokenUpperCase) {
					case "":
						break;
					case "'":
						if (stringLiteral == null) {
							stringLiteral = new StringBuilder();
						}
						break;
					case "(":
						parseToken = new ParseToken(TOKEN_PARENTHESES_GROUP);
						addAndPush(parseToken);
						break;
					case ")":
						popParenthesesStack();
						break;
					case Keywords.END:
						parseToken = new ParseToken(token, true);
						currentToken.addNode(parseToken);
						popStack();
						break;
					case Keywords.GROUP:
						assertAhead(tokens, i, Keywords.BY);
						i++;
						popBackToBefore(BEFORE_GROUP_BY);
						
						parseToken = new ParseToken(Keywords.GROUP_BY, true);
						addAndPush(parseToken);
						break;
					case Keywords.HAVING:
						popBackToBefore(BEFORE_HAVING);
						parseToken = new ParseToken(Keywords.HAVING, true);
						addAndPush(parseToken);
						break;
					case Keywords.IS:
						if (tokenAhead(tokens, i, Keywords.NOT)) {
							if (tokenAhead(tokens, i + 1, Keywords.NULL)) {
								i += 2;
								parseToken = new ParseToken(Keywords.IS_NOT_NULL, true);
							} else {
								throwExpectTokenException(Keywords.NULL, Keywords.NOT);
							}
						} else if (tokenAhead(tokens, i, Keywords.NULL)) {
							i++;
							parseToken = new ParseToken(Keywords.IS_NULL, true);
						} else {
							throwExpectTokenException(Keywords.NULL, tokenUpperCase);
						}
						currentToken.addNode(parseToken);
						break;
					case Keywords.INNER:
						assertAhead(tokens, i++, Keywords.JOIN);
						// Fall through to JOIN
					case Keywords.JOIN:
						parseToken = new ParseToken(Keywords.INNER_JOIN, true);
						currentToken.addNode(parseToken);
						break;
					case Keywords.NOT:
						if (tokenAhead(tokens, i, Keywords.BETWEEN)) {
							parseToken = new ParseToken(Keywords.NOT_BETWEEN, true);
						} else if (tokenAhead(tokens, i, Keywords.EXISTS)) {
							parseToken = new ParseToken(Keywords.NOT_EXISTS, true);
						} else if (tokenAhead(tokens, i, Keywords.IN)) {
							parseToken = new ParseToken(Keywords.NOT_IN, true);
						} else if (tokenAhead(tokens, i, Keywords.LIKE)) {
							parseToken = new ParseToken(Keywords.NOT_LIKE, true);
						} else {
							throw new IllegalArgumentException("Expecting { " + Keywords.BETWEEN 
									+ " | " + Keywords.EXISTS 
									+ " | " + Keywords.IN 
									+ " | " + Keywords.LIKE 
									+ " } after " + tokenUpperCase);
						}
						i++;
						currentToken.addNode(parseToken);
						break;
					case Keywords.ORDER:
						assertAhead(tokens, i, Keywords.BY);
						i++;
						popBackToBefore(BEFORE_ORDER_BY);
						
						parseToken = new ParseToken(Keywords.ORDER_BY, true);
						addAndPush(parseToken);
						break;
					case Keywords.FULL:
					case Keywords.LEFT:
					case Keywords.RIGHT:
						if (tokenAhead(tokens, i, Keywords.OUTER)) {
							if (tokenAhead(tokens, i + 1, Keywords.JOIN)) {
								i += 2;
							} else {
								throwExpectTokenException(Keywords.JOIN, Keywords.OUTER);
							}
						} else if (tokenAhead(tokens, i, Keywords.JOIN)) {
							i++;
						} else {
							throwExpectTokenException(Keywords.JOIN, tokenUpperCase);
						}

						switch (tokenUpperCase) {
							case Keywords.FULL: parseToken = new ParseToken(Keywords.FULL_OUTER_JOIN, true); break;
							case Keywords.LEFT: parseToken = new ParseToken(Keywords.LEFT_OUTER_JOIN, true); break;
							case Keywords.RIGHT: parseToken = new ParseToken(Keywords.RIGHT_OUTER_JOIN, true); break;
						}
						currentToken.addNode(parseToken);
						break;
					case Keywords.CASE:
					case Keywords.SELECT:
					case Keywords.SET:
						parseToken = new ParseToken(token, true);
						addAndPush(parseToken);
						break;
					case Keywords.EXCEPT:
					case Keywords.INTERSECT:
					case Keywords.UNION:
						popBackToBefore(BEFORE_ORDER_BY);
						
						if (tokenUpperCase.equals(Keywords.UNION) 
								&& tokenAhead(tokens, i, Keywords.ALL)) {
							parseToken = new ParseToken(Keywords.UNION_ALL, true);
							i++;
						} else {
							parseToken = new ParseToken(token, true);
						}
						
						currentToken.addNode(parseToken);
						break;
					case Keywords.FROM:
					case Keywords.WHERE:
						switch (tokenUpperCase) {
							case Keywords.FROM: popBackToBefore(BEFORE_FROM); break;
							case Keywords.WHERE: popBackToBefore(BEFORE_WHERE); break;
						}
						parseToken = new ParseToken(token, true);
						addAndPush(parseToken);
						break;
					default:
						parseToken = new ParseToken(token, Keywords.KEYWORD_SET.contains(token));
						currentToken.addNode(parseToken);
						break;
				}
			}
			
			return returnRoot();
		}

		private void addAndPush(ParseToken token) {
			currentToken.addNode(token);
			tokenStack.push(token);
			
			if (token.getToken().equals(TOKEN_PARENTHESES_GROUP)) {
				parenthesesStack.push(1);
			} else if (!parenthesesStack.isEmpty()) {
				int size = parenthesesStack.pop();
				parenthesesStack.push(size + 1);
			}
		}
		
		private void downsizeCurrentParentheses() {
			if (!parenthesesStack.isEmpty()) {
				int size = parenthesesStack.pop();
				size--;
				
				if (size > 0) {
					parenthesesStack.push(size);
				}
			}
		}
		
		private void popBackToBefore(Set<String> tokens) {
			if (tokenStack.size() == 1) {
				return;
			}
			
			ParseToken poppedToken = null;
			do {
				poppedToken = popStack();
				currentToken = tokenStack.peek();
			} while (!tokens.contains(poppedToken.getToken()) 
					&& !currentToken.getToken().equals(TOKEN_ROOT));
		}
		
		private void popParenthesesStack() {
			int len = !parenthesesStack.isEmpty() ? parenthesesStack.pop() : 1;
			for (int i = 0; i < len; i++) {
				tokenStack.pop();
			}
		}
		
		private ParseToken popStack() {
			ParseToken poppedToken = tokenStack.pop();
			downsizeCurrentParentheses();
			return poppedToken;
		}
		
		private ParseToken returnRoot() {
			while (tokenStack.size() > 1) {
				tokenStack.pop();
			}
			return tokenStack.pop();
		}
	}
	
	private class StringLiteralParseToken extends ParseToken {
		private String value;

		public String getValue() { return value; }
		
		public StringLiteralParseToken(String str) {
			super(SqlStringUtils.createLiteralToken(str), false);
			value = str;
		}
	}
	
	/* END Classes */
}