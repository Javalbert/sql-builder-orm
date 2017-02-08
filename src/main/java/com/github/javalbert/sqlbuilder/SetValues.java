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

import java.util.ArrayList;
import java.util.List;

@SuppressWarnings("rawtypes")
public class SetValues implements Node<SetValues>, NodeHolder {
	protected List<Node> nodes = new ArrayList<>();
	
	@Override
	public List<Node> getNodes() { return nodes; }
	@Override
	public int getType() { return TYPE_SET_VALUES; }

	public SetValues() {}
	
	public SetValues(SetValues values) {
		nodes = NodeUtils.mutableNodes(values);
	}
	
	@Override
	public boolean accept(NodeVisitor visitor) {
		return NodeUtils.visit(this, nodes, visitor);
	}

	@Override
	public SetValues immutable() {
		SetValues values = new ImmutableSetValues(this);
		return values;
	}

	@Override
	public SetValues mutable() {
		SetValues values = new SetValues(this);
		return values;
	}
	
	/* BEGIN Fluent API */
	
	public SetValues add(SetValue value) {
		if (value == null) {
			throw new NullPointerException("");
		}
		nodes.add(value);
		return this;
	}
	
	/* END Fluent API */
}