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

public interface ExpressionBuilding<T> extends ColumnBuilder<T> {
	T append(String token);
	T append(String token, boolean isNextNodeAnExpression);
	T expression(Expression expression);
	T function(Function function);
	T literal(Boolean bool);
	T literal(Number number);
	/**
	 * WARN: Strong possibility of SQL injection attacks. See <code>http://stackoverflow.com/q/5741187</code>.
	 * <br><code>str</code> parameter value is surrounded by single quotes and single quotes in <code>str</code> 
	 * are replaced by two (2) single quotes. See <code>com.github.javalbert.sqlbuilder.vendor</code> package classes.
	 * @param str
	 * @return
	 */
	T literal(String str);
	T literalNull();
	/**
	 * 
	 * @param name name of parameter, should satisfy the regex :\w+
	 * @return
	 */
	T param(String name);
	/**
	 * SQL CASE statement<br>
	 * couldn't name the method case because it is a reserved word in Java for the switch statement
	 * @param sqlCase
	 * @return
	 */
	T sqlCase(Case sqlCase);
	T subquery(Select select);
}