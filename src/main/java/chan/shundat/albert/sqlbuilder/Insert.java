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

import java.util.ArrayList;
import java.util.List;

@SuppressWarnings("rawtypes")
public class Insert implements DMLStatement<Insert>, Node<Insert>, NodeHolder {
	protected List<Node> nodes = new ArrayList<>();
	
	@Override
	public List<Node> getNodes() { return nodes; }
	@Override
	public int getType() { return TYPE_INSERT; }

	public Insert() {}
	
	public Insert(Insert insert) {
		nodes = NodeUtils.mutableNodes(insert);
	}
	
	@Override
	public boolean accept(NodeVisitor visitor) {
		return NodeUtils.visit(this, nodes, visitor);
	}
	
	@Override
	public Insert immutable() {
		Insert insert = new ImmutableInsert(this);
		return insert;
	}

	@Override
	public Insert mutable() {
		Insert insert = new Insert(this);
		return insert;
	}
	
	/* BEGIN Fluent API */
	
	public Insert columns(ColumnList columns) {
		if (columns == null) {
			throw new NullPointerException("columns cannot be null");
		}
		nodes.add(columns);
		return this;
	}
	
	public Insert into(String name) {
		Table table = new Table(name);
		nodes.add(table);
		return this;
	}
	
	public Insert values(ColumnValues values) {
		if (values == null) {
			throw new NullPointerException("values cannot be null");
		}
		nodes.add(values);
		return this;
	}
	
	public Insert subselect(Select select) {
		if (select == null) {
			throw new NullPointerException("select cannot be null");
		}
		nodes.add(select);
		return this;
	}

	public Insert with(With with) {
		if (with == null) {
			throw new NullPointerException("with cannot be null");
		}
		nodes.add(with);
		return this;
	}
	
	/* END Fluent API */
}