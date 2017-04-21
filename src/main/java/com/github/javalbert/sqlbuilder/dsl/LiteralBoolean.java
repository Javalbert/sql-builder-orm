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

public class LiteralBoolean extends Literal<LiteralBoolean, Boolean> {
	public static final LiteralBoolean TRUE = new LiteralBoolean(Boolean.TRUE);
	public static final LiteralBoolean FALSE = new LiteralBoolean(Boolean.FALSE);

	@Override
	public int getNodeType() {
		return TYPE_LITERAL_BOOLEAN;
	}
	
	LiteralBoolean(Boolean value) {
		super(value);
	}

	@Override
	public LiteralBoolean as(String alias) {
		LiteralBoolean literal = copy();
		literal.alias = alias;
		return literal;
	}
	
	@Override
	LiteralBoolean copy() {
		LiteralBoolean copy = new LiteralBoolean(value);
		copy.alias = alias;
		return copy;
	}
}
