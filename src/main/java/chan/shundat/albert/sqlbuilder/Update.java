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
public class Update implements DMLStatement<Update>, 
		Node<Update>, 
		NodeHolder, 
		TableNameSpecifier<Update> {
	protected List<Node> nodes = new ArrayList<>();
	
	@Override
	public List<Node> getNodes() { return nodes; }
	@Override
	public int getType() { return TYPE_UPDATE; }

	public Update() {}
	
	public Update(Update update) {
		nodes = NodeUtils.mutableNodes(update);
	}
	
	@Override
	public boolean accept(NodeVisitor visitor) {
		return NodeUtils.visit(this, nodes, visitor);
	}

	@Override
	public Update immutable() {
		Update update = new ImmutableUpdate(this);
		return update;
	}

	@Override
	public Update mutable() {
		Update update = new Update(this);
		return update;
	}
	
	/* BEGIN Fluent API */
	
	public Update set(SetValues values) {
		if (values == null) {
			throw new NullPointerException("values cannot be null");
		}
		nodes.add(values);
		return this;
	}
	
	@Override
	public Update tableName(String name) {
		Table table = new Table(name);
		nodes.add(table);
		return this;
	}
	
	public Update where(Where where) {
		if (where == null) {
			throw new NullPointerException("where cannot be null");
		}
		nodes.add(where);
		return this;
	}
	
	public Update with(With with) {
		if (with == null) {
			throw new NullPointerException("with cannot be null");
		}
		nodes.add(with);
		return this;
	}
	
	/* END Fluent API */
}