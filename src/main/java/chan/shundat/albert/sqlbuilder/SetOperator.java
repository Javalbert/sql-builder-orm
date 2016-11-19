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

public class SetOperator implements Node<SetOperator> {
	public static final SetOperator EXCEPT = new ImmutableSetOperator(Keywords.EXCEPT);
	public static final SetOperator INTERSECT = new ImmutableSetOperator(Keywords.INTERSECT);
	public static final SetOperator UNION = new ImmutableSetOperator(Keywords.UNION);
	public static final SetOperator UNION_ALL = new ImmutableSetOperator(Keywords.UNION_ALL);
	
	public static SetOperator except(Select select) {
		if (select == null) {
			return EXCEPT;
		}
		return new SetOperator(Keywords.EXCEPT, select);
	}
	public static SetOperator intersect(Select select) {
		if (select == null) {
			return INTERSECT;
		}
		return new SetOperator(Keywords.INTERSECT, select);
	}
	public static SetOperator union(Select select) {
		if (select == null) {
			return UNION;
		}
		return new SetOperator(Keywords.UNION, select);
	}
	public static SetOperator unionAll(Select select) {
		if (select == null) {
			return UNION_ALL;
		}
		return new SetOperator(Keywords.UNION_ALL, select);
	}
	
	private String operator;
	private Select select;
	
	public String getOperator() { return operator; }
	public void setOperator(String operator) { this.operator = operator; }
	public Select getSelect() { return select; }
	public void setSelect(Select select) { this.select = select; }
	@Override
	public int getType() {
		return TYPE_SET_OPERATOR;
	}
	
	public SetOperator(SetOperator operator) {
		this(operator.getOperator(), operator.getSelect().mutable());
	}
	
	protected SetOperator(String operator) {
		this(operator, null);
	}
	
	protected SetOperator(String operator, Select select) {
		this.operator = operator;
		this.select = select;
	}
	
	@Override
	public boolean accept(NodeVisitor visitor) {
		if (!visitor.visit(this)) {
			return false;
		}
		return select != null ? select.accept(visitor) : true;
	}
	
	@Override
	public SetOperator immutable() {
		SetOperator operator = new ImmutableSetOperator(this);
		return operator;
	}
	
	@Override
	public SetOperator mutable() {
		SetOperator operator = new SetOperator(this);
		return operator;
	}
}