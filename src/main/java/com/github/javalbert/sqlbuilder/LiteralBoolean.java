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

public class LiteralBoolean extends Literal<Boolean> {
	@Override
	public int getType() {
		return TYPE_LITERAL_BOOLEAN;
	}
	@Override
	public void setValue(Boolean value) {
		if (value == null) {
			throw new NullPointerException("value cannot be null");
		}
		super.setValue(value);
	}
	
	public LiteralBoolean(LiteralBoolean literal) {
		super(literal.getValue());
		alias = literal.getAlias();
	}
	
	protected LiteralBoolean(Boolean value) {
		if (value == null) {
			throw new NullPointerException("value cannot be null");
		}
		this.value = value;
	}
	
	@Override
	public LiteralBoolean immutable() {
		LiteralBoolean literal = new ImmutableLiteralBoolean(this);
		return literal;
	}
	
	@Override
	public LiteralBoolean mutable() {
		LiteralBoolean literal = new LiteralBoolean(this);
		return literal;
	}
}