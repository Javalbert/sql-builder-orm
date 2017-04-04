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
package com.github.javalbert.sqlbuilder.vendor;

import java.util.List;

import com.github.javalbert.sqlbuilder.BinaryOperator;
import com.github.javalbert.sqlbuilder.Case;
import com.github.javalbert.sqlbuilder.Column;
import com.github.javalbert.sqlbuilder.Condition;
import com.github.javalbert.sqlbuilder.Expression;
import com.github.javalbert.sqlbuilder.Fetch;
import com.github.javalbert.sqlbuilder.From;
import com.github.javalbert.sqlbuilder.Function;
import com.github.javalbert.sqlbuilder.GroupBy;
import com.github.javalbert.sqlbuilder.Keywords;
import com.github.javalbert.sqlbuilder.Literal;
import com.github.javalbert.sqlbuilder.Node;
import com.github.javalbert.sqlbuilder.Offset;
import com.github.javalbert.sqlbuilder.OrderBy;
import com.github.javalbert.sqlbuilder.Param;
import com.github.javalbert.sqlbuilder.Select;
import com.github.javalbert.sqlbuilder.SelectList;
import com.github.javalbert.sqlbuilder.SetOperator;
import com.github.javalbert.sqlbuilder.Token;
import com.github.javalbert.utils.string.Strings;

@SuppressWarnings("rawtypes")
public class MySQL extends ANSI {
	public static final String KEYWORD_CONCAT = "CONCAT";
	public static final String KEYWORD_LIMIT = "LIMIT";
	
	public static Function concatFunction() { return new Function(KEYWORD_CONCAT); }
	
	private static Function createConcatFunction(Expression expression) {
		for (Node node : expression.getNodes()) {
			if (node == BinaryOperator.CONCAT) {
				return createConcatFunction(expression.getNodes());
			}
		}
		return null;
	}
	
	private static Function createConcatFunction(List<Node> nodes) {
		Function concat = concatFunction();
		
		for (Node node : nodes) {
			switch (node.getType()) {
				case Node.TYPE_CASE:
					concat.sqlCase((Case)node);
					break;
				case Node.TYPE_COLUMN:
					Column column = (Column)node;
					
					if (column.getPrefix() != null) {
						switch (column.getPrefix()) {
							case TABLE_ALIAS: concat.tableAlias(column.getPrefixValue()); break;
							case TABLE_NAME: concat.tableName(column.getPrefixValue()); break;
						}
					}
					
					concat.column(column.getName());
					break;
				case Node.TYPE_EXPRESSION:
					concat.expression((Expression)node);
					break;
				case Node.TYPE_FUNCTION:
					concat.function((Function)node);
					break;
				case Node.TYPE_LITERAL_BOOLEAN:
				case Node.TYPE_LITERAL_NULL:
				case Node.TYPE_LITERAL_NUMBER:
				case Node.TYPE_LITERAL_STRING:
					Literal literal = (Literal)node;
					
					switch (node.getType()) {
						case Node.TYPE_LITERAL_BOOLEAN: concat.literal((Boolean)literal.getValue()); break;
						case Node.TYPE_LITERAL_NULL: concat.literalNull(); break;
						case Node.TYPE_LITERAL_NUMBER: concat.literal((Number)literal.getValue()); break;
						case Node.TYPE_LITERAL_STRING: concat.literal((String)literal.getValue()); break;
					}
					break;
				case Node.TYPE_PARAM:
					Param param = (Param)node;
					concat.param(param.getName());
					break;
				case Node.TYPE_SELECT:
					Select select = (Select)node;
					concat.subquery(select);
					break;
				case Node.TYPE_TOKEN:
					Token token = (Token)node;
					concat.append(token.getToken(), token.isNextNodeAnExpression());
					break;
			}
		}
		
		return concat;
	}
	
	@Override
	public String createTableIdentifier(String catalog, String schema, String table) {
		StringBuilder identifier = new StringBuilder();
		
		if (!Strings.isNullOrEmpty(catalog)) {
			identifier.append(catalog).append(".");
		} else if (!Strings.isNullOrEmpty(schema)) {
			identifier.append(schema).append(".");
		}
		identifier.append(table);
		
		return identifier.toString();
	}
	
	@Override
	public String print(Fetch fetch) {
		StringBuilder builder = new StringBuilder()
				.append(KEYWORD_LIMIT)
				.append(" ")
				.append(fetch.getFetchCount());
		return builder.toString();
	}
	
	@Override
	public String print(Offset offset) {
		StringBuilder builder = new StringBuilder()
				.append(Keywords.OFFSET)
				.append(" ")
				.append(offset.getSkipCount());
		return builder.toString();
	}

	@Override
	public String print(Select select, final boolean subquery) {
		/*
		 * A copy of ANSI.print(Select, boolean) except for
		 * "LIMIT <row_count> OFFSET <skip>" MySQL syntax which is 
		 * "OFFSET <skip> ROWS FETCH FIRST <row_count> ROWS ONLY" in ANSI
		 */
		Offset offset = null;
		
		StringBuilder builder = new StringBuilder();
		
		if (subquery) {
			builder.append("(");
		}
		
		beginWithClause(builder, select);
		builder.append(Keywords.SELECT);
		
		for (Node node : select.getNodes()) {
			switch (node.getType()) {
				case Node.TYPE_HAVING:
				case Node.TYPE_WHERE:
					switch (node.getType()) {
						case Node.TYPE_HAVING: builder.append(" ").append(Keywords.HAVING); break;
						case Node.TYPE_WHERE: builder.append(" ").append(Keywords.WHERE); break;
					}
					// Fall through to TYPE_CONDITION
				case Node.TYPE_CONDITION:
					builder.append(" ").append(print((Condition)node));
					break;
				case Node.TYPE_FETCH:
					builder.append(" ")
							.append(print((Fetch)node))
							.append(" ")
							.append(print(offset));
					break;
				case Node.TYPE_FROM:
					builder.append(" ").append(print((From)node));
					break;
				case Node.TYPE_GROUP_BY:
					builder.append(" ").append(print((GroupBy)node));
					break;
				case Node.TYPE_OFFSET:
					offset = (Offset)node;
					break;
				case Node.TYPE_ORDER_BY:
					builder.append(" ").append(print((OrderBy)node));
					break;
				case Node.TYPE_SELECT:
					builder.append(" ").append(print((Select)node));
					break;
				case Node.TYPE_SELECT_LIST:
					builder.append(" ").append(print((SelectList)node));
					break;
				case Node.TYPE_SET_OPERATOR:
					builder.append(" ").append(print((SetOperator)node));
					break;
			}
		}
		if (subquery) {
			builder.append(")");
		}

		appendAsKeyword(builder, select);
		return builder.toString();
	}
	
	@Override
	protected void appendExpression(StringBuilder builder, Expression expression, boolean subExpression) {
		Function concat = createConcatFunction(expression);
		if (concat != null) {
			concat.setAlias(expression.getAlias());
			builder.append(print(concat));
		} else {
			super.appendExpression(builder, expression, subExpression);
		}
	}
}