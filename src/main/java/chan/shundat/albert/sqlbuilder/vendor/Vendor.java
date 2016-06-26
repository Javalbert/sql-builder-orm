package chan.shundat.albert.sqlbuilder.vendor;

import chan.shundat.albert.sqlbuilder.Case;
import chan.shundat.albert.sqlbuilder.Column;
import chan.shundat.albert.sqlbuilder.ColumnList;
import chan.shundat.albert.sqlbuilder.ColumnValues;
import chan.shundat.albert.sqlbuilder.CommonTableExpression;
import chan.shundat.albert.sqlbuilder.Condition;
import chan.shundat.albert.sqlbuilder.Delete;
import chan.shundat.albert.sqlbuilder.Expression;
import chan.shundat.albert.sqlbuilder.Fetch;
import chan.shundat.albert.sqlbuilder.From;
import chan.shundat.albert.sqlbuilder.Function;
import chan.shundat.albert.sqlbuilder.GroupBy;
import chan.shundat.albert.sqlbuilder.InValues;
import chan.shundat.albert.sqlbuilder.Insert;
import chan.shundat.albert.sqlbuilder.Literal;
import chan.shundat.albert.sqlbuilder.Node;
import chan.shundat.albert.sqlbuilder.Offset;
import chan.shundat.albert.sqlbuilder.OrderBy;
import chan.shundat.albert.sqlbuilder.Param;
import chan.shundat.albert.sqlbuilder.Predicate;
import chan.shundat.albert.sqlbuilder.Select;
import chan.shundat.albert.sqlbuilder.SelectList;
import chan.shundat.albert.sqlbuilder.SetOperator;
import chan.shundat.albert.sqlbuilder.SetValue;
import chan.shundat.albert.sqlbuilder.SetValues;
import chan.shundat.albert.sqlbuilder.Table;
import chan.shundat.albert.sqlbuilder.Token;
import chan.shundat.albert.sqlbuilder.Update;
import chan.shundat.albert.sqlbuilder.With;

@SuppressWarnings("rawtypes")
public interface Vendor {
	String createTableIdentifier(chan.shundat.albert.orm.Table tableAnno);
	
	String print(Case sqlCase);
	String print(Column column);
	String print(ColumnList columns);
	String print(ColumnValues values);
	String print(CommonTableExpression cte);
	String print(Condition condition);
	String print(Condition condition, boolean group);
	String print(Delete delete);
	String print(Expression expression);
	String print(Expression expression, boolean subExpression);
	String print(Fetch fetch);
	String print(From from);
	String print(Function function);
	String print(GroupBy groupBy);
	String print(Insert insert);
	String print(InValues inValues);
	String print(Literal literal);
	String print(Node node);
	String print(Offset offset);
	String print(OrderBy orderBy);
	String print(Param param);
	String print(Predicate predicate);
	String print(Select select);
	String print(Select select, boolean subquery);
	String print(SelectList list);
	String print(SetOperator operator);
	String print(SetValue value);
	String print(SetValues values);
	String print(Table table);
	String print(Token token);
	String print(Update update);
	String print(With with);
}