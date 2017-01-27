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
public class Delete implements DMLStatement<Delete>, NodeHolder, TableNameSpecifier<Delete> {
	protected List<Node> nodes = new ArrayList<>();

	@Override
	public List<Node> getNodes() { return nodes; }
	@Override
	public int getType() { return TYPE_DELETE; }

	public Delete() {}
	
	public Delete(Delete delete) {
		nodes = NodeUtils.mutableNodes(delete);
	}
	
	@Override
	public boolean accept(NodeVisitor visitor) {
		return NodeUtils.visit(this, nodes, visitor);
	}

	@Override
	public Delete immutable() {
		Delete delete = new ImmutableDelete(this);
		return delete;
	}

	@Override
	public Delete mutable() {
		Delete delete = new Delete(this);
		return delete;
	}
	
	/* BEGIN Fluent API */
	
	// DELETE FROM JOIN syntax not ANSI standard
//	public Delete from(From from) {
//		if (from == null) {
//			throw new NullPointerException("from cannot be null");
//		}
//		nodes.add(from);
//		return this;
//	}

	@Override
	public Delete tableName(String name) {
		Table table = new Table(name);
		nodes.add(table);
		return this;
	}
	
	public Delete where(Where where) {
		if (where == null) {
			throw new NullPointerException("where cannot be null");
		}
		nodes.add(where);
		return this;
	}
	
	public Delete with(With with) {
		if (with == null) {
			throw new NullPointerException("with cannot be null");
		}
		nodes.add(with);
		return this;
	}
	
	/* END Fluent API */
}