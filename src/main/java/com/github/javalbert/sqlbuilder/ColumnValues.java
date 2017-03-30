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

/**
 * AKA Row Value Constructor
 * @author Albert
 *
 */
public class ColumnValues extends ExpressionBuilder<ColumnValues> implements Node<ColumnValues> {
	public static final Token DEFAULT = new ConstantToken(Keywords.DEFAULT, false);
	
	@Override
	public int getType() { return TYPE_COLUMN_VALUES; }

	public ColumnValues() {}
	
	public ColumnValues(ColumnValues values) {
		nodes = NodeUtils.mutableNodes(values);
	}
	
	@Override
	public boolean accept(NodeVisitor visitor) {
		return NodeUtils.visit(this, nodes, visitor);
	}

	@Override
	public ColumnValues immutable() {
		return new ImmutableColumnValues(this);
	}

	@Override
	public ColumnValues mutable() {
		return new ColumnValues(this);
	}
	
	/* BEGIN Fluent API */

	public ColumnValues sqlDefault() {
		nodes.add(DEFAULT);
		return this;
	}
	
	/* END Fluent API */
}