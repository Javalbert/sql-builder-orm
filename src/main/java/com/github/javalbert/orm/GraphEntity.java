/*******************************************************************************
 * Copyright 2016 Albert Shun-Dat Chan
 *
 * Licensed under the Apache License, Version 2.0 (the "License"); you may not use this file except in
 * compliance with the License. You may obtain a copy of the License at
 *
 * http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software distributed under the License is
 * distributed on an "AS IS" BASIS, WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied. See
 * the License for the specific language governing permissions and limitations under the License.
 *******************************************************************************/
package com.github.javalbert.orm;

import java.util.Collections;
import java.util.LinkedHashSet;
import java.util.Set;

import com.github.javalbert.orm.Relationship.Builder;
import com.github.javalbert.utils.string.Strings;

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