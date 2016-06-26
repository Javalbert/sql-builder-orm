package chan.shundat.albert.sqlbuilder;

import chan.shundat.albert.utils.string.Strings;

@SuppressWarnings("rawtypes")
public abstract class Literal<T> implements Aliasable, Node {
	protected String alias;
	protected T value;
	
	@Override
	public String getAlias() { return alias; }
	@Override
	public void setAlias(String alias) { this.alias = Strings.safeTrim(alias); }
	public T getValue() { return value; }
	public void setValue(T value) { this.value = value; }
	
	public Literal() {
		this(null);
	}
	
	public Literal(T value) {
		this.value = value;
	}
	
	@Override
	public boolean accept(NodeVisitor visitor) {
		return visitor.visit(this);
	}
}