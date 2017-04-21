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

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

import com.github.javalbert.utils.collections.ArrayUtils;
import com.github.javalbert.utils.collections.CollectionUtils;

public class InPredicate extends Predicate {
	private static List<ValueExpression> valueList(Number...values) {
		List<ValueExpression> valueList = new ArrayList<>();
		for (Number value : values) {
			valueList.add(DSL.literal(value));
		}
		return valueList;
	}
	
	private static List<ValueExpression> valueList(String...values) {
		List<ValueExpression> valueList = new ArrayList<>();
		for (String value : values) {
			valueList.add(DSL.literal(value));
		}
		return valueList;
	}
	
	private final List<ValueExpression> values;
	
	@Override
	public int getNodeType() {
		return TYPE_PREDICATE_IN;
	}
	public List<ValueExpression> getValues() {
		return values;
	}
	
	InPredicate(Predicand predicand, ValueExpression...values) {
		this(predicand, false, values);
	}
	
	InPredicate(Predicand predicand, boolean negate, ValueExpression...values) {
		this(
				predicand,
				Arrays.asList(
						ArrayUtils.illegalArgOnEmpty(values, "values cannot be null or empty")
						),
				negate);
	}

	InPredicate(Predicand predicand, Number...values) {
		this(predicand, valueList(values));
	}
	InPredicate(Predicand predicand, String...values) {
		this(predicand, valueList(values));
	}
	
	InPredicate(Predicand predicand, List<ValueExpression> values) {
		this(predicand, values, false);
	}
	
	InPredicate(Predicand predicand, boolean negate, Number...values) {
		this(predicand, valueList(values), true);
	}
	InPredicate(Predicand predicand, boolean negate, String...values) {
		this(predicand, valueList(values), true);
	}
	
	InPredicate(Predicand predicand, List<ValueExpression> values, boolean negate) {
		super(predicand, negate ? PredicateOperator.NOT_IN : PredicateOperator.IN);
		this.values = CollectionUtils.immutableArrayList(
				CollectionUtils.illegalArgOnEmpty(values, "values cannot be null or empty"));
	}
}
