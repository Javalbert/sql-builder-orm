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

public class LiteralNull extends Literal<LiteralNull, Void> {
	public static final LiteralNull INSTANCE = new LiteralNull();
	
	@Override
	public LiteralNull as(String alias) {
		LiteralNull literal = copy();
		literal.alias = alias;
		return literal;
	}
	
	private LiteralNull() {}
	
	LiteralNull copy() {
		LiteralNull copy = new LiteralNull();
		copy.alias = alias;
		return copy;
	}
}
