package chan.shundat.albert.sqlbuilder;

public class ImmutablePredicate extends Predicate {
	public ImmutablePredicate(Predicate predicate) {
		nodes = NodeUtils.immutableNodes(predicate);
	}
}