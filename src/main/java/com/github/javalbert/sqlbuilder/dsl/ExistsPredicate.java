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

public class ExistsPredicate extends Predicate {
	public SelectStatement getSubquery() {
		return (SelectStatement)getRightPredicand();
	}
	
	ExistsPredicate(SelectStatement subquery) {
		this(subquery, false);
	}
	
	ExistsPredicate(SelectStatement subquery, boolean negate) {
		super(
				null,
				Objects.requireNonNull(subquery, "subquery cannot be null"),
				negate ? PredicateOperator.NOT_EXISTS : PredicateOperator.EXISTS);
	}

}
