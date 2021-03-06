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

import java.util.Objects;

public class LiteralBoolean extends Literal<Boolean> {
	@Override
	public int getType() {
		return TYPE_LITERAL_BOOLEAN;
	}
	@Override
	public void setValue(Boolean value) {
		super.setValue(Objects.requireNonNull(value, "value cannot be null"));
	}
	
	public LiteralBoolean(LiteralBoolean literal) {
		super(literal.getValue());
		alias = literal.getAlias();
	}
	
	protected LiteralBoolean(Boolean value) {
		this.value = Objects.requireNonNull(value, "value cannot be null");
	}
	
	@Override
	public LiteralBoolean immutable() {
		return new ImmutableLiteralBoolean(this);
	}
	
	@Override
	public LiteralBoolean mutable() {
		return new LiteralBoolean(this);
	}
}