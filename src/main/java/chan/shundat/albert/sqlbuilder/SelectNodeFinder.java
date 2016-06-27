/*******************************************************************************
 * Copyright (c) 2016 Albert Shun-Dat Chan
 *
 * Licensed under MIT License
 * See LICENSE file at the root of the project or
 * https://github.com/Javalbert/sql-builder-orm/blob/master/LICENSE
 *******************************************************************************/
package chan.shundat.albert.sqlbuilder;

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