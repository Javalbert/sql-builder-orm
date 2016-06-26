package chan.shundat.albert.sqlbuilder;

import chan.shundat.albert.sqlbuilder.NodeUtils;
import chan.shundat.albert.sqlbuilder.SetValues;

public class ImmutableSetValues extends SetValues {
	public ImmutableSetValues(SetValues values) {
		nodes = NodeUtils.immutableNodes(values);
	}
}