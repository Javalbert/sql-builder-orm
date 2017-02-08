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

public class LiteralString extends Literal<String> {
	@Override
	public int getType() {
		return TYPE_LITERAL_STRING;
	}
	@Override
	public void setValue(String value) {
		if (value == null) {
			throw new NullPointerException("value cannot be null");
		}
		super.setValue(value);
	}
	
	public LiteralString(LiteralString literal) {
		super(literal.getValue());
		alias = literal.getAlias();
	}
	
	public LiteralString(String value) {
		if (value == null) {
			throw new NullPointerException("value cannot be null");
		}
		this.value = value;
	}
	
	@Override
	public LiteralString immutable() {
		LiteralString literal = new ImmutableLiteralString(this);
		return literal;
	}
	
	@Override
	public LiteralString mutable() {
		LiteralString literal = new LiteralString(this);
		return literal;
	}
}