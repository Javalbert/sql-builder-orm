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
import com.github.javalbert.sqlbuilder.Expression;
import com.github.javalbert.sqlbuilder.Fetch;
import com.github.javalbert.sqlbuilder.Function;
import com.github.javalbert.sqlbuilder.Keywords;
import com.github.javalbert.sqlbuilder.Literal;
import com.github.javalbert.sqlbuilder.Node;
import com.github.javalbert.sqlbuilder.Offset;
import com.github.javalbert.sqlbuilder.Param;
import com.github.javalbert.sqlbuilder.Select;
import com.github.javalbert.sqlbuilder.Token;
import com.github.javalbert.utils.string.Strings;

@SuppressWarnings("rawtypes")
public class MySQL extends ANSI {
	public static final String KEYWORD_CONCAT = "CONCAT";
	public static final String KEYWORD_LIMIT = "LIMIT";
	
	public static Function concatFunction() { return new Function(KEYWORD_CONCAT); }
	
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
	protected void appendExpression(StringBuilder builder, Expression expression, boolean subExpression) {
		Function concat = createConcatFunction(expression);
		if (concat != null) {
			concat.setAlias(expression.getAlias());
			builder.append(print(concat));
		} else {
			super.appendExpression(builder, expression, subExpression);
		}
	}
	
	private static Function createConcatFunction(Expression expression) {
		for (Node node : expression.getNodes()) {
			if (node != BinaryOperator.CONCAT) {
				continue;
			}
			return createConcatFunction(expression.getNodes());
		}
		return null;
	}
}