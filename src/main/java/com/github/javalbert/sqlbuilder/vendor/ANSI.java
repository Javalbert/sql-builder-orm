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

import com.github.javalbert.sqlbuilder.Aliasable;
import com.github.javalbert.sqlbuilder.Case;
import com.github.javalbert.sqlbuilder.Column;
import com.github.javalbert.sqlbuilder.ColumnList;
import com.github.javalbert.sqlbuilder.ColumnValues;
import com.github.javalbert.sqlbuilder.CommonTableExpression;
import com.github.javalbert.sqlbuilder.Condition;
import com.github.javalbert.sqlbuilder.Delete;
import com.github.javalbert.sqlbuilder.Expression;
import com.github.javalbert.sqlbuilder.ExpressionBuilder;
import com.github.javalbert.sqlbuilder.Fetch;
import com.github.javalbert.sqlbuilder.From;
import com.github.javalbert.sqlbuilder.Function;
import com.github.javalbert.sqlbuilder.GroupBy;
import com.github.javalbert.sqlbuilder.InValues;
import com.github.javalbert.sqlbuilder.Insert;
import com.github.javalbert.sqlbuilder.Join;
import com.github.javalbert.sqlbuilder.Keywords;
import com.github.javalbert.sqlbuilder.Literal;
import com.github.javalbert.sqlbuilder.Node;
import com.github.javalbert.sqlbuilder.NodeHolder;
import com.github.javalbert.sqlbuilder.Offset;
import com.github.javalbert.sqlbuilder.OrderBy;
import com.github.javalbert.sqlbuilder.Param;
import com.github.javalbert.sqlbuilder.Predicate;
import com.github.javalbert.sqlbuilder.Select;
import com.github.javalbert.sqlbuilder.SelectList;
import com.github.javalbert.sqlbuilder.SetOperator;
import com.github.javalbert.sqlbuilder.SetValue;
import com.github.javalbert.sqlbuilder.SetValues;
import com.github.javalbert.sqlbuilder.SqlStringUtils;
import com.github.javalbert.sqlbuilder.Table;
import com.github.javalbert.sqlbuilder.Token;
import com.github.javalbert.sqlbuilder.Update;
import com.github.javalbert.sqlbuilder.Where;
import com.github.javalbert.sqlbuilder.With;
import com.github.javalbert.utils.string.Strings;

@SuppressWarnings("rawtypes")
public class ANSI implements Vendor {
	public static final ANSI INSTANCE = new ANSI();
	
	protected static void appendAsKeyword(StringBuilder builder, Aliasable aliasable) {
		String alias = aliasable.getAlias();
		if (Strings.isNullOrEmpty(alias)) {
			return;
		}
		
		builder.append(" ")
				.append(Keywords.AS)
				.append(" ")
				.append(SqlStringUtils.createLiteralToken(alias));
	}

	/* BEGIN Vendor interface methods */
	
	@Override
	public String createTableIdentifier(com.github.javalbert.orm.Table tableAnno) {
		StringBuilder identifier = new StringBuilder();
		
		if (!Strings.isNullOrEmpty(tableAnno.catalog())) {
			identifier.append(tableAnno.catalog()).append(".");
		}
		if (!Strings.isNullOrEmpty(tableAnno.schema())) {
			identifier.append(tableAnno.schema()).append(".");
		}
		identifier.append(tableAnno.name());
		
		return identifier.toString();
	}
	
	@Override
	public String print(Case sqlCase) {
		StringBuilder builder = new StringBuilder();
		builder.append(Keywords.CASE);
		
		for (Node node : sqlCase.getNodes()) {
			if (builder.length() > 0) {
				builder.append(" ");
			}
			
			switch (node.getType()) {
				case Node.TYPE_CASE:
					builder.append(print((Case)node));
					break;
				case Node.TYPE_COLUMN:
					builder.append(print((Column)node));
					break;
				case Node.TYPE_CONDITION:
					builder.append(print((Condition)node));
					break;
				case Node.TYPE_EXPRESSION:
					appendExpression(builder, (Expression)node);
					break;
				case Node.TYPE_FUNCTION:
					builder.append(print((Function)node));
					break;
				case Node.TYPE_LITERAL_BOOLEAN:
				case Node.TYPE_LITERAL_NULL:
				case Node.TYPE_LITERAL_NUMBER:
				case Node.TYPE_LITERAL_STRING:
					builder.append(print((Literal)node));
					break;
				case Node.TYPE_PARAM:
					builder.append(print((Param)node));
					break;
				case Node.TYPE_PREDICATE:
					builder.append(print((Predicate)node));
					break;
				case Node.TYPE_SELECT:
					builder.append(print((Select)node, true));
					break;
				case Node.TYPE_TOKEN:
					builder.append(print((Token)node));
					break;
			}
		}
		
		appendAsKeyword(builder, sqlCase);
		return builder.toString();
	}

	@Override
	public String print(Column column) {
		StringBuilder builder = new StringBuilder();
		
		if (!Strings.isNullOrEmpty(column.getPrefixValue())) {
			builder.append(column.getPrefixValue()).append(".");
		}
		if (!Strings.isNullOrEmpty(column.getName())) {
			builder.append(column.getName());
			appendAsKeyword(builder, column);
		} else if (!Strings.isNullOrEmpty(column.getAlias())) {
			builder.append(SqlStringUtils.createLiteralToken(column.getAlias()));
		}
		
		return builder.toString();
	}

	@Override
	public String print(ColumnList columns) {
		StringBuilder builder = new StringBuilder("(");
		
		List<Node> nodes = columns.getNodes();
		for (int i = 0; i < nodes.size(); i++) {
			Node node = nodes.get(i);
			
			if (node.getType() != Node.TYPE_COLUMN) {
				continue;
			}
			if (i > 0) {
				builder.append(", ");
			}
			
			Column column = (Column)node;
			builder.append(print(column));
		}
		
		builder.append(")");
		return builder.toString();
	}
	
	@Override
	public String print(ColumnValues values) {
		StringBuilder builder = new StringBuilder()
				.append("(")
				.append(printCommaSeparated(values))
				.append(")");
		return builder.toString();
	}
	
	@Override
	public String print(CommonTableExpression cte) {
		StringBuilder builder = new StringBuilder(cte.getName());
		List<String> columns = cte.getColumns();
		
		if (columns != null && !columns.isEmpty()) {
			builder.append(" (");
			
			for (int i = 0; i < columns.size(); i++) {
				if (i > 0 ) {
					builder.append(", ");
				}
				builder.append(columns.get(i));
			}
			builder.append(")");
		}
		
		builder.append(" ")
				.append(Keywords.AS)
				.append(" ")
				.append(print(cte.getSelect(), true));
		return builder.toString();
	}
	
	@Override
	public String print(Condition condition) {
		return print(condition, false);
	}
	
	@Override
	public String print(Condition condition, final boolean group) {
		StringBuilder builder = new StringBuilder();
		if (group) {
			builder.append("(");
		}
		
		List<Node> nodes = condition.getNodes();
		for (int i = 0; i < nodes.size(); i++) {
			Node node = nodes.get(i);
			
			if (i > 0) {
				builder.append(" ");
			}
			switch (node.getType()) {
				case Node.TYPE_CONDITION:
					builder.append(print((Condition)node, true));
					break;
				case Node.TYPE_PREDICATE:
					builder.append(print((Predicate)node));
					break;
				case Node.TYPE_TOKEN:
					builder.append(print((Token)node));
					break;
			}
		}

		if (group) {
			builder.append(")");
		}
		return builder.toString();
	}
	
	@Override
	public String print(Delete delete) {
		StringBuilder builder = new StringBuilder();
//		boolean specifiedTable = false;
		
		beginWithClause(builder, delete);
		builder.append(Keywords.DELETE_FROM);
		
		for (Node node : delete.getNodes()) {
			switch (node.getType()) {
				// DELETE FROM JOIN syntax not ANSI standard
//				case Node.TYPE_FROM:
//					builder.append(" ").append(print((From)node));
//					break;
				case Node.TYPE_TABLE:
					builder.append(/*specifiedTable ? ", " : */" ").append(print((Table)node));
//					specifiedTable = true;
					break;
				case Node.TYPE_WHERE:
					builder.append(" ")
							.append(Keywords.WHERE)
							.append(" ")
							.append(print((Where)node));
					break;
			}
		}
		
		return builder.toString();
	}
	
	@Override
	public String print(Expression expression) {
		return print(expression, false);
	}
	
	@Override
	public String print(Expression expression, final boolean subExpression) {
		StringBuilder builder = new StringBuilder();
		if (subExpression) {
			builder.append("(");
		}
		
		List<Node> nodes = expression.getNodes();
		for (int i = 0; i < nodes.size(); i++) {
			Node node = nodes.get(i);
			
			if (i > 0) {
				builder.append(" ");
			}
			
			switch (node.getType()) {
				case Node.TYPE_CASE:
					builder.append("(")
							.append(print((Case)node))
							.append(")");
					break;
				case Node.TYPE_COLUMN:
					builder.append(print((Column)node));
					break;
				case Node.TYPE_EXPRESSION:
					appendExpression(builder, (Expression)node, true);
					break;
				case Node.TYPE_FUNCTION:
					builder.append(print((Function)node));
					break;
				case Node.TYPE_LITERAL_BOOLEAN:
				case Node.TYPE_LITERAL_NULL:
				case Node.TYPE_LITERAL_NUMBER:
				case Node.TYPE_LITERAL_STRING:
					builder.append(print((Literal)node));
					break;
				case Node.TYPE_PARAM:
					builder.append(print((Param)node));
					break;
				case Node.TYPE_SELECT:
					builder.append(print((Select)node, true));
					break;
				case Node.TYPE_BINARY_OPERATOR:
				case Node.TYPE_TOKEN:
					builder.append(print((Token)node));
					break;
			}
		}
		
		if (subExpression) {
			builder.append(")");
		}
		appendAsKeyword(builder, expression);
		return builder.toString();
	}
	
	@Override
	public String print(Fetch fetch) {
		StringBuilder builder = new StringBuilder()
				.append(Keywords.FETCH)
				.append(" ")
				.append(Keywords.FIRST)
				.append(" ")
				.append(fetch.getFetchCount())
				.append(" ")
				.append(Keywords.ROWS)
				.append(" ")
				.append(Keywords.ONLY);
		return builder.toString();
	}

	@Override
	public String print(From from) {
		StringBuilder builder = new StringBuilder(Keywords.FROM);
		Node prevNode = null;
		
		for (Node node : from.getNodes()) {
			String str = "";
			
			switch (node.getType()) {
				case Node.TYPE_CONDITION:
					builder.append(" ").append(Keywords.ON);
					str = print((Condition)node);
					break;
				case Node.TYPE_SELECT:
					str = print((Select)node, true);
					break;
				case Node.TYPE_TABLE:
					str = print((Table)node);
					break;
				case Node.TYPE_TOKEN:
					str = print((Token)node);
					break;
			}
			
			if (node instanceof Table 
					&& prevNode != null 
					&& !(prevNode instanceof Join)) {
				builder.append(",");
			}
			builder.append(" ");
			builder.append(str);
			prevNode = node;
		}
		
		return builder.toString();
	}
	
	@Override
	public String print(Function function) {
		StringBuilder builder = new StringBuilder();
		builder.append(function.getName()).append("(");
		
		List<Node> nodes = function.getNodes();
		
		for (int i = 0; i < nodes.size(); i++) {
			Node node = nodes.get(i);
			
			if (i > 0) {
				builder.append(", ");
			}
			
			switch (node.getType()) {
				case Node.TYPE_CASE:
					builder.append(print((Case)node));
					break;
				case Node.TYPE_COLUMN:
					builder.append(print((Column)node));
					break;
				case Node.TYPE_EXPRESSION:
					appendExpression(builder, (Expression)node);
					break;
				case Node.TYPE_FUNCTION:
					builder.append(print((Function)node));
					break;
				case Node.TYPE_LITERAL_BOOLEAN:
				case Node.TYPE_LITERAL_NULL:
				case Node.TYPE_LITERAL_NUMBER:
				case Node.TYPE_LITERAL_STRING:
					builder.append(print((Literal)node));
					break;
				case Node.TYPE_PARAM:
					builder.append(print((Param)node));
					break;
				case Node.TYPE_SELECT:
					builder.append(print((Select)node, true));
					break;
				case Node.TYPE_TOKEN:
					builder.append(print((Token)node));
					break;
			}
		}
		
		builder.append(")");
		appendAsKeyword(builder, function);
		return builder.toString();
	}

	@Override
	public String print(GroupBy groupBy) {
		StringBuilder builder = new StringBuilder(Keywords.GROUP_BY);
		
		List<Node> nodes = groupBy.getNodes();
		for (int i = 0; i < nodes.size(); i++) {
			Node node = nodes.get(i);
			
			if (i > 0) {
				builder.append(",");
			}
			builder.append(" ");
			builder.append(print((Column)node));
		}
		
		return builder.toString();
	}
	
	@Override
	public String print(Insert insert) {
		StringBuilder builder = new StringBuilder();

		beginWithClause(builder, insert);
		builder.append(Keywords.INSERT);
		
		boolean rowValueConstructors = false;
		
		for (Node node : insert.getNodes()) {
			switch (node.getType()) {
				case Node.TYPE_COLUMN_LIST:
					builder.append(" ").append(print((ColumnList)node));
					break;
				case Node.TYPE_COLUMN_VALUES:
					if (rowValueConstructors) {
						builder.append(", ").append(print((ColumnValues)node));
					} else {
						builder.append(" ")
								.append(Keywords.VALUES)
								.append(" ")
								.append(print((ColumnValues)node));
						rowValueConstructors = true;
					}
					break;
				case Node.TYPE_SELECT:
					builder.append(" ").append(print((Select)node));
					break;
				case Node.TYPE_TABLE:
					builder.append(" ")
							.append(Keywords.INTO)
							.append(" ")
							.append(print((Table)node));
					break;
			}
		}
		
		return builder.toString();
	}
	
	@Override
	public String print(InValues inValues) {
		StringBuilder builder = new StringBuilder()
				.append("(")
				.append(printCommaSeparated(inValues))
				.append(")");
		return builder.toString();
	}
	
	@Override
	public String print(Literal literal) {
		StringBuilder builder = new StringBuilder();
		switch (literal.getType()) {
			case Node.TYPE_LITERAL_BOOLEAN:
				builder.append((boolean)literal.getValue() ? Keywords.TRUE : Keywords.FALSE);
				break;
			case Node.TYPE_LITERAL_NULL:
				builder.append(Keywords.NULL);
				break;
			case Node.TYPE_LITERAL_NUMBER:
				builder.append(literal.getValue().toString());
				break;
			case Node.TYPE_LITERAL_STRING:
				builder.append(SqlStringUtils.createLiteralToken((String)literal.getValue(), false));
				break;
		}
		appendAsKeyword(builder, literal);
		return builder.toString();
	}
	
	// Not called by any other methods in here
	@Override
	public String print(Node node) {
		switch (node.getType()) {
			case Node.TYPE_CASE:
				return print((Case)node);
			case Node.TYPE_COLUMN:
				return print((Column)node);
			case Node.TYPE_COLUMN_LIST:
				return print((ColumnList)node);
			case Node.TYPE_COLUMN_VALUES:
				return print((ColumnValues)node);
			case Node.TYPE_COMMON_TABLE_EXPRESSION:
				return print((CommonTableExpression)node);
			case Node.TYPE_HAVING:
			case Node.TYPE_WHERE:
			case Node.TYPE_CONDITION:
				return print((Condition)node);
			case Node.TYPE_DELETE:
				return print((Delete)node);
			case Node.TYPE_EXPRESSION:
				return print((Expression)node);
			case Node.TYPE_FETCH:
				return print((Fetch)node);
			case Node.TYPE_FROM:
				return print((From)node);
			case Node.TYPE_FUNCTION:
				return print((Function)node);
			case Node.TYPE_GROUP_BY:
				return print((GroupBy)node);
			case Node.TYPE_IN_VALUES:
				return print((InValues)node);
			case Node.TYPE_INSERT:
				return print((Insert)node);
			case Node.TYPE_LITERAL_BOOLEAN:
			case Node.TYPE_LITERAL_NULL:
			case Node.TYPE_LITERAL_NUMBER:
			case Node.TYPE_LITERAL_STRING:
				return print((Literal)node);
			case Node.TYPE_OFFSET:
				return print((Offset)node);
			case Node.TYPE_ORDER_BY:
				return print((OrderBy)node);
			case Node.TYPE_PARAM:
				return print((Param)node);
			case Node.TYPE_PREDICATE:
				return print((Predicate)node);
			case Node.TYPE_SELECT:
				return print((Select)node);
			case Node.TYPE_SELECT_LIST:
				return print((SelectList)node);
			case Node.TYPE_SET_OPERATOR:
				return print((SetOperator)node);
			case Node.TYPE_TABLE:
				return print((Table)node);
			case Node.TYPE_BINARY_OPERATOR:
			case Node.TYPE_ORDER_BY_SORT:
			case Node.TYPE_TOKEN:
				return print((Token)node);
			case Node.TYPE_UPDATE:
				return print((Update)node);
			case Node.TYPE_WITH:
				return print((With)node);
		}
		return null;
	}

	@Override
	public String print(Offset offset) {
		StringBuilder builder = new StringBuilder()
				.append(Keywords.OFFSET)
				.append(" ")
				.append(offset.getSkipCount())
				.append(" ")
				.append(Keywords.ROWS);
		return builder.toString();
	}
	
	@Override
	public String print(OrderBy orderBy) {
		StringBuilder builder = new StringBuilder(Keywords.ORDER_BY);
		
		List<Node> nodes = orderBy.getNodes();
		for (int i = 0; i < nodes.size(); i++) {
			Node node = nodes.get(i);

			boolean appendComma = true;
			String str = "";
			
			switch (node.getType()) {
				case Node.TYPE_COLUMN:
					str = print((Column)node);
					break;
				case Node.TYPE_FETCH:
					appendComma = false;
					str = print((Fetch)node);
					break;
				case Node.TYPE_OFFSET:
					appendComma = false;
					str = print((Offset)node);
					break;
				case Node.TYPE_ORDER_BY_SORT:
					appendComma = false;
					// Fall through to TYPE_TOKEN
				case Node.TYPE_TOKEN:
					str = print((Token)node);
					break;
			}
			
			if (i > 0 && appendComma) {
				builder.append(",");
			}
			builder.append(" ");
			builder.append(str);
		}
		
		return builder.toString();
	}
	
	@Override
	public String print(Param param) {
		StringBuilder builder = new StringBuilder();
		builder.append(":" + param.getName());
		appendAsKeyword(builder, param);
		return builder.toString();
	}

	@Override
	public String print(Predicate predicate) {
		StringBuilder builder = new StringBuilder();
		
		for (Node node : predicate.getNodes()) {
			if (builder.length() > 0) {
				builder.append(" ");
			}
			
			switch (node.getType()) {
				case Node.TYPE_CASE:
					builder.append(print((Case)node));
					break;
				case Node.TYPE_COLUMN:
					builder.append(print((Column)node));
					break;
				case Node.TYPE_EXPRESSION:
					appendExpression(builder, (Expression)node);
					break;
				case Node.TYPE_FUNCTION:
					builder.append(print((Function)node));
					break;
				case Node.TYPE_IN_VALUES:
					builder.append(print((InValues)node));
					break;
				case Node.TYPE_LITERAL_BOOLEAN:
				case Node.TYPE_LITERAL_NULL:
				case Node.TYPE_LITERAL_NUMBER:
				case Node.TYPE_LITERAL_STRING:
					builder.append(print((Literal)node));
					break;
				case Node.TYPE_PARAM:
					builder.append(print((Param)node));
					break;
				case Node.TYPE_SELECT:
					builder.append(print((Select)node, true));
					break;
				case Node.TYPE_TOKEN:
					builder.append(print((Token)node));
					break;
			}
		}
		
		return builder.toString();
	}
	
	@Override
	public String print(Select select) {
		return print(select, false);
	}
	
	@Override
	public String print(Select select, final boolean subquery) {
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
				case Node.TYPE_FROM:
					builder.append(" ").append(print((From)node));
					break;
				case Node.TYPE_GROUP_BY:
					builder.append(" ").append(print((GroupBy)node));
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
	public String print(SelectList list) {
		return printCommaSeparated(list);
	}
	
	@Override
	public String print(SetOperator operator) {
		StringBuilder builder = new StringBuilder(operator.getOperator());
		if (operator.getSelect() != null) {
			builder.append(" ").append(print(operator.getSelect(), true));
		}
		return builder.toString();
	}
	
	@Override
	public String print(SetValue value) {
		StringBuilder builder = new StringBuilder();
		
		for (Node node : value.getNodes()) {
			switch (node.getType()) {
				case Node.TYPE_CASE:
					builder.append(print((Case)node));
					break;
				case Node.TYPE_COLUMN:
					builder.append(print((Column)node))
							.append(" = ");
					break;
				case Node.TYPE_EXPRESSION:
					appendExpression(builder, (Expression)node);
					break;
				case Node.TYPE_FUNCTION:
					builder.append(print((Function)node));
					break;
				case Node.TYPE_LITERAL_BOOLEAN:
				case Node.TYPE_LITERAL_NULL:
				case Node.TYPE_LITERAL_NUMBER:
				case Node.TYPE_LITERAL_STRING:
					builder.append(print((Literal)node));
					break;
				case Node.TYPE_PARAM:
					builder.append(print((Param)node));
					break;
				case Node.TYPE_SELECT:
					builder.append(print((Select)node, true));
					break;
				case Node.TYPE_BINARY_OPERATOR:
				case Node.TYPE_TOKEN:
					builder.append(print((Token)node));
					break;
			}
		}
		return builder.toString();
	}
	
	@Override
	public String print(SetValues values) {
		StringBuilder builder = new StringBuilder();
		for (Node node : values.getNodes()) {
			if (builder.length() > 0) {
				builder.append(", ");
			}
			builder.append(print((SetValue)node));
		}
		return builder.toString();
	}

	@Override
	public String print(Table table) {
		StringBuilder builder = new StringBuilder();
		
		if (!Strings.isNullOrEmpty(table.getName())) {
			builder.append(table.getName());
		}
		if (!Strings.isNullOrEmpty(table.getAlias())) {
			if (builder.length() > 0) {
				builder.append(" ");
			}
			
			builder.append(table.getAlias());
		}
		
		return builder.toString();
	}
	
	@Override
	public String print(Token token) {
		return token.getToken();
	}
	
	@Override
	public String print(Update update) {
		StringBuilder builder = new StringBuilder();
		
		beginWithClause(builder, update);
		builder.append(Keywords.UPDATE);
		
		for (Node node : update.getNodes()) {
			switch (node.getType()) {
				case Node.TYPE_SET_VALUES:
					builder.append(" ")
							.append(Keywords.SET)
							.append(" ")
							.append(print((SetValues)node));
					break;
				case Node.TYPE_TABLE:
					builder.append(" ").append(print((Table)node));
					break;
				case Node.TYPE_WHERE:
					builder.append(" ")
							.append(Keywords.WHERE)
							.append(" ")
							.append(print((Where)node));
					break;
			}
		}
		return builder.toString();
	}
	
	@Override
	public String print(With with) {
		StringBuilder builder = new StringBuilder(Keywords.WITH);
		
		List<Node> nodes = with.getNodes();
		for (int i = 0; i < nodes.size(); i++) {
			Node node = nodes.get(i);
			
			if (i > 0) {
				builder.append(",");
			}
			
			switch (node.getType()) {
				case Node.TYPE_COMMON_TABLE_EXPRESSION:
					builder.append(" ").append(print((CommonTableExpression)node));
					break;
			}
		}
		
		return builder.toString();
	}
	
	/* END Vendor interface methods */
	
	/* BEGIN Protected methods */
	
	protected void appendExpression(StringBuilder builder, Expression expression) {
		appendExpression(builder, expression, false);
	}
	
	protected void appendExpression(StringBuilder builder, Expression expression, boolean subExpression) {
		builder.append(print(expression, subExpression));
	}
	
	protected void beginWithClause(StringBuilder builder, NodeHolder holder) {
		Node first = holder.getNodes().get(0);
		boolean foundCte = first.getType() == Node.TYPE_WITH;
		if (foundCte) {
			builder.append(print((With)first)).append(" ");
		}
	}
	
	protected <T> String printCommaSeparated(ExpressionBuilder<T> expressionBuilder) {
		boolean appendComma = true;
		StringBuilder builder = new StringBuilder();
		
		for (Node node : expressionBuilder.getNodes()) {
			if (builder.length() > 0) {
				if (appendComma) {
					builder.append(",");
				}
				builder.append(" ");
			}
			appendComma = true;
			
			switch (node.getType()) {
				case Node.TYPE_CASE:
					builder.append(print((Case)node));
					break;
				case Node.TYPE_COLUMN:
					builder.append(print((Column)node));
					break;
				case Node.TYPE_EXPRESSION:
					appendExpression(builder, (Expression)node);
					break;
				case Node.TYPE_FUNCTION:
					builder.append(print((Function)node));
					break;
				case Node.TYPE_LITERAL_BOOLEAN:
				case Node.TYPE_LITERAL_NULL:
				case Node.TYPE_LITERAL_NUMBER:
				case Node.TYPE_LITERAL_STRING:
					builder.append(print((Literal)node));
					break;
				case Node.TYPE_PARAM:
					builder.append(print((Param)node));
					break;
				case Node.TYPE_SELECT:
					builder.append(print((Select)node, true));
					break;
				case Node.TYPE_TOKEN:
					builder.append(print((Token)node));
					appendComma = !((Token)node).isNextNodeAnExpression();
					break;
			}
		}
		return builder.toString();
	}
	
	/* END Protected methods */
}