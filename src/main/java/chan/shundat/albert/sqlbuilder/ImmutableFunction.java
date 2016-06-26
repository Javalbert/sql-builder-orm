package chan.shundat.albert.sqlbuilder;

public class ImmutableFunction extends Function {
	@Override
	public void setAlias(String alias) {
		throw new UnsupportedOperationException("immutable");
	}
	@Override
	public void setMaxArguments(int maxArguments) {
		throw new UnsupportedOperationException("immutable");
	}
	@Override
	public void setName(String name) {
		throw new UnsupportedOperationException("immutable");
	}
	
	public ImmutableFunction(Function function) {
		super(function.getName(), function.getMaxArguments());
		alias = function.getAlias();
		nodes = NodeUtils.immutableNodes(function);
	}
}