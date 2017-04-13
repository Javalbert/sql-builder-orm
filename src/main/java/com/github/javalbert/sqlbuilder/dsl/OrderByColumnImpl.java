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

import java.util.Objects;

import com.github.javalbert.utils.string.Strings;

public class OrderByColumnImpl implements OrderByColumn {
	private final String label;
	private SortType sortType;
	
	@Override
	public String getOrderByColumnLabel() {
		return label;
	}
	@Override
	public SortType getSortType() {
		return sortType;
	}
	
	public OrderByColumnImpl(String label, SortType sortType) {
		this.label = Strings.illegalArgOnEmpty(label, "label cannot be null or empty");
		this.sortType = Objects.requireNonNull(sortType, "sort type cannot be null or empty");
	}
}
