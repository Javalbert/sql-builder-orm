/*******************************************************************************
 * Copyright (c) 2016 Albert Shun-Dat Chan
 *
 * Licensed under MIT License
 * See LICENSE file at the root of the project or
 * https://github.com/Javalbert/sql-builder-orm/blob/master/LICENSE
 *******************************************************************************/
package chan.shundat.albert.sqlbuilder;

public class SelectList extends ExpressionBuilder<SelectList> implements Node<SelectList> {
	public static final Token DISTINCT = new ConstantToken(Keywords.DISTINCT, true);

	@Override
	public int getType() { return TYPE_SELECT_LIST; }
	
	public SelectList() {}
	
	public SelectList(SelectList list) {
		nodes = NodeUtils.mutableNodes(list);
	}
	
	@Override
	public boolean accept(NodeVisitor visitor) {
		return NodeUtils.visit(this, nodes, visitor);
	}
	
	@Override
	public SelectList immutable() {
		SelectList list = new ImmutableSelectList(this);
		return list;
	}
	
	@Override
	public SelectList mutable() {
		SelectList list = new SelectList(this);
		return list;
	}
	
	/* BEGIN Fluent API */
	
	public SelectList as(String alias) {
		NodeUtils.setAlias(alias, nodes);
		return this;
	}

	public SelectList distinct() {
		nodes.add(DISTINCT);
		return this;
	}
	
	/* END Fluent API */
}