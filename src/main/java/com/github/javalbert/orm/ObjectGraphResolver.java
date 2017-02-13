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

import java.sql.Connection;
import java.sql.SQLException;
import java.util.Collection;

public abstract class ObjectGraphResolver {
	protected final JdbcMapper jdbcMapper;
	
	protected ObjectGraphResolver(JdbcMapper jdbcMapper) {
		if (jdbcMapper == null) {
			throw new NullPointerException("jdbcMapper cannot be null");
		}
		
		this.jdbcMapper = jdbcMapper;
	}

	public abstract <T> void resolveRelatedObjects(
			Connection connection, 
			GraphEntity<T> graphEntity, 
			Collection<T> collection) throws SQLException;
	public abstract <T> void resolveRelatedObjects(
			Connection connection, 
			GraphEntity<T> graphEntity, 
			T object) throws SQLException;
	public abstract <T> Collection<T> toCollection(
			Connection connection, 
			JdbcStatement statement, 
			GraphEntity<T> graphEntity, 
			Collection<T> collection) throws SQLException;
	public abstract <T> Collection<T> toCollection(
			Connection connection, 
			JdbcStatement statement, 
			GraphEntity<T> graphEntity, 
			Collection<T> collection, 
			ObjectCache objectCache) throws SQLException;
}