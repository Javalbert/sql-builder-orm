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

import java.util.ArrayList;
import java.util.List;
import java.util.Objects;

@SuppressWarnings({ "rawtypes", "unchecked" })
public abstract class ExpressionBuilder<T> implements ExpressionBuilding<T>, NodeHolder {
	protected List<Node> nodes = new ArrayList<>();
	protected Column workColumn;
	
	@Override
	public List<Node> getNodes() { return nodes; }

	/* BEGIN Fluent API */

	@Override
	public T append(String token) {
		return append(token, true);
	}
	
	@Override
	public T append(String token, boolean isNextNodeAnExpression) {
		nodes.add(new Token(token, isNextNodeAnExpression));
		return (T)this;
	}

	@Override
	public T column(String name) {
		NodeUtils.addColumn(this, workColumn, name);
		workColumn = null;
		return (T)this;
	}

	@Override
	public T expression(Expression expression) {
		nodes.add(Objects.requireNonNull(expression, "expression cannot be null"));
		return (T)this;
	}
	
	@Override
	public T function(Function function) {
		nodes.add(Objects.requireNonNull(function, "function cannot be null"));
		return (T)this;
	}
	
	@Override
	public T literal(Boolean bool) {
		nodes.add(bool != null ? new LiteralBoolean(bool) : new LiteralNull());
		return (T)this;
	}
	
	@Override
	public T literal(Number number) {
		nodes.add(number != null ? new LiteralNumber(number) : new LiteralNull());
		return (T)this;
	}
	
	@Override
	public T literal(String str) {
		nodes.add(str != null ? new LiteralString(str) : new LiteralNull());
		return (T)this;
	}
	
	@Override
	public T literalNull() {
		nodes.add(new LiteralNull());
		return (T)this;
	}
	
	@Override
	public T param(String name) {
		nodes.add(new Param(name));
		return (T)this;
	}
	
	@Override
	public T sqlCase(Case sqlCase) {
		nodes.add(Objects.requireNonNull(sqlCase, "sqlCase cannot be null"));
		return (T)this;
	}
	
	@Override
	public T subquery(Select select) {
		nodes.add(Objects.requireNonNull(select, "select cannot be null"));
		return (T)this;
	}
	
	@Override
	public T tableAlias(String alias) {
		workColumn = Column.byAlias(alias);
		return (T)this;
	}
	
	@Override
	public T tableName(String name) {
		workColumn = Column.byTableName(name);
		return (T)this;
	}
	
	/* END Fluent API */
}