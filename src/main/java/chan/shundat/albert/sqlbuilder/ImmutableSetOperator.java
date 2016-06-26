package chan.shundat.albert.sqlbuilder;

public class ImmutableSetOperator extends SetOperator {
	@Override
	public void setOperator(String operator) {
		throw new UnsupportedOperationException("immutable");
	}
	@Override
	public void setSelect(Select select) {
		throw new UnsupportedOperationException("immutable");
	}

	public ImmutableSetOperator(SetOperator operator) {
		this(operator.getOperator(), operator.getSelect().immutable());
	}
	
	protected ImmutableSetOperator(String operator) {
		super(operator);
	}
	
	protected ImmutableSetOperator(String operator, Select select) {
		super(operator, select);
	}
}