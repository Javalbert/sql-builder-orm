package chan.shundat.albert.sqlbuilder;

public class ImmutableColumnValues extends ColumnValues {
	public ImmutableColumnValues(ColumnValues values) {
		nodes = NodeUtils.immutableNodes(values);
	}
}