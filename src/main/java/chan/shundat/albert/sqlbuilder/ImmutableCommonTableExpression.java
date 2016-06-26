package chan.shundat.albert.sqlbuilder;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

public class ImmutableCommonTableExpression extends CommonTableExpression {
	@Override
	public void setColumns(List<String> columns) {
		throw new UnsupportedOperationException("immutable");
	}
	@Override
	public void setName(String name) {
		throw new UnsupportedOperationException("immutable");
	}
	@Override
	public void setSelect(Select select) {
		throw new UnsupportedOperationException("immutable");
	}
	
	public ImmutableCommonTableExpression(CommonTableExpression cte) {
		super(cte.getName());
		if (cte.getColumns() != null) {
			columns = new ArrayList<>();
			columns.addAll(cte.getColumns());
			columns = Collections.unmodifiableList(columns);
		}
		select = cte.getSelect().immutable();
	}
}