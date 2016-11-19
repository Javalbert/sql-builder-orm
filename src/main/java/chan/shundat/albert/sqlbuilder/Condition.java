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

public class Condition extends ConditionBuilder<Condition> implements Node<Condition> {
	@Override
	public int getType() { return TYPE_CONDITION; }
	
	public Condition() {}
	
	public Condition(Condition condition) {
		nodes = NodeUtils.mutableNodes(condition);
	}
	
	@Override
	public boolean accept(NodeVisitor visitor) {
		return NodeUtils.visit(this, nodes, visitor);
	}
	
	@Override
	public Condition immutable() {
		Condition condition = new ImmutableCondition(this);
		return condition;
	}
	
	@Override
	public Condition mutable() {
		Condition condition = new Condition(this);
		return condition;
	}
}