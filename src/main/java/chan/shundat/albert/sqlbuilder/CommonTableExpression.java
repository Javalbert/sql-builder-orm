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

import chan.shundat.albert.utils.string.Strings;

public class CommonTableExpression implements Node<CommonTableExpression> {
	protected List<String> columns;
	protected Select select;
	private String name;
	
	public List<String> getColumns() { return columns; }
	public void setColumns(List<String> columns) { this.columns = columns; }
	public String getName() { return name; }
	public void setName(String name) { this.name = Strings.safeTrim(name); }
	public Select getSelect() { return select; }
	public void setSelect(Select select) { this.select = select; }
	@Override
	public int getType() { return TYPE_COMMON_TABLE_EXPRESSION; }
	
	public CommonTableExpression(CommonTableExpression cte) {
		this(cte.getName());
		if (cte.getColumns() != null) {
			columns = new ArrayList<>();
			columns.addAll(cte.getColumns());
		}
		select = cte.getSelect().mutable();
	}
	
	public CommonTableExpression(String name) {
		this.name = Strings.safeTrim(name);
	}
	
	@Override
	public boolean accept(NodeVisitor visitor) {
		if (!visitor.visit(this)) {
			return false;
		}
		return select != null ? select.accept(visitor) : true;
	}

	@Override
	public CommonTableExpression immutable() {
		CommonTableExpression cte = new ImmutableCommonTableExpression(this);
		return cte;
	}
	
	@Override
	public CommonTableExpression mutable() {
		CommonTableExpression cte = new CommonTableExpression(this);
		return cte;
	}
}