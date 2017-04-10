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

import java.util.Objects;

public class Predicate implements BooleanExpression {
	private final Predicand leftPredicand;
	private final PredicateOperator operator;
	private final Predicand rightPredicand;

	public Predicand getLeftPredicand() {
		return leftPredicand;
	}
	public PredicateOperator getOperator() {
		return operator;
	}
	public Predicand getRightPredicand() {
		return rightPredicand;
	}
	
	Predicate(Predicand leftPredicand, PredicateOperator operator) {
		this(leftPredicand, null, operator);
	}

	Predicate(Predicand leftPredicand, Predicand rightPredicand, PredicateOperator operator) {
		this.leftPredicand = Objects.requireNonNull(leftPredicand, "leftPredicand cannot be null");
		this.operator = Objects.requireNonNull(operator, "operator cannot be null");
		this.rightPredicand = rightPredicand;
	}
}
