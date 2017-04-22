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

public interface Predicand extends DSLNode {
	public static <T extends Literal<? extends U, ? extends K>, U, K>
	Predicand ifNullLiteral(T predicand) {
		return predicand != null ? predicand : LiteralNull.INSTANCE;
	}
	
	default BetweenPredicate between(Number value1, Number value2) {
		return between(DSL.literal(value1), DSL.literal(value2));
	}
	default BetweenPredicate between(String value1, String value2) {
		return between(DSL.literal(value1), DSL.literal(value2));
	}
	
	default BetweenPredicate between(ValueExpression value1, ValueExpression value2) {
		return new BetweenPredicate(this, value1, value2);
	}
	
	default Predicate eq(Boolean value) {
		return eq(DSL.literal(value));
	}
	default Predicate eq(Number value) {
		return eq(DSL.literal(value));
	}
	default Predicate eq(String value) {
		return eq(DSL.literal(value));
	}
	
	default Predicate eq(Predicand predicand) {
		return new Predicate(this, predicand, PredicateOperator.EQ);
	}
	
	default Predicate gt(Number value) {
		return gt(DSL.literal(value));
	}
	default Predicate gt(String value) {
		return gt(DSL.literal(value));
	}
	
	default Predicate gt(Predicand predicand) {
		return new Predicate(this, predicand, PredicateOperator.GT);
	}
	
	default Predicate gteq(Number value) {
		return gteq(DSL.literal(value));
	}
	default Predicate gteq(String value) {
		return gteq(DSL.literal(value));
	}
	
	default Predicate gteq(Predicand predicand) {
		return new Predicate(this, predicand, PredicateOperator.GT_EQ);
	}
	
	default InPredicate in(Number...values) {
		return new InPredicate(this, values);
	}
	default InPredicate in(String...values) {
		return new InPredicate(this, values);
	}
	
	default InPredicate in(ValueExpression...values) {
		return new InPredicate(this, values);
	}
	
	default Predicate isNotNull() {
		return new Predicate(this, PredicateOperator.IS_NOT_NULL);
	}
	
	default Predicate isNull() {
		return new Predicate(this, PredicateOperator.IS_NULL);
	}
	
	default Predicate like(String value) {
		return like(DSL.literal(value));
	}
	
	default Predicate like(Predicand predicand) {
		return new Predicate(this, predicand, PredicateOperator.LIKE);
	}
	
	default Predicate lt(Number value) {
		return lt(DSL.literal(value));
	}
	default Predicate lt(String value) {
		return lt(DSL.literal(value));
	}
	
	default Predicate lt(Predicand predicand) {
		return new Predicate(this, predicand, PredicateOperator.LT);
	}

	default Predicate lteq(Number value) {
		return lteq(DSL.literal(value));
	}
	default Predicate lteq(String value) {
		return lteq(DSL.literal(value));
	}
	
	default Predicate lteq(Predicand predicand) {
		return new Predicate(this, predicand, PredicateOperator.LT_EQ);
	}
	
	default BetweenPredicate notBetween(Number value1, Number value2) {
		return notBetween(DSL.literal(value1), DSL.literal(value2));
	}
	default BetweenPredicate notBetween(String value1, String value2) {
		return notBetween(DSL.literal(value1), DSL.literal(value2));
	}
	
	default BetweenPredicate notBetween(ValueExpression value1, ValueExpression value2) {
		return new BetweenPredicate(this, value1, value2, true);
	}
	
	default Predicate noteq(Boolean value) {
		return noteq(DSL.literal(value));
	}
	default Predicate noteq(Number value) {
		return noteq(DSL.literal(value));
	}
	default Predicate noteq(String value) {
		return noteq(DSL.literal(value));
	}
	
	default Predicate noteq(Predicand predicand) {
		return new Predicate(this, predicand, PredicateOperator.NOT_EQ);
	}
	
	default InPredicate notIn(Number...values) {
		return new InPredicate(this, true, values);
	}
	default InPredicate notIn(String...values) {
		return new InPredicate(this, true, values);
	}
	
	default InPredicate notIn(ValueExpression...values) {
		return new InPredicate(this, true, values);
	}
	
	default Predicate notLike(String value) {
		return new Predicate(this, DSL.literal(value), PredicateOperator.NOT_LIKE);
	}
	
	default Predicate notLike(Predicand predicand) {
		return new Predicate(this, predicand, PredicateOperator.NOT_LIKE);
	}
}
