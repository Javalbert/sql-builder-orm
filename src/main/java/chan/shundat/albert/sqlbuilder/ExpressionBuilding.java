package chan.shundat.albert.sqlbuilder;

public interface ExpressionBuilding<T> extends ColumnBuilder<T> {
	T append(String token);
	T append(String token, boolean isNextNodeAnExpression);
	T expression(Expression expression);
	T function(Function function);
	T literal(Boolean bool);
	T literal(Number number);
	/**
	 * WARN: Strong possibility of SQL injection attacks. See <code>http://stackoverflow.com/q/5741187</code>.
	 * <br><code>str</code> parameter value is surrounded by single quotes and single quotes in <code>str</code> 
	 * are replaced by two (2) single quotes. See <code>chan.shundat.albert.sqlbuilder.vendor</code> package classes.
	 * @param str
	 * @return
	 */
	T literal(String str);
	T literalNull();
	/**
	 * 
	 * @param name name of parameter, should satisfy the regex :\w+
	 * @return
	 */
	T param(String name);
	/**
	 * SQL CASE statement<br>
	 * couldn't name the method case because it is a reserved word in Java for the switch statement
	 * @param sqlCase
	 * @return
	 */
	T sqlCase(Case sqlCase);
	T subquery(Select select);
}