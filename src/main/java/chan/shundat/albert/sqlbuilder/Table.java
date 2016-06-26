package chan.shundat.albert.sqlbuilder;

import chan.shundat.albert.utils.string.Strings;

public class Table implements Aliasable, Node<Table> {
	public static Table name(String name) {
		return new Table(name, null);
	}
	
	protected String alias;
	protected String name;
	
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
	public int getType() { return TYPE_TABLE; }

	public Table(String name) {
		this(name, null);
	}
	
	public Table(String name, String alias) {
		this.name = Strings.safeTrim(name);
		this.alias = Strings.safeTrim(alias);
	}
	
	public Table(Table table) {
		this(table.getName(), table.getAlias());
	}
	
	@Override
	public boolean accept(NodeVisitor visitor) {
		return visitor.visit(this);
	}
	
	@Override
	public Table immutable() {
		Table table = new ImmutableTable(this);
		return table;
	}
	
	@Override
	public Table mutable() {
		Table table = new Table(this);
		return table;
	}
}