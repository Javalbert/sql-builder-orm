package chan.shundat.albert.sqlbuilder;

public class ImmutableOffset extends Offset {
	@Override
	public void setSkipCount(int skipCount) {
		throw new UnsupportedOperationException("immutable");
	}
	
	public ImmutableOffset(Offset offset) {
		super(offset.getSkipCount());
	}
}