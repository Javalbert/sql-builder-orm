package chan.shundat.albert.sqlbuilder;

public class ImmutableSelectList extends SelectList {
	public ImmutableSelectList(SelectList list) {
		nodes = NodeUtils.immutableNodes(list);
	}
}