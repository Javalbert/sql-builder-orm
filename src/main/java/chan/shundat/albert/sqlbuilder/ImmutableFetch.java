package chan.shundat.albert.sqlbuilder;

public class ImmutableFetch extends Fetch {
	@Override
	public void setFetchCount(int fetchCount) {
		throw new UnsupportedOperationException("immutable");
	}
	
	public ImmutableFetch(Fetch fetch) {
		super(fetch.getFetchCount());
	}
}