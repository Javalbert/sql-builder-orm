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

public class When {
	public static When search(BooleanExpression booleanExpression) {
		return new When(Objects.requireNonNull(booleanExpression,
				"boolean expression cannot be null"), null);
	}
	
	public static When simple(ValueExpression simpleExpression) {
		return new When(null, simpleExpression != null ? simpleExpression : LiteralNull.INSTANCE);
	}
	
	private final BooleanExpression booleanExpression;
	private final ValueExpression simpleExpression;
	private ValueExpression then;
	
	public BooleanExpression getBooleanExpression() {
		return booleanExpression;
	}
	public ValueExpression getSimpleExpression() {
		return simpleExpression;
	}
	public ValueExpression getThen() {
		return then;
	}

	private When(BooleanExpression booleanExpression, ValueExpression simpleExpression) {
		this.booleanExpression = booleanExpression;
		this.simpleExpression = simpleExpression;
	}
	
	When then(ValueExpression then) {
		When when = copy();
		when.then = then;
		return when;
	}
	
	When copy() {
		When copy = new When(booleanExpression, simpleExpression);
		copy.then = then;
		return copy;
	}
}
