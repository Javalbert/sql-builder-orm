package chan.shundat.albert.sqlbuilder;

public class ImmutableCondition extends Condition {
	public ImmutableCondition(Condition condition) {
		nodes = NodeUtils.immutableNodes(condition);
	}
}