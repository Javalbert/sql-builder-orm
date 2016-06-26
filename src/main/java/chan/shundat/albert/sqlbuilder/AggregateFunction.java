package chan.shundat.albert.sqlbuilder;

public class AggregateFunction extends Function {
	public static AggregateFunction avg() { return new AggregateFunction(Keywords.AVG); }
	public static AggregateFunction count() { return new AggregateFunction(Keywords.COUNT); }
	public static AggregateFunction max() { return new AggregateFunction(Keywords.MAX); }
	public static AggregateFunction min() { return new AggregateFunction(Keywords.MIN); }
	public static AggregateFunction sum() { return new AggregateFunction(Keywords.SUM); }
	
	private AggregateFunction(String name) {
		super(name, 1);
	}
}