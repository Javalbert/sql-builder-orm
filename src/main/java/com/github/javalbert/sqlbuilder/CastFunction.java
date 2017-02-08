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

@SuppressWarnings("rawtypes")
public class CastFunction extends Function {
	public CastFunction() {
		super(Keywords.CAST, 1);
	}
	
	/* BEGIN Fluent API */
	
	@Override
	public CastFunction append(String token) {
		super.append(token);
		return this;
	}
	
	@Override
	public CastFunction append(String token, boolean isNextNodeAnExpression) {
		super.append(token, isNextNodeAnExpression);
		return this;
	}
	
	public CastFunction as(String dataType) {
		Node node = !nodes.isEmpty() ? nodes.get(nodes.size() - 1) : null;
		
		if (node == null) {
			throw new IllegalStateException("No nodes");
		} else if (node instanceof Aliasable) {
			Aliasable aliasable = (Aliasable)node;
			aliasable.setAlias(dataType);
		}
		return this;
	}
	
	@Override
	public CastFunction column(String name) {
		super.column(name);
		return this;
	}
	
	@Override
	public CastFunction function(Function function) {
		super.function(function);
		return this;
	}
	
	@Override
	public CastFunction param(String name) {
		super.param(name);
		return this;
	}
	
	@Override
	public CastFunction sqlCase(Case sqlCase) {
		super.sqlCase(sqlCase);
		return this;
	}
	
	@Override
	public CastFunction subquery(Select select) {
		super.subquery(select);
		return this;
	}
	
	@Override
	public CastFunction tableAlias(String alias) {
		super.tableAlias(alias);
		return this;
	}
	
	@Override
	public CastFunction tableName(String name) {
		super.tableName(name);
		return this;
	}
	
	/* END Fluent API */
}