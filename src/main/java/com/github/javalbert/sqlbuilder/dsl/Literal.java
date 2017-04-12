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

/**
 * 
 * @author Albert
 *
 * @param <I> the class extending {@code Literal}
 * @param <T> the type of the literal
 */
public abstract class Literal<I, T>
implements ExpressionBuilder, Predicand, SelectColumn<I>, ValueExpression {
	protected String alias;
	protected T value;
	
	protected Literal(T value2) {
		this.value = Objects.requireNonNull(value, "value cannot be null");
	}
	
	@Override
	public String getAlias() {
		return alias;
	}
	public T getValue() {
		return value;
	}
	
	abstract I copy();
}
