/*******************************************************************************
 * Copyright (c) 2016 Albert Shun-Dat Chan
 *
 * Licensed under MIT License
 * See LICENSE file at the root of the project or
 * https://github.com/Javalbert/sql-builder-orm/blob/master/LICENSE
 *******************************************************************************/
package chan.shundat.albert.sqlbuilder;

import java.util.ArrayList;
import java.util.List;

@SuppressWarnings("rawtypes")
public class ColumnList implements ColumnBuilder<ColumnList>, Node<ColumnList>, NodeHolder {
	protected List<Node> nodes = new ArrayList<>();
	private Column workColumn;
	
	@Override
	public List<Node> getNodes() { return nodes; }
	@Override
	public int getType() { return TYPE_COLUMN_LIST; }

	public ColumnList() {}
	
	public ColumnList(ColumnList columns) {
		nodes = NodeUtils.mutableNodes(columns);
	}
	
	@Override
	public boolean accept(NodeVisitor visitor) {
		return NodeUtils.visit(this, nodes, visitor);
	}

	@Override
	public ColumnList immutable() {
		ColumnList columns = new ImmutableColumnList(this);
		return columns;
	}

	@Override
	public ColumnList mutable() {
		ColumnList columns = new ColumnList(this);
		return columns;
	}
	
	/* BEGIN Fluent API */
	
	@Override
	public ColumnList column(String name) {
		NodeUtils.addColumn(this, workColumn, name);
		workColumn = null;
		return this;
	}
	
	@Override
	public ColumnList tableAlias(String alias) {
		workColumn = Column.byAlias(alias);
		return this;
	}

	@Override
	public ColumnList tableName(String name) {
		workColumn = Column.byTableName(name);
		return this;
	}
	
	/* END Fluent API */
}