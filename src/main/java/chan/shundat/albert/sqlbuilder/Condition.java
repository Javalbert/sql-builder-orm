package chan.shundat.albert.sqlbuilder;

public class Condition extends ConditionBuilder<Condition> implements Node<Condition> {
	@Override
	public int getType() { return TYPE_CONDITION; }
	
	public Condition() {}
	
	public Condition(Condition condition) {
		nodes = NodeUtils.mutableNodes(condition);
	}
	
	@Override
	public boolean accept(NodeVisitor visitor) {
		return NodeUtils.visit(this, nodes, visitor);
	}
	
	@Override
	public Condition immutable() {
		Condition condition = new ImmutableCondition(this);
		return condition;
	}
	
	@Override
	public Condition mutable() {
		Condition condition = new Condition(this);
		return condition;
	}
}