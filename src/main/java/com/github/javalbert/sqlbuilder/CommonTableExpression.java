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

import com.github.javalbert.utils.string.Strings;

public class CommonTableExpression implements Node<CommonTableExpression> {
	protected List<String> columns;
	protected Select select;
	private String name;
	
	public List<String> getColumns() { return columns; }
	public void setColumns(List<String> columns) { this.columns = columns; }
	public String getName() { return name; }
	public void setName(String name) { this.name = Strings.safeTrim(name); }
	public Select getSelect() { return select; }
	public void setSelect(Select select) { this.select = select; }
	@Override
	public int getType() { return TYPE_COMMON_TABLE_EXPRESSION; }
	
	public CommonTableExpression(CommonTableExpression cte) {
		this(cte.getName());
		if (cte.getColumns() != null) {
			columns = new ArrayList<>();
			columns.addAll(cte.getColumns());
		}
		select = cte.getSelect().mutable();
	}
	
	public CommonTableExpression(String name) {
		this.name = Strings.safeTrim(name);
	}
	
	@Override
	public boolean accept(NodeVisitor visitor) {
		if (!visitor.visit(this)) {
			return false;
		}
		return select != null ? select.accept(visitor) : true;
	}

	@Override
	public CommonTableExpression immutable() {
		CommonTableExpression cte = new ImmutableCommonTableExpression(this);
		return cte;
	}
	
	@Override
	public CommonTableExpression mutable() {
		CommonTableExpression cte = new CommonTableExpression(this);
		return cte;
	}
}