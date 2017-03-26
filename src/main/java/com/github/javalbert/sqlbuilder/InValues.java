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
package com.github.javalbert.sqlbuilder;

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
		return new ImmutableInValues(this);
	}

	@Override
	public InValues mutable() {
		return new InValues(this);
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