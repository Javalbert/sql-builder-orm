/*******************************************************************************
 * Copyright (c) 2016 Albert Shun-Dat Chan
 *
 * Licensed under MIT License
 * See LICENSE file at the root of the project or
 * https://github.com/Javalbert/sql-builder-orm/blob/master/LICENSE
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