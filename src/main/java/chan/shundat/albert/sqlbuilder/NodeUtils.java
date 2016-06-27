/*******************************************************************************
 * Copyright (c) 2016 Albert Shun-Dat Chan
 *
 * Licensed under MIT License
 * See LICENSE file at the root of the project or
 * https://github.com/Javalbert/sql-builder-orm/blob/master/LICENSE
 *******************************************************************************/
package chan.shundat.albert.sqlbuilder;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

@SuppressWarnings("rawtypes")
public final class NodeUtils {
	public static void addColumn(NodeHolder holder, Column workColumn, String name) {
		if (workColumn != null) {
			workColumn.setName(name);
		} else {
			workColumn = new Column(null, null, name);
		}
		
		holder.getNodes().add(workColumn);
	}
	
	public static List<Node> immutableNodes(NodeHolder holder) {
		List<Node> nodes = new ArrayList<>();
		for (Node node : holder.getNodes()) {
			nodes.add(node.immutable());
		}
		return Collections.unmodifiableList(nodes);
	}

	public static List<Node> mutableNodes(NodeHolder holder) {
		List<Node> nodes = new ArrayList<>();
		for (Node node : holder.getNodes()) {
			nodes.add(node.mutable());
		}
		return nodes;
	}
	
	public static boolean visit(Node visitedNode, List<Node> nodes, NodeVisitor visitor) {
		if (!visitor.visit(visitedNode)) {
			return false;
		}
		
		for (Node node : nodes) {
			if (!node.accept(visitor)) {
				return false;
			}
		}
		return true;
	}
	
	/**
	 * Gets the previous node and sets the specified alias to it
	 * @param alias
	 * @param nodes
	 */
	public static void setAlias(String alias, List<Node> nodes) {
		Node node = !nodes.isEmpty() ? nodes.get(nodes.size() - 1) : null;
		
		if (node == null) {
			throw new IllegalStateException("No nodes");
		} else if (node instanceof Aliasable) {
			Aliasable aliasable = (Aliasable)node;
			aliasable.setAlias(alias);
		}
	}
	
	private NodeUtils() {}
}