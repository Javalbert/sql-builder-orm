/*******************************************************************************
 * Copyright (c) 2016 Albert Shun-Dat Chan
 *
 * Licensed under MIT License
 * See LICENSE file at the root of the project or
 * https://github.com/Javalbert/sql-builder-orm/blob/master/LICENSE
 *******************************************************************************/
package chan.shundat.albert.orm;

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

	public abstract <T> void resolveRelatedObjects(Connection connection, 
			GraphEntity graphEntity, 
			Collection<T> collection) throws SQLException;
	public abstract void resolveRelatedObjects(Connection connection, 
			GraphEntity graphEntity, 
			Object object) throws SQLException;
	public abstract <T, C extends Collection<T>> C toCollection(
			Connection connection, 
			JdbcStatement statement, 
			GraphEntity graphEntity, 
			C collection) throws SQLException;
	public abstract <T, C extends Collection<T>> C toCollection(
			Connection connection, 
			JdbcStatement statement, 
			GraphEntity graphEntity, 
			C collection, 
			ObjectCache objectCache) throws SQLException;
}