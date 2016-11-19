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
package chan.shundat.albert.orm;

import java.io.IOException;
import java.io.Serializable;
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import chan.shundat.albert.sqlbuilder.Aliasable;
import chan.shundat.albert.sqlbuilder.Delete;
import chan.shundat.albert.sqlbuilder.Insert;
import chan.shundat.albert.sqlbuilder.Node;
import chan.shundat.albert.sqlbuilder.Select;
import chan.shundat.albert.sqlbuilder.SelectList;
import chan.shundat.albert.sqlbuilder.SelectNodeFinder;
import chan.shundat.albert.sqlbuilder.SqlStatement;
import chan.shundat.albert.sqlbuilder.Update;
import chan.shundat.albert.sqlbuilder.vendor.ANSI;
import chan.shundat.albert.sqlbuilder.vendor.Vendor;
import chan.shundat.albert.utils.ClassUtils;
import chan.shundat.albert.utils.jdbc.JdbcUtils;
import chan.shundat.albert.utils.jdbc.ResultSetHelper;

@SuppressWarnings({ "rawtypes", "unchecked" })
public class JdbcMapper {
	/* BEGIN Class members */
	
	public static final int NOT_UPDATED = -1;
	public static final int SAVED = 1;
	public static final int UPDATED = 2;
	
	private static final Logger logger = LoggerFactory.getLogger(JdbcMapper.class);
	
	private static <T> T createObject(Class<T> clazz, 
			List<FieldColumnMapping> columnMappings, 
			ResultSetHelper rs) 
			throws SQLException {
		T instance = null;
		try {
			instance = clazz.newInstance();
			setObjectProperties(instance, columnMappings, rs);
		} catch (InstantiationException | IllegalAccessException e) {}
		return instance;
	}
	
	private static void save(Connection connection, 
			Object object, 
			JdbcStatement insertStatement, 
			ClassRowMapping classRowMapping) 
			throws SQLException {
		PreparedStatement stmt = null;
		ResultSet rs = null;
		try {
			stmt = insertStatement.createPreparedStatement(connection, classRowMapping.isAutoIncrementId());
			stmt.executeUpdate();
			
			if (classRowMapping.isAutoIncrementId()) {
				rs = stmt.getGeneratedKeys();
				
				if (rs.next()) {
					int id = rs.getInt(1);
					classRowMapping.setAutoIncrementId(object, id);
				}
			}
		} catch (SQLException e) {
			JdbcUtils.closeQuietly(rs);
			JdbcUtils.closeQuietly(stmt);
			throw e;
		}
	}
	
	private static void setObjectProperties(Object instance, 
			List<FieldColumnMapping> columnMappings, 
			ResultSetHelper rs) 
			throws SQLException {
		for (FieldColumnMapping fieldColumnMapping : columnMappings) {
			fieldColumnMapping.setFromResultSet(instance, rs);
		}
	}
	
	private static boolean refresh(Connection connection, 
			Object object, 
			JdbcStatement selectStatement, 
			List<FieldColumnMapping> columnMappings) throws SQLException {
		PreparedStatement stmt = null;
		ResultSetHelper rs = null;
		try {
			stmt = selectStatement.createPreparedStatement(connection);
			rs = new ResultSetHelper(stmt.executeQuery());
			
			if (!rs.next()) {
				return false;
			}
			
			setObjectProperties(object, columnMappings, rs);
			return true;
		} catch (SQLException e) {
			throw e;
		} finally {
			JdbcUtils.closeQuietly(rs);
			JdbcUtils.closeQuietly(stmt);
		}
	}

	private static <T> T uniqueResultById(Connection connection, 
			Class<T> clazz, 
			Serializable id, 
			ClassRowMapping classRowMapping, 
			JdbcStatement selectStatement) throws SQLException {
		classRowMapping.setIdParameters(selectStatement, id);
		T object = selectStatement.uniqueResult(connection, clazz);
		return object;
	}
	
	private static boolean update(Connection connection, JdbcStatement updateStatement, ClassRowMapping classRowMapping) throws SQLException {
		int updatedRows = updateStatement.executeUpdate(connection);
		boolean updated = updatedRows > 0;
		
		if (!updated && classRowMapping.hasVersionControl()) {
			throw new IllegalStateException("row was updated or deleted by another transaction");
		}
		return updated;
	}
	
	/* END Class members */
	
	private final Map<Class, ClassRowMapping> mappings = new HashMap<>();
	private final Vendor vendor;
	
	public Map<Class, ClassRowMapping> getMappings() { return Collections.unmodifiableMap(mappings); }
	public Vendor getVendor() { return vendor; }
	
	public JdbcMapper() {
		this(new ANSI());
	}
	
	public JdbcMapper(Vendor vendor) {
		if (vendor == null) {
			throw new NullPointerException("vendor cannot be null");
		}
		this.vendor = vendor;
	}

	/* BEGIN Public methods */

	/**
	 * Remember to call JdbcStatement.sqlStatement(SqlStatement) method
	 * @return
	 */
	public JdbcStatement createQuery() {
		return createQuery(null);
	}
	
	public JdbcStatement createQuery(SqlStatement sqlStatement) {
		return new JdbcStatement(this, sqlStatement);
	}
	
	public boolean delete(Connection connection, Object object) throws SQLException {
		ClassRowMapping classRowMapping = mappings.get(object.getClass());
		Serializable id = classRowMapping.getOrCreateId(object);
		return delete(connection, id, classRowMapping);
	}
	
	public boolean delete(Connection connection, Class clazz, Serializable id) throws SQLException {
		ClassRowMapping classRowMapping = mappings.get(clazz);
		return delete(connection, id, classRowMapping);
	}
	
//	public <T, C extends Collection<T>, ID extends Serializable, IDList extends Collection<ID>> C getCollection(
//			Connection connection, 
//			Class<T> clazz, 
//			C collection, 
//			IDList ids) 
//			throws SQLException {
//		return getCollection(connection, clazz, collection, ids, null);
//	}
//	
//	/**
//	 * WARNING: Could be slow because each ID in <b>ids</b> is another round-trip to the database. <br>
//	 * RECOMMENDED: Construct a <code>Select</code> object with a <code>Where</code> that selects the same IDs in <b>ids</b><br>
//	 * <br>
//	 * JDBC does not support batch reads, see http://stackoverflow.com/q/21592224.
//	 * @param connection
//	 * @param graphEntity
//	 * @param collection
//	 * @param ids
//	 * @param graph
//	 * @return
//	 * @throws SQLException
//	 */
//	public <T, C extends Collection<T>, ID extends Serializable, IDList extends Collection<ID>> C getCollection(
//			Connection connection, 
//			GraphEntity graphEntity, 
//			C collection, 
//			IDList ids, 
//			ObjectGraphResolver graphResolver) 
//			throws SQLException {
//		Class clazz = graphEntity.getClazz();
//		
//		ClassRowMapping classRowMapping = mappings.get(clazz);
//		
//		Select selectById = classRowMapping.getSelectById();
//		
//		JdbcStatement selectStatement = createQuery(selectById)
//				.cachePreparedStatement(true);
//
//		try {
//			for (Serializable id : ids) {
//				T object = (T)uniqueResultById(connection, clazz, id, classRowMapping, selectStatement);
//				collection.add(object);
//			}
//
//			if (graphResolver != null) {
//				graphResolver.resolveRelatedObjects(connection, graphEntity, collection);
//			}
//		} catch (SQLException e) {
//			throw e;
//		} finally {
//			selectStatement.closePreparedStatement();
//		}
//		return collection;
//	}

	public <T> T get(Connection connection, Class<T> clazz, Serializable id) throws SQLException {
		ClassRowMapping classRowMapping = mappings.get(clazz);
		Select selectById = classRowMapping.getSelectById();
		JdbcStatement selectStatement = createQuery(selectById);
		return uniqueResultById(connection, clazz, id, classRowMapping, selectStatement);
	}

	public <T> T get(Connection connection, GraphEntity graphEntity, Serializable id, ObjectGraphResolver graphResolver) throws SQLException {
		T object = (T)get(connection, graphEntity.getClazz(), id);
		graphResolver.resolveRelatedObjects(connection, graphEntity, object);
		return object;
	}
	
	public List<FieldColumnMapping> getColumnMappings(ClassRowMapping classRowMapping, Select select) {
		List<FieldColumnMapping> columnMappings = new ArrayList<>();
		Map<String, FieldColumnMapping> fieldColumnMappings = classRowMapping.getFieldColumnMappings();
		Map<String, FieldColumnMapping> fieldAliasMappings = classRowMapping.getFieldAliasMappings();
		
		SelectNodeFinder finder = new SelectNodeFinder();
		select.accept(finder);
		SelectList selectList = finder.getSelectList();
		
		for (Node columnNode : selectList.getNodes()) {
			Aliasable aliasable = (Aliasable)columnNode;
			
			// Give priority to column aliases because PreparedStatement objects' get*(String columnLabel) 
			// methods will check aliases instead, if they were defined in the SQL string
			FieldColumnMapping columnMapping = fieldAliasMappings.get(aliasable.getAlias());
			
			if (columnMapping == null) {
				chan.shundat.albert.sqlbuilder.Column column = columnNode.getType() == Node.TYPE_COLUMN 
						? (chan.shundat.albert.sqlbuilder.Column)columnNode : null;
				
				if (column != null) {
					columnMapping = fieldColumnMappings.get(column.getName());
				}
				if (columnMapping == null) {
					StringBuilder error = new StringBuilder();
					
					if (column != null) {
						error.append("Column (")
								.append(vendor.print(column))
								.append(")");
					} else {
						error.append("Alias (")
								.append(aliasable.getAlias())
								.append(")");
					}
					
					error.append(" is not mapped in the class (")
							.append(classRowMapping.getClazz())
							.append(")");
					logger.warn(error.toString());
				}
			}
			columnMappings.add(columnMapping);
		}
		
		return columnMappings;
	}
	
	public boolean refresh(Connection connection, Object object) throws SQLException {
		ClassRowMapping classRowMapping = mappings.get(object.getClass());
		Select selectById = classRowMapping.getSelectById();
		JdbcStatement selectStatement = createQuery(selectById);

		classRowMapping.setIdParameters(selectStatement, object);
		List<FieldColumnMapping> columnMappings = getColumnMappings(classRowMapping, selectById);
		
		return refresh(connection, object, selectStatement, columnMappings);
	}
	
	public void register(Class clazz) {
		if (!clazz.isAnnotationPresent(Entity.class) 
				|| mappings.containsKey(clazz)) {
			return;
		}
		
		ClassRowMapping mapping = new ClassRowMapping(clazz, vendor);
		mappings.put(clazz, mapping);
	}
	
	public void register(String packageName) {
		try {
			for (Class clazz : ClassUtils.getClasses(packageName)) {
				register(clazz);
			}
		} catch (ClassNotFoundException | IOException e) {}
	}

	public void save(Connection connection, Object object) throws SQLException {
		ClassRowMapping classRowMapping = mappings.get(object.getClass());
		
		classRowMapping.setVersion(object);
		Insert insert = classRowMapping.getInsert();
		JdbcStatement insertStatement = createQuery(insert).setParametersFrom(object, classRowMapping);
		
		save(connection, object, insertStatement, classRowMapping);
	}

	public int saveOrUpdate(Connection connection, Object object) throws SQLException {
		ClassRowMapping classRowMapping = mappings.get(object.getClass());

		Serializable id = classRowMapping.getOrCreateId(object);
		boolean update = false;

		if (classRowMapping.isAutoIncrementId()) {
			update = (int)id > 0;
		} else {
			Object currentDbRecord = get(connection, object.getClass(), id);
			update = currentDbRecord != null;
		}
		
		int result = 0;
		if (update) {
			boolean updated = update(connection, object);
			result = updated ? UPDATED : NOT_UPDATED;
		} else {
			save(connection, object);
			result = SAVED;
		}
		
		return result;
	}
	
	/**
	 * WARN: This does not call ResultSet.next()
	 * @param clazz
	 * @param select
	 * @param rs
	 * @return
	 * @throws SQLException
	 */
	public <T> T toObject(Class<T> clazz, Select select, ResultSet rs) throws SQLException {
		ClassRowMapping classRowMapping = mappings.get(clazz);
		List<FieldColumnMapping> columnMappings = getColumnMappings(classRowMapping, select);

		ResultSetHelper rsHelper = new ResultSetHelper(rs);
		T object = createObject(clazz, columnMappings, rsHelper);
		return object;
	}
	
	public <T, C extends Collection> C toCollection(Class<T> clazz, Select select, ResultSet rs, C objects) throws SQLException {
		ResultSetHelper rsHelper = new ResultSetHelper(rs);
		
		ClassRowMapping classRowMapping = mappings.get(clazz);
		List<FieldColumnMapping> columnMappings = getColumnMappings(classRowMapping, select);
		
		while (rs.next()) {
			T object = createObject(clazz, columnMappings, rsHelper);
			objects.add(object);
		}
		return objects;
	}

	public <K, T> Map<K, T> toMap(Class<T> clazz, Select select, ResultSet rs, Map map, String mapKeyName) throws SQLException {
		ResultSetHelper rsHelper = new ResultSetHelper(rs);
		
		ClassRowMapping classRowMapping = mappings.get(clazz);
		List<FieldColumnMapping> columnMappings = getColumnMappings(classRowMapping, select);
		
		while (rs.next()) {
			T object = createObject(clazz, columnMappings, rsHelper);
			K key = classRowMapping.getMapKeyValue(mapKeyName, object);
			map.put(key, object);
		}
		return map;
	}
	
	public boolean update(Connection connection, Object object) throws SQLException {
		ClassRowMapping classRowMapping = mappings.get(object.getClass());
		Update updateById = classRowMapping.getUpdateById();
		JdbcStatement updateStatement = createQuery(updateById).setParametersFrom(object, classRowMapping);
		return update(connection, updateStatement, classRowMapping);
	}
	
	/* END Public methods */
	
	/* BEGIN Private methods */
	
	private boolean delete(Connection connection, Serializable id, ClassRowMapping classRowMapping) throws SQLException {
		Delete delete = classRowMapping.getDeleteById();
		JdbcStatement deleteStatement = createQuery(delete);
		classRowMapping.setIdParameters(deleteStatement, id);
		int deletedRows = deleteStatement.executeUpdate(connection);
		return deletedRows > 0;
	}
	
	/* END Private methods */
}