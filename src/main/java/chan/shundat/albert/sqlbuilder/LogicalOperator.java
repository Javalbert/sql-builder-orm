package chan.shundat.albert.sqlbuilder;

public class LogicalOperator extends Token {
	public static final LogicalOperator AND = new LogicalOperator(Keywords.AND);
	public static final LogicalOperator OR = new LogicalOperator(Keywords.OR);
	
	@Override
	public void setToken(String token) {
		throw new UnsupportedOperationException("this is a flyweight object");
	}
	
	private LogicalOperator(String operator) {
		super(operator);
	}
	
	@Override
	public LogicalOperator immutable() {
		return this;
	}
	
	@Override
	public LogicalOperator mutable() {
		return this;
	}
}