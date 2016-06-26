package chan.shundat.albert.sqlbuilder;

import chan.shundat.albert.utils.string.Strings;

public class Param implements Aliasable, Node<Param> {
	protected String alias;
	private String name;

	@Override
	public String getAlias() { return alias; }
	@Override
	public void setAlias(String alias) {
		this.alias = Strings.safeTrim(alias);
	}
	public String getName() { return name; }
	public void setName(String name) {
		this.name = Strings.safeTrim(name);
	}
	@Override
	public int getType() { return TYPE_PARAM; }
	
	public Param(Param param) {
		this(param.getName());
		alias = param.getAlias();
	}
	
	public Param(String name) {
		this.name = Strings.safeTrim(name);
	}
	
	@Override
	public boolean accept(NodeVisitor visitor) {
		return visitor.visit(this);
	}
	
	@Override
	public Param immutable() {
		Param param = new ImmutableParam(this);
		return param;
	}
	
	@Override
	public Param mutable() {
		Param param = new Param(this);
		return param;
	}
}