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

public interface Predicand {
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
	
	default Predicate isNotNull() {
		return new Predicate(this, PredicateOperator.IS_NOT_NULL);
	}
	
	default Predicate isNull() {
		return new Predicate(this, PredicateOperator.IS_NULL);
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
}
