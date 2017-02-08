/*******************************************************************************
 * Copyright 2016 Albert Shun-Dat Chan
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
package com.github.javalbert.sqlbuilder;

import com.github.javalbert.utils.string.Strings;

public class Function extends ExpressionBuilder<Function> implements Aliasable, Node<Function> {
	public static final int UNKNOWN_ARGUMENTS = -1;
	
	protected String alias;
	private int maxArguments;
	private String name;

	@Override
	public String getAlias() { return alias; }
	@Override
	public void setAlias(String alias) {
		this.alias = Strings.safeTrim(alias);
	}
	public int getMaxArguments() { return maxArguments; }
	public void setMaxArguments(int maxArguments) { this.maxArguments = maxArguments; }
	public String getName() { return name; }
	public void setName(String name) {
		this.name = Strings.safeTrim(name);
	}
	@Override
	public int getType() { return TYPE_FUNCTION; }
	
	public Function(Function function) {
		this(function.getName(), function.getMaxArguments());
		alias = function.getAlias();
		nodes = NodeUtils.mutableNodes(function);
	}
	
	public Function(String name) {
		this(name, UNKNOWN_ARGUMENTS);
	}
	
	public Function(String name, int maxArguments) {
		this.maxArguments = maxArguments;
		this.name = Strings.safeTrim(name);
	}
	
	@Override
	public boolean accept(NodeVisitor visitor) {
		return NodeUtils.visit(this, nodes, visitor);
	}
	
	@Override
	public Function immutable() {
		Function function = new ImmutableFunction(this);
		return function;
	}
	
	@Override
	public Function mutable() {
		Function function = new Function(this);
		return function;
	}
}