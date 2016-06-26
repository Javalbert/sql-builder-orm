package chan.shundat.albert.sqlbuilder;

public class ImmutableHaving extends Having {
	public ImmutableHaving(Condition having) {
		nodes = NodeUtils.immutableNodes(having);
	}
}