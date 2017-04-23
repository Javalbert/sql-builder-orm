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

import java.util.Collections;
import java.util.List;

import com.github.javalbert.utils.collections.CollectionUtils;
import com.github.javalbert.utils.string.Strings;

public class Function
implements ExpressionBuilder, Predicand, SelectColumn<Function>, ValueExpression {
	private String alias;
	private final String name;
	private List<ValueExpression> parameters = Collections.emptyList();
	
	@Override
	public String getAlias() {
		return alias;
	}
	public String getName() {
		return name;
	}
	@Override
	public int getNodeType() {
		return NODE_FUNCTION;
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
	
	public Function call() {
		Function function = copy();
		function.parameters = Collections.emptyList();
		return function;
	}
	
	public Function call(ValueExpression parameter) {
		Function function = copy();
		function.parameters = Collections.singletonList(parameter);
		return function;
	}
	
	public Function call(ValueExpression...parameters) {
		Function function = copy();
		function.parameters = CollectionUtils.immutableArrayList(parameters);
		return function;
	}
	
	Function copy() {
		Function copy = new Function(name);
		copy.alias = alias;
		copy.parameters = parameters;
		return copy;
	}
}
