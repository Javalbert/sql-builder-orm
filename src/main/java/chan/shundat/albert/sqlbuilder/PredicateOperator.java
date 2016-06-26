package chan.shundat.albert.sqlbuilder;

public class PredicateOperator extends Token {
	public static final PredicateOperator AND = new PredicateOperator(Keywords.AND);
	public static final PredicateOperator BETWEEN = new PredicateOperator(Keywords.BETWEEN);
	public static final PredicateOperator EQ = new PredicateOperator(RelationalOperator.EQ);
	public static final PredicateOperator EXISTS = new PredicateOperator(Keywords.EXISTS);
	public static final PredicateOperator GT = new PredicateOperator(RelationalOperator.GT);
	public static final PredicateOperator GT_EQ = new PredicateOperator(RelationalOperator.GT_EQ);
	public static final PredicateOperator IN = new PredicateOperator(Keywords.IN);
	public static final PredicateOperator IS_NOT_NULL = new PredicateOperator(Keywords.IS_NOT_NULL, false);
	public static final PredicateOperator IS_NULL = new PredicateOperator(Keywords.IS_NULL, false);
	public static final PredicateOperator LIKE = new PredicateOperator(Keywords.LIKE);
	public static final PredicateOperator LT = new PredicateOperator(RelationalOperator.LT);
	public static final PredicateOperator LT_EQ = new PredicateOperator(RelationalOperator.LT_EQ);
	public static final PredicateOperator NOT_BETWEEN = new PredicateOperator(Keywords.NOT_BETWEEN);
	public static final PredicateOperator NOT_EQ = new PredicateOperator(RelationalOperator.NOT_EQ);
	public static final PredicateOperator NOT_EXISTS = new PredicateOperator(Keywords.NOT_EXISTS);
	public static final PredicateOperator NOT_IN = new PredicateOperator(Keywords.NOT_IN);
	public static final PredicateOperator NOT_LIKE = new PredicateOperator(Keywords.NOT_LIKE);
	
	@Override
	public void setToken(String token) {
		throw new UnsupportedOperationException("this is a flyweight object");
	}
	
	private PredicateOperator(String token) {
		this(token, true);
	}
	
	private PredicateOperator(String token, boolean isNextNodeAnExpression) {
		super(token, isNextNodeAnExpression);
	}
	
	@Override
	public PredicateOperator immutable() {
		return this;
	}
	
	@Override
	public PredicateOperator mutable() {
		return this;
	}
}