package chan.shundat.albert.sqlbuilder.vendor;

import java.util.List;

import chan.shundat.albert.sqlbuilder.BinaryOperator;
import chan.shundat.albert.sqlbuilder.Case;
import chan.shundat.albert.sqlbuilder.Column;
import chan.shundat.albert.sqlbuilder.Expression;
import chan.shundat.albert.sqlbuilder.Fetch;
import chan.shundat.albert.sqlbuilder.Function;
import chan.shundat.albert.sqlbuilder.Keywords;
import chan.shundat.albert.sqlbuilder.Literal;
import chan.shundat.albert.sqlbuilder.Node;
import chan.shundat.albert.sqlbuilder.Offset;
import chan.shundat.albert.sqlbuilder.OrderBy;
import chan.shundat.albert.sqlbuilder.Param;
import chan.shundat.albert.sqlbuilder.Select;
import chan.shundat.albert.sqlbuilder.Token;

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
	public String createTableIdentifier(chan.shundat.albert.orm.Table tableAnno) {
		StringBuilder identifier = new StringBuilder();
		
		if (tableAnno.catalog() != null) {
			identifier.append(tableAnno.catalog()).append(".");
		} else if (tableAnno.schema() != null) {
			identifier.append(tableAnno.schema()).append(".");
		}
		identifier.append(tableAnno.name());
		
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
	
	/**
	 * Can print LIMIT n OFFSET skip
	 */
	@Override
	public String print(OrderBy orderBy) {
		StringBuilder builder = new StringBuilder(Keywords.ORDER_BY);
		
		String offset = null;
		
		List<Node> nodes = orderBy.getNodes();
		for (int i = 0; i < nodes.size(); i++) {
			Node node = nodes.get(i);

			boolean appendComma = true;
			boolean foundLimit = false;
			String str = "";
			
			switch (node.getType()) {
				case Node.TYPE_COLUMN:
					str = print((Column)node);
					break;
				case Node.TYPE_FETCH:
					appendComma = false;
					foundLimit = true;
					str = print((Fetch)node);
					break;
				case Node.TYPE_OFFSET:
					appendComma = false;
					offset = print((Offset)node);
					continue;
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
			
			if (foundLimit && offset != null) {
				builder.append(str).append(" ").append(offset);
			} else {
				builder.append(str);
			}
		}
		
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