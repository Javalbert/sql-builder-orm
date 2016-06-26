package chan.shundat.albert.sqlbuilder;

import chan.shundat.albert.utils.string.Strings;

public class Function extends ExpressionBuilder<Function> implements Aliasable, Node<Function> {
	public static final int UNKNOWN_ARGUMENTS = -1;
	
	protected String alias;
	private int maxArguments;
	private String name;

	@Override
	public String getAlias() { return alias; }
	@Override
	public void setAlias(String alias) {
		this.alias = Strings.safeTrim(alias);
	}
	public int getMaxArguments() { return maxArguments; }
	public void setMaxArguments(int maxArguments) { this.maxArguments = maxArguments; }
	public String getName() { return name; }
	public void setName(String name) {
		this.name = Strings.safeTrim(name);
	}
	@Override
	public int getType() { return TYPE_FUNCTION; }
	
	public Function(Function function) {
		this(function.getName(), function.getMaxArguments());
		alias = function.getAlias();
		nodes = NodeUtils.mutableNodes(function);
	}
	
	public Function(String name) {
		this(name, UNKNOWN_ARGUMENTS);
	}
	
	public Function(String name, int maxArguments) {
		this.maxArguments = maxArguments;
		this.name = Strings.safeTrim(name);
	}
	
	@Override
	public boolean accept(NodeVisitor visitor) {
		return NodeUtils.visit(this, nodes, visitor);
	}
	
	@Override
	public Function immutable() {
		Function function = new ImmutableFunction(this);
		return function;
	}
	
	@Override
	public Function mutable() {
		Function function = new Function(this);
		return function;
	}
}