package chan.shundat.albert.orm;

public class ImmutableGraphEntity extends GraphEntity {
	public ImmutableGraphEntity(GraphEntity graphEntity) {
		super(graphEntity.getClazz(), graphEntity.getTableAlias());
		relationships.addAll(graphEntity.getRelationships());
	}
	
	@Override
	protected void addRelationship(Relationship relationship) {
		throw new UnsupportedOperationException("immutable GraphEntity, cannot add relationship");
	}
}