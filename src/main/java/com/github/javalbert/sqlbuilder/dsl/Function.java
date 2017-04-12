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

import com.github.javalbert.utils.string.Strings;

public class Function
implements ExpressionBuilder, Predicand, SelectColumn<Function>, ValueExpression {
	private String alias;
	private final String name;
	@SuppressWarnings("unchecked")
	private List<ValueExpression> parameters = Collections.EMPTY_LIST;
	
	@Override
	public String getAlias() {
		return alias;
	}
	public String getName() {
		return name;
	}
	public List<ValueExpression> getParameters() {
		return parameters;
	}
	
	public Function(String name) {
		this.name = Strings.illegalArgOnEmpty(name, "name cannot be null or empty");
	}

	@Override
	public Function as(String alias) {
		Function function = copy();
		function.alias = alias;
		return function;
	}
	
	@SuppressWarnings("unchecked")
	public Function call() {
		Function function = copy();
		function.parameters = Collections.EMPTY_LIST;
		return function;
	}
	
	public Function call(ValueExpression parameter) {
		Function function = copy();
		function.parameters = Collections.singletonList(parameter);
		return function;
	}
	
	public Function call(ValueExpression...parameters) {
		Function function = copy();
		function.parameters = Collections.unmodifiableList(Arrays.asList(parameters));
		return function;
	}
	
	Function copy() {
		Function copy = new Function(name);
		copy.alias = alias;
		copy.parameters = parameters;
		return copy;
	}
}
