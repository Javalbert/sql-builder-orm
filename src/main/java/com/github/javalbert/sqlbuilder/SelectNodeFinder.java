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

@SuppressWarnings("rawtypes")
public class SelectNodeFinder implements NodeVisitor {
	private SelectList selectList;
	private From from;
	private Where where;
	private GroupBy groupBy;
	private Having having;
	private OrderBy orderBy;
	
	public SelectList getSelectList() { return selectList; }
	public From getFrom() { return from; }
	public Where getWhere() { return where; }
	public GroupBy getGroupBy() { return groupBy; }
	public Having getHaving() { return having; }
	public OrderBy getOrderBy() { return orderBy; }

	@Override
	public boolean visit(Node node) {
		if (node.getType() == Node.TYPE_SELECT) {
			Select select = (Select)node;
			getNodes(select);
		}
		return false;
	}
	
	private void getNodes(Select select) {
		for (Node node : select.getNodes()) {
			switch (node.getType()) {
				case Node.TYPE_SELECT_LIST: selectList = (SelectList)node; break;
				case Node.TYPE_FROM: from = (From)node; break;
				case Node.TYPE_WHERE: where = (Where)node; break;
				case Node.TYPE_GROUP_BY: groupBy = (GroupBy)node; break;
				case Node.TYPE_HAVING: having = (Having)node; break;
				case Node.TYPE_ORDER_BY: orderBy = (OrderBy)node; break;
			}
		}
	}
}