package chan.shundat.albert.sqlbuilder;

public class ImmutableUpdate extends Update {
	public ImmutableUpdate(Update update) {
		nodes = NodeUtils.immutableNodes(update);
	}
}