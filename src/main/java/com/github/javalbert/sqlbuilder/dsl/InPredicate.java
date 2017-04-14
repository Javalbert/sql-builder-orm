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

import java.util.Arrays;
import java.util.Collections;
import java.util.List;

import com.github.javalbert.utils.collections.ArrayUtils;
import com.github.javalbert.utils.collections.CollectionUtils;

public class InPredicate extends Predicate {
	private final List<ValueExpression> values;
	
	public List<ValueExpression> getValues() {
		return values;
	}
	
	InPredicate(Predicand predicand, ValueExpression...values) {
		this(predicand, false, values);
	}
	
	InPredicate(Predicand predicand, boolean negate, ValueExpression...values) {
		this(
				predicand,
				Collections.unmodifiableList(Arrays.asList(
						ArrayUtils.illegalArgOnEmpty(values, "values cannot be null or empty")
						)),
				negate);
	}

	InPredicate(Predicand predicand, List<ValueExpression> values) {
		this(predicand, values, false);
	}
	
	InPredicate(Predicand predicand, List<ValueExpression> values, boolean negate) {
		super(predicand, negate ? PredicateOperator.NOT_IN : PredicateOperator.IN);
		this.values = CollectionUtils.illegalArgOnEmpty(values, "values cannot be null or empty");
	}
}
