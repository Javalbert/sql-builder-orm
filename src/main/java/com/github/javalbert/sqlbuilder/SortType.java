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

public class SortType extends Token {
	public static final SortType ASC = new SortType(Keywords.ASC);
	public static final SortType DESC = new SortType(Keywords.DESC);
	
	@Override
	public void setToken(String token) {
		throw new UnsupportedOperationException("this is a flyweight object");
	}
	@Override
	public int getType() {
		return TYPE_ORDER_BY_SORT;
	}
	
	private SortType(String keyword) {
		super(keyword, false);
	}
	
	@Override
	public SortType immutable() {
		return this;
	}
	
	@Override
	public SortType mutable() {
		return this;
	}
}