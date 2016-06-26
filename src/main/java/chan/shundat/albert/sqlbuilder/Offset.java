package chan.shundat.albert.sqlbuilder;

public class Offset implements Node<Offset> {
	private int skipCount;

	public int getSkipCount() { return skipCount; }
	public void setSkipCount(int skipCount) { this.skipCount = skipCount; }
	@Override
	public int getType() {
		return TYPE_OFFSET;
	}
	
	public Offset(int skipCount) {
		this.skipCount = skipCount;
	}
	
	public Offset(Offset offset) {
		this(offset.getSkipCount());
	}
	
	@Override
	public boolean accept(NodeVisitor visitor) {
		return visitor.visit(this);
	}
	
	@Override
	public Offset immutable() {
		Offset offset = new ImmutableOffset(this);
		return offset;
	}
	
	@Override
	public Offset mutable() {
		Offset offset = new Offset(this);
		return offset;
	}
}