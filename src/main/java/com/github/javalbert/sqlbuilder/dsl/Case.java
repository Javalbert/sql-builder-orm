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
import java.util.Collections;
import java.util.List;

import com.github.javalbert.utils.collections.CollectionUtils;

public class Case
implements ExpressionBuilder, Predicand, SelectColumn<Case>, ValueExpression {
	private String alias;
	private ValueExpression elseExpression;
	private final ValueExpression simpleCaseExpression;
	private List<When> whenClauses = Collections.emptyList();
	
	@Override
	public String getAlias() {
		return alias;
	}
	public ValueExpression getElseExpression() {
		return elseExpression;
	}
	@Override
	public int getNodeType() {
		return NODE_CASE;
	}
	public ValueExpression getSimpleCaseExpression() {
		return simpleCaseExpression;
	}
	public List<When> getWhenClauses() {
		return whenClauses;
	}

	Case() {
		this(LiteralNull.INSTANCE);
	}
	
	Case(Boolean value) {
		this(DSL.literal(value));
	}
	Case(Number value) {
		this(DSL.literal(value));
	}
	Case(String value) {
		this(DSL.literal(value));
	}
	
	/**
	 * @param simpleCaseExpression if not null, the <code>CASE</code> expression is
	 * called "simple CASE" expression which mirrors the switch statement,
	 * otherwise it is called "searched CASE" expression where each <code>WHEN</code>
	 * clause uses a boolean expression
	 */
	Case(ValueExpression simpleCaseExpression) {
		this.simpleCaseExpression = ValueExpression.ifNull(simpleCaseExpression);
	}
	
	@Override
	public Case as(String alias) {
		Case sqlCase = copy();
		sqlCase.alias = alias;
		return sqlCase;
	}
	
	public Case ifElse(Boolean value) {
		return ifElse(DSL.literal(value));
	}
	public Case ifElse(Number value) {
		return ifElse(DSL.literal(value));
	}
	public Case ifElse(String value) {
		return ifElse(DSL.literal(value));
	}
	
	public Case ifElse(ValueExpression elseExpression) {
		Case sqlCase = copy();
		sqlCase.elseExpression = elseExpression;
		return sqlCase;
	}
	
	public Case then(Boolean value) {
		return then(DSL.literal(value));
	}
	public Case then(Number value) {
		return then(DSL.literal(value));
	}
	public Case then(String value) {
		return then(DSL.literal(value));
	}
	
	public Case then(ValueExpression then) {
		if (whenClauses.isEmpty()) {
			throw new IllegalStateException("call When.when() first");
		}
		Case sqlCase = copy();

		final int last = whenClauses.size() - 1;
		List<When> whenClauses = CollectionUtils.subArrayList(this.whenClauses, 0, last);
		whenClauses.add(this.whenClauses.get(last).then(then));
		sqlCase.whenClauses = Collections.unmodifiableList(whenClauses);
		
		return sqlCase;
	}
	
	public Case when(Boolean value) {
		return when(DSL.literal(value));
	}
	public Case when(Number value) {
		return when(DSL.literal(value));
	}
	public Case when(String value) {
		return when(DSL.literal(value));
	}
	
	public Case when(BooleanExpression booleanExpression) {
		assertSearchedCaseExpression();
		Case sqlCase = copy();
		copyAndAddWhenClauses(sqlCase, When.search(booleanExpression));
		return sqlCase;
	}
	
	public Case when(ValueExpression simpleExpression) {
		assertSimpleCaseExpression();
		Case sqlCase = copy();
		copyAndAddWhenClauses(sqlCase, When.simple(simpleExpression));
		return sqlCase;
	}
	
	Case copy() {
		Case copy = new Case(simpleCaseExpression);
		copy.alias = alias;
		copy.elseExpression = elseExpression;
		copy.whenClauses = whenClauses;
		return copy;
	}
	
	private void assertCanAddWhen() {
		When lastWhen = !whenClauses.isEmpty() ? whenClauses.get(whenClauses.size() - 1) : null;
		if (lastWhen != null && lastWhen.getThen() == null) {
			throw new IllegalStateException("must call When.then() for previous WHEN clause"
					+ " before calling When.when()");
		}
	}
	
	private void assertSearchedCaseExpression() {
		if (simpleCaseExpression != LiteralNull.INSTANCE) {
			throw new IllegalStateException("not allowed to add boolean expression"
					+ " because this is a \"simple CASE\" expression");
		}
	}
	
	private void assertSimpleCaseExpression() {
		if (simpleCaseExpression == LiteralNull.INSTANCE) {
			throw new IllegalStateException("not allowed to add simple expression"
					+ " because this is a \"searched CASE\" expression");
		}
	}
	
	private void copyAndAddWhenClauses(Case sqlCase, When newWhenClause) {
		assertCanAddWhen();
		List<When> whenClauses = new ArrayList<>(this.whenClauses);
		whenClauses.add(newWhenClause);
		sqlCase.whenClauses = Collections.unmodifiableList(whenClauses);
	}
}
