/*******************************************************************************
 * Copyright (c) 2016 Albert Shun-Dat Chan
 *
 * Licensed under MIT License
 * See LICENSE file at the root of the project or
 * https://github.com/Javalbert/sql-builder-orm/blob/master/LICENSE
 *******************************************************************************/
package chan.shundat.albert.orm;

import java.util.Collections;
import java.util.LinkedHashSet;
import java.util.Set;

import chan.shundat.albert.orm.Relationship.Builder;
import chan.shundat.albert.utils.string.Strings;

@SuppressWarnings("rawtypes")
public class GraphEntity {
	protected final Class clazz;
	protected final Set<Relationship> relationships = new LinkedHashSet<>();
	protected final String tableAlias;
	
	public Class getClazz() { return clazz; }
	public Set<Relationship> getRelationships() { return Collections.unmodifiableSet(relationships); }
	public String getTableAlias() { return tableAlias; }

	public GraphEntity(Class clazz, String tableAlias) {
		if (clazz == null) {
			throw new NullPointerException("clazz cannot be null");
		} else if (Strings.isNullOrEmpty(tableAlias)) {
			throw new IllegalArgumentException("tableAlias cannot be null or empty");
		}
		
		this.clazz = clazz;
		this.tableAlias = tableAlias;
	}

	public Builder isRelatedToMany(GraphEntity relatedEntity) {
		return new Relationship.Builder(this)
				.isRelatedToMany(relatedEntity);
	}
	
	public ImmutableGraphEntity createImmutableClone() {
		return new ImmutableGraphEntity(this);
	}
	
	public Builder isRelatedToOne(GraphEntity relatedEntity) {
		return new Relationship.Builder(this)
				.isRelatedToOne(relatedEntity);
	}
	
	protected void addRelationship(Relationship relationship) {
		relationships.add(relationship);
	}
	
	@Override
	public int hashCode() {
		final int prime = 31;
		int result = 1;
		result = prime * result + ((clazz == null) ? 0 : clazz.hashCode());
		result = prime * result + ((tableAlias == null) ? 0 : tableAlias.hashCode());
		return result;
	}
	
	@Override
	public boolean equals(Object obj) {
		if (this == obj)
			return true;
		if (obj == null)
			return false;
		if (getClass() != obj.getClass())
			return false;
		GraphEntity other = (GraphEntity) obj;
		if (clazz == null) {
			if (other.clazz != null)
				return false;
		} else if (!clazz.equals(other.clazz))
			return false;
		if (tableAlias == null) {
			if (other.tableAlias != null)
				return false;
		} else if (!tableAlias.equals(other.tableAlias))
			return false;
		return true;
	}
	
	@Override
	public String toString() {
		return "GraphEntity [clazz=" + clazz + ", tableAlias=" + tableAlias + "]";
	}
}