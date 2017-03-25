package com.github.javalbert.sqlbuilder;

public class ImmutableMerge extends Merge {
	public ImmutableMerge(Merge merge) {
		nodes = NodeUtils.immutableNodes(merge);
	}
	
	/* START Fluent API */
	
	@Override
	public Merge tableName(String name) {
		throw new UnsupportedOperationException("immutable");
	}
	
	/* END Fluent API */
}
