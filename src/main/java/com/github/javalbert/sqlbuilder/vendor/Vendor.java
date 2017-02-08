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
package com.github.javalbert.sqlbuilder.vendor;

import com.github.javalbert.sqlbuilder.Case;
import com.github.javalbert.sqlbuilder.Column;
import com.github.javalbert.sqlbuilder.ColumnList;
import com.github.javalbert.sqlbuilder.ColumnValues;
import com.github.javalbert.sqlbuilder.CommonTableExpression;
import com.github.javalbert.sqlbuilder.Condition;
import com.github.javalbert.sqlbuilder.Delete;
import com.github.javalbert.sqlbuilder.Expression;
import com.github.javalbert.sqlbuilder.Fetch;
import com.github.javalbert.sqlbuilder.From;
import com.github.javalbert.sqlbuilder.Function;
import com.github.javalbert.sqlbuilder.GroupBy;
import com.github.javalbert.sqlbuilder.InValues;
import com.github.javalbert.sqlbuilder.Insert;
import com.github.javalbert.sqlbuilder.Literal;
import com.github.javalbert.sqlbuilder.Node;
import com.github.javalbert.sqlbuilder.Offset;
import com.github.javalbert.sqlbuilder.OrderBy;
import com.github.javalbert.sqlbuilder.Param;
import com.github.javalbert.sqlbuilder.Predicate;
import com.github.javalbert.sqlbuilder.Select;
import com.github.javalbert.sqlbuilder.SelectList;
import com.github.javalbert.sqlbuilder.SetOperator;
import com.github.javalbert.sqlbuilder.SetValue;
import com.github.javalbert.sqlbuilder.SetValues;
import com.github.javalbert.sqlbuilder.Table;
import com.github.javalbert.sqlbuilder.Token;
import com.github.javalbert.sqlbuilder.Update;
import com.github.javalbert.sqlbuilder.With;

@SuppressWarnings("rawtypes")
public interface Vendor {
	String createTableIdentifier(com.github.javalbert.orm.Table tableAnno);
	
	String print(Case sqlCase);
	String print(Column column);
	String print(ColumnList columns);
	String print(ColumnValues values);
	String print(CommonTableExpression cte);
	String print(Condition condition);
	String print(Condition condition, boolean group);
	String print(Delete delete);
	String print(Expression expression);
	String print(Expression expression, boolean subExpression);
	String print(Fetch fetch);
	String print(From from);
	String print(Function function);
	String print(GroupBy groupBy);
	String print(Insert insert);
	String print(InValues inValues);
	String print(Literal literal);
	String print(Node node);
	String print(Offset offset);
	String print(OrderBy orderBy);
	String print(Param param);
	String print(Predicate predicate);
	String print(Select select);
	String print(Select select, boolean subquery);
	String print(SelectList list);
	String print(SetOperator operator);
	String print(SetValue value);
	String print(SetValues values);
	String print(Table table);
	String print(Token token);
	String print(Update update);
	String print(With with);
}