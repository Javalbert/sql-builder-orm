package com.github.javalbert.sqlbuilder;

import java.util.ArrayList;
import java.util.List;

public class Merge implements DMLStatement<Merge>, NodeHolder, TableNameSpecifier<Merge> {
	@SuppressWarnings("rawtypes")
	protected List<Node> nodes = new ArrayList<>();

	@SuppressWarnings("rawtypes")
	@Override
	public List<Node> getNodes() {
		return nodes;
	}
	
	@Override
	public int getType() {
		return TYPE_MERGE;
	}
	
	public Merge() {}
	
	public Merge(Merge merge) {
		nodes = NodeUtils.mutableNodes(merge);
	}

	@Override
	public boolean accept(NodeVisitor visitor) {
		return NodeUtils.visit(this, nodes, visitor);
	}

	@Override
	public Merge immutable() {
		return new ImmutableMerge(this);
	}

	@Override
	public Merge mutable() {
		return new Merge(this);
	}
	
	/* START Fluent API */

	@Override
	public Merge tableName(String name) {
		Table table = new Table(name);
		nodes.add(table);
		return this;
	}
	
	/* END Fluent API */
}
