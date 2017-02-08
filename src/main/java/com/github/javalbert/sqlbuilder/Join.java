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

public class Join extends Token {
	public static final Join FULL_OUTER_JOIN = new Join(Keywords.FULL_OUTER_JOIN);
	public static final Join INNER_JOIN = new Join(Keywords.INNER_JOIN);
	public static final Join LEFT_OUTER_JOIN = new Join(Keywords.LEFT_OUTER_JOIN);
	public static final Join RIGHT_OUTER_JOIN = new Join(Keywords.RIGHT_OUTER_JOIN);
	
	@Override
	public void setToken(String token) {
		throw new UnsupportedOperationException("this is a flyweight object");
	}
	
	private Join(String keyword) {
		super(keyword);
	}
	
	@Override
	public Join immutable() {
		return this;
	}
	
	@Override
	public Join mutable() {
		return this;
	}
}