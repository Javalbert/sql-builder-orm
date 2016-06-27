/*******************************************************************************
 * Copyright (c) 2016 Albert Shun-Dat Chan
 *
 * Licensed under MIT License
 * See LICENSE file at the root of the project or
 * https://github.com/Javalbert/sql-builder-orm/blob/master/LICENSE
 *******************************************************************************/
package chan.shundat.albert.sqlbuilder;

/**
 * AKA Row Value Constructor
 * @author Albert
 *
 */
public class ColumnValues extends ExpressionBuilder<ColumnValues> implements Node<ColumnValues> {
	public static final Token DEFAULT = new ConstantToken(Keywords.DEFAULT, false);
	
	@Override
	public int getType() { return TYPE_COLUMN_VALUES; }

	public ColumnValues() {}
	
	public ColumnValues(ColumnValues values) {
		nodes = NodeUtils.mutableNodes(values);
	}
	
	@Override
	public boolean accept(NodeVisitor visitor) {
		return NodeUtils.visit(this, nodes, visitor);
	}

	@Override
	public ColumnValues immutable() {
		ColumnValues values = new ImmutableColumnValues(this);
		return values;
	}

	@Override
	public ColumnValues mutable() {
		ColumnValues values = new ColumnValues(this);
		return values;
	}
	
	/* BEGIN Fluent API */
	
	@Override
	public ColumnValues append(String token) {
		return super.append(token);
	}
	
	@Override
	public ColumnValues append(String token, boolean isNextNodeAnExpression) {
		return super.append(token, isNextNodeAnExpression);
	}
	
	@Override
	public ColumnValues column(String name) {
		return super.column(name);
	}
	
	@Override
	public ColumnValues expression(Expression expression) {
		return super.expression(expression);
	}
	
	@Override
	public ColumnValues function(Function function) {
		return super.function(function);
	}
	
	@Override
	public ColumnValues literal(Boolean bool) {
		return super.literal(bool);
	}
	
	@Override
	public ColumnValues literal(Number number) {
		return super.literal(number);
	}
	
	@Override
	public ColumnValues literal(String str) {
		return super.literal(str);
	}
	
	@Override
	public ColumnValues literalNull() {
		return super.literalNull();
	}
	
	@Override
	public ColumnValues param(String name) {
		return super.param(name);
	}
	
	@Override
	public ColumnValues sqlCase(Case sqlCase) {
		return super.sqlCase(sqlCase);
	}

	public ColumnValues sqlDefault() {
		nodes.add(DEFAULT);
		return this;
	}
	
	@Override
	public ColumnValues subquery(Select select) {
		return super.subquery(select);
	}
	
	@Override
	public ColumnValues tableAlias(String alias) {
		return super.tableAlias(alias);
	}
	
	@Override
	public ColumnValues tableName(String name) {
		return super.tableName(name);
	}
	
	/* END Fluent API */
}