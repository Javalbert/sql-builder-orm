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

public class LiteralNumber extends Literal<LiteralNumber, Number> {
	@Override
	public int getNodeType() {
		return NODE_LITERAL_NUMBER;
	}
	
	LiteralNumber(Number value) {
		super(value);
	}

	@Override
	public LiteralNumber as(String alias) {
		LiteralNumber literal = copy();
		literal.alias = alias;
		return literal;
	}
	
	@Override
	LiteralNumber copy() {
		LiteralNumber copy = new LiteralNumber(value);
		copy.alias = alias;
		return copy;
	}
}
