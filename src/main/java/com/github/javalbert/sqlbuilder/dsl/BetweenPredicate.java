/*******************************************************************************
 * Copyright 2017 Albert Shun-Dat Chan
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
package com.github.javalbert.sqlbuilder.dsl;

public class BetweenPredicate extends Predicate {
	private final ValueExpression value1;
	private final ValueExpression value2;

	public ValueExpression getValue1() {
		return value1;
	}
	public ValueExpression getValue2() {
		return value2;
	}
	
	BetweenPredicate(
			Predicand predicand,
			ValueExpression value1,
			ValueExpression value2) {
		this(predicand, value1, value2, false);
	}
	
	BetweenPredicate(
			Predicand predicand,
			ValueExpression value1,
			ValueExpression value2,
			boolean negate) {
		super(predicand, null, negate ? PredicateOperator.NOT_BETWEEN : PredicateOperator.BETWEEN);
		this.value1 = ValueExpression.ifNull(value1);
		this.value2 = ValueExpression.ifNull(value2);
	}
}
