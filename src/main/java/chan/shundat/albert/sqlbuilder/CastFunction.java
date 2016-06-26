package chan.shundat.albert.sqlbuilder;

@SuppressWarnings("rawtypes")
public class CastFunction extends Function {
	public CastFunction() {
		super(Keywords.CAST, 1);
	}
	
	/* BEGIN Fluent API */
	
	@Override
	public CastFunction append(String token) {
		super.append(token);
		return this;
	}
	
	@Override
	public CastFunction append(String token, boolean isNextNodeAnExpression) {
		super.append(token, isNextNodeAnExpression);
		return this;
	}
	
	public CastFunction as(String dataType) {
		Node node = !nodes.isEmpty() ? nodes.get(nodes.size() - 1) : null;
		
		if (node == null) {
			throw new IllegalStateException("No nodes");
		} else if (node instanceof Aliasable) {
			Aliasable aliasable = (Aliasable)node;
			aliasable.setAlias(dataType);
		}
		return this;
	}
	
	@Override
	public CastFunction column(String name) {
		super.column(name);
		return this;
	}
	
	@Override
	public CastFunction function(Function function) {
		super.function(function);
		return this;
	}
	
	@Override
	public CastFunction param(String name) {
		super.param(name);
		return this;
	}
	
	@Override
	public CastFunction sqlCase(Case sqlCase) {
		super.sqlCase(sqlCase);
		return this;
	}
	
	@Override
	public CastFunction subquery(Select select) {
		super.subquery(select);
		return this;
	}
	
	@Override
	public CastFunction tableAlias(String alias) {
		super.tableAlias(alias);
		return this;
	}
	
	@Override
	public CastFunction tableName(String name) {
		super.tableName(name);
		return this;
	}
	
	/* END Fluent API */
}