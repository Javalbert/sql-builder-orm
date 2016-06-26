package chan.shundat.albert.sqlbuilder;

public class ImmutableParam extends Param {
	@Override
	public void setAlias(String alias) {
		throw new UnsupportedOperationException("immutable");
	}
	@Override
	public void setName(String name) {
		throw new UnsupportedOperationException("immutable");
	}
	
	public ImmutableParam(Param param) {
		super(param.getName());
		alias = param.getAlias();
	}
}