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
package com.github.javalbert.sqlbuilder;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

public final class NodeUtils {
	public static void addColumn(NodeHolder holder, Column workColumn, String name) {
		if (workColumn != null) {
			workColumn.setName(name);
		} else {
			workColumn = new Column(null, null, name);
		}
		
		holder.getNodes().add(workColumn);
	}
	
	@SuppressWarnings("rawtypes")
	public static List<Node> immutableNodes(NodeHolder holder) {
		List<Node> nodes = new ArrayList<>();
		for (Node node : holder.getNodes()) {
			nodes.add(node.immutable());
		}
		return Collections.unmodifiableList(nodes);
	}

	@SuppressWarnings("rawtypes")
	public static List<Node> mutableNodes(NodeHolder holder) {
		List<Node> nodes = new ArrayList<>();
		for (Node node : holder.getNodes()) {
			nodes.add(node.mutable());
		}
		return nodes;
	}
	
	public static boolean visit(
			@SuppressWarnings("rawtypes") Node visitedNode,
			@SuppressWarnings("rawtypes") List<Node> nodes,
			NodeVisitor visitor) {
		if (!visitor.visit(visitedNode)) {
			return false;
		}
		
		for (@SuppressWarnings("rawtypes") Node node : nodes) {
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
	public static void setAlias(String alias, @SuppressWarnings("rawtypes") List<Node> nodes) {
		Node<?> node = !nodes.isEmpty() ? nodes.get(nodes.size() - 1) : null;
		
		if (node == null) {
			throw new IllegalStateException("No nodes");
		} else if (node instanceof Aliasable) {
			Aliasable aliasable = (Aliasable)node;
			aliasable.setAlias(alias);
		}
	}
	
	private NodeUtils() {}
}