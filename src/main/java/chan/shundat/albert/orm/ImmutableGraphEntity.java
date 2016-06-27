/*******************************************************************************
 * Copyright (c) 2016 Albert Shun-Dat Chan
 *
 * Licensed under MIT License
 * See LICENSE file at the root of the project or
 * https://github.com/Javalbert/sql-builder-orm/blob/master/LICENSE
 *******************************************************************************/
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