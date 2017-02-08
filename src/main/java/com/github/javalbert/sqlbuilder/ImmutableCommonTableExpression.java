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
import java.util.Collections;
import java.util.List;

public class ImmutableCommonTableExpression extends CommonTableExpression {
	@Override
	public void setColumns(List<String> columns) {
		throw new UnsupportedOperationException("immutable");
	}
	@Override
	public void setName(String name) {
		throw new UnsupportedOperationException("immutable");
	}
	@Override
	public void setSelect(Select select) {
		throw new UnsupportedOperationException("immutable");
	}
	
	public ImmutableCommonTableExpression(CommonTableExpression cte) {
		super(cte.getName());
		if (cte.getColumns() != null) {
			columns = new ArrayList<>();
			columns.addAll(cte.getColumns());
			columns = Collections.unmodifiableList(columns);
		}
		select = cte.getSelect().immutable();
	}
}