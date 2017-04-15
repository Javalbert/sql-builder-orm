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

import java.util.AbstractList;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.List;
import java.util.Objects;

public class CteList extends AbstractList<CommonTableExpression> {
	public static final CteList EMPTY = new CteList();
	
	private final List<CommonTableExpression> cteList;
	
	@SuppressWarnings("unchecked")
	CteList() {
		this(Collections.EMPTY_LIST);
	}
	
	CteList(Collection<CommonTableExpression> ctes) {
		this.cteList = new ArrayList<>(ctes);
	}

	@Override
	public CommonTableExpression get(int index) {
		return cteList.get(index);
	}

	@Override
	public int size() {
		return cteList.size();
	}
	
	public CteList as(SelectStatement query) {
		CteList list = copy();
		list.cteList.set(getLastIndex(), getPreviousCte().as(query));
		return list;
	}

	public CteList columns(TableColumn...columns) {
		CteList list = copy();
		list.cteList.set(getLastIndex(), getPreviousCte().columns(columns));
		return list;
	}

	public CteList with(Table queryName) {
		assertPreviousCte();
		CteList list = copy();
		list.cteList.add(new CommonTableExpression(queryName));
		return list;
	}
	
	public DeleteStatement delete(Table table) {
		return DSL.delete(table).with(this);
	}
	
	public InsertStatement insert(Table table) {
		return DSL.insert(table).with(this);
	}
	
	public SelectStatement select(SelectColumn<?>...columns) {
		return DSL.select(columns).with(this);
	}
	
	public UpdateStatement update(Table table) {
		return DSL.update(table).with(this);
	}

	CteList add0(CommonTableExpression cte) {
		CteList list = copy();
		list.cteList.add(cte);
		return list;
	}
	
	CteList copy() {
		return new CteList(this);
	}
	
	private void assertPreviousCte() {
		CommonTableExpression cte = cteList.isEmpty() ? null : cteList.get(getLastIndex());
		if (cte != null) {
			Objects.requireNonNull(cte.getQuery(), "call CteList.as(SelectStatement) first");
		}
	}
	
	private int getLastIndex() {
		return cteList.size() - 1;
	}
	
	private CommonTableExpression getPreviousCte() {
		return Objects.requireNonNull(cteList.isEmpty() ? null : cteList.get(getLastIndex()));
	}
}
