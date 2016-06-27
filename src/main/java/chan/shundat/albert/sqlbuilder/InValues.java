/*******************************************************************************
 * Copyright (c) 2016 Albert Shun-Dat Chan
 *
 * Licensed under MIT License
 * See LICENSE file at the root of the project or
 * https://github.com/Javalbert/sql-builder-orm/blob/master/LICENSE
 *******************************************************************************/
package chan.shundat.albert.sqlbuilder;

import java.math.BigDecimal;
import java.util.Collection;

public class InValues extends ExpressionBuilder<InValues> implements Node<InValues> {
	@Override
	public int getType() { return TYPE_IN_VALUES; }

	public InValues() {}
	
	public InValues(InValues values) {
		nodes = NodeUtils.mutableNodes(values);
	}
	
	@Override
	public boolean accept(NodeVisitor visitor) {
		return NodeUtils.visit(this, nodes, visitor);
	}

	@Override
	public InValues immutable() {
		InValues values = new ImmutableInValues(this);
		return values;
	}

	@Override
	public InValues mutable() {
		InValues values = new InValues(this);
		return values;
	}

	/* BEGIN Fluent API */
	
	public InValues bigDecimals(Collection<BigDecimal> numberLiterals) {
		for (BigDecimal numberLiteral : numberLiterals) {
			literal(numberLiteral);
		}
		return this;
	}
	
	public InValues doubles(Collection<Double> numberLiterals) {
		for (Double numberLiteral : numberLiterals) {
			literal(numberLiteral);
		}
		return this;
	}
	
	public InValues floats(Collection<Float> numberLiterals) {
		for (Float numberLiteral : numberLiterals) {
			literal(numberLiteral);
		}
		return this;
	}
	
	public InValues integers(Collection<Integer> numberLiterals) {
		for (Integer numberLiteral : numberLiterals) {
			literal(numberLiteral);
		}
		return this;
	}
	
	public InValues longs(Collection<Long> numberLiterals) {
		for (Long numberLiteral : numberLiterals) {
			literal(numberLiteral);
		}
		return this;
	}
	
	public InValues strings(Collection<String> stringLiterals) {
		for (String stringLiteral : stringLiterals) {
			literal(stringLiteral);
		}
		return this;
	}
	
	@Override
	public InValues subquery(Select select) {
		throw new UnsupportedOperationException("call Predicate.in(Select) instead of Predicate.in(InValues.subquery(Select)) for subqueries");
	}
	
	/* END Fluent API */
}