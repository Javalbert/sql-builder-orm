package chan.shundat.albert.sqlbuilder;

import java.util.ArrayList;
import java.util.List;

@SuppressWarnings("rawtypes")
public class SetValues implements Node<SetValues>, NodeHolder {
	protected List<Node> nodes = new ArrayList<>();
	
	@Override
	public List<Node> getNodes() { return nodes; }
	@Override
	public int getType() { return TYPE_SET_VALUES; }

	public SetValues() {}
	
	public SetValues(SetValues values) {
		nodes = NodeUtils.mutableNodes(values);
	}
	
	@Override
	public boolean accept(NodeVisitor visitor) {
		return NodeUtils.visit(this, nodes, visitor);
	}

	@Override
	public SetValues immutable() {
		SetValues values = new ImmutableSetValues(this);
		return values;
	}

	@Override
	public SetValues mutable() {
		SetValues values = new SetValues(this);
		return values;
	}
	
	/* BEGIN Fluent API */
	
	public SetValues add(SetValue value) {
		if (value == null) {
			throw new NullPointerException("");
		}
		nodes.add(value);
		return this;
	}
	
	/* END Fluent API */
}