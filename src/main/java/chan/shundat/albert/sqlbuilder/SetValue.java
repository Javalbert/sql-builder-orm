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
package chan.shundat.albert.sqlbuilder;

public class SetValue extends ExpressionBuilder<SetValue> implements Node<SetValue> {
	@Override
	public int getType() { return TYPE_SET_VALUE; }

	public SetValue() {}
	
	public SetValue(SetValue value) {
		nodes = NodeUtils.mutableNodes(value);
	}
	
	@Override
	public boolean accept(NodeVisitor visitor) {
		return NodeUtils.visit(this, nodes, visitor);
	}

	@Override
	public SetValue immutable() {
		SetValue value = new ImmutableSetValue(this);
		return value;
	}

	@Override
	public SetValue mutable() {
		SetValue value = new SetValue(this);
		return value;
	}
}