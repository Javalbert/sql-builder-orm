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

import java.io.Serializable;
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.github.javalbert.orm.Relationship.JoinColumn;
import com.github.javalbert.orm.Relationship.OrderByColumn;
import com.github.javalbert.sqlbuilder.Condition;
import com.github.javalbert.sqlbuilder.From;
import com.github.javalbert.sqlbuilder.OrderBy;
import com.github.javalbert.sqlbuilder.Predicate;
import com.github.javalbert.sqlbuilder.Select;
import com.github.javalbert.sqlbuilder.SelectList;
import com.github.javalbert.sqlbuilder.SortType;
import com.github.javalbert.sqlbuilder.Where;
import com.github.javalbert.utils.collections.CollectionFactory;
import com.github.javalbert.utils.collections.CollectionUtils;
import com.github.javalbert.utils.collections.MapFactory;
import com.github.javalbert.utils.jdbc.JdbcUtils;
import com.github.javalbert.utils.jdbc.ResultSetHelper;
import com.github.javalbert.utils.reflection.MemberAccess;
import com.github.javalbert.utils.string.Strings;

public class BatchResolver extends ObjectGraphResolver {
	/* BEGIN Class members */
	
	private static final Logger logger = LoggerFactory.getLogger(BatchResolver.class);

	private static void assertSingleJoinColumn(List<JoinColumn> joinColumns) {
		if (joinColumns.size() > 1) {
			throw new IllegalArgumentException(BatchResolver.class.getSimpleName() 
					+ " does not support multiple join columns in the relationships");
		}
	}
	
	/* END Class members */
	
	public BatchResolver(JdbcMapper jdbcMapper) {
		super(jdbcMapper);
	}
	
	@Override
	public <T> void resolveRelatedObjects(
			Connection connection, 
			GraphEntity<T> graphEntity, 
			Collection<T> collection) throws SQLException {
		EntityQuery query = new EntityQuery(connection);
		query.resolveRelationships(graphEntity, collection);
	}
	
	@Override
	public <T> void resolveRelatedObjects(
			Connection connection, 
			GraphEntity<T> graphEntity, 
			T object) throws SQLException {
		resolveRelatedObjects(connection, graphEntity, Collections.singletonList(object));
	}
	
	@Override
	public <T> Collection<T> toCollection(
			Connection connection, 
			JdbcStatement statement, 
			GraphEntity<T> graphEntity, 
			Collection<T> collection) 
			throws SQLException {
		return toCollection(connection, statement, graphEntity, collection, null);
	}
	
	@Override
	public <T> Collection<T> toCollection(
			Connection connection, 
			JdbcStatement statement,
			GraphEntity<T> graphEntity, 
			Collection<T> collection, 
			ObjectCache objectCache) throws SQLException {
		EntityQuery query = new EntityQuery(connection, objectCache);
		return query.toCollection(graphEntity, statement, collection);
	}
	
	/* BEGIN Inner classes */
	
	private interface JoinConditionStrategy {
		void appendConditions(Relationship relationship, Select select, From from);
	}
	
	private class EntityColumns {
		private final ClassRowMapping classRowMapping;
		private final List<FieldColumnMapping> fieldColumnMappings;
		@SuppressWarnings("rawtypes")
		private final GraphEntity graphEntity;
		private int lastPrimaryKeyIndex;
		private final ObjectCache objectCache;
		
		public EntityColumns(GraphEntity<?> graphEntity, ObjectCache objectCache) {
			this(graphEntity, objectCache, null);
		}
		
		public EntityColumns(GraphEntity<?> graphEntity, ObjectCache objectCache, JdbcStatement statement) {
			if (graphEntity == null) {
				throw new NullPointerException("graphEntity cannot be null");
			}
			
			classRowMapping = jdbcMapper.getMappings()
					.get(graphEntity.getEntityClass());
			fieldColumnMappings = statement != null 
					? jdbcMapper.getColumnMappings(classRowMapping, (Select)statement.getSqlStatement()) 
					: classRowMapping.getFieldColumnMappingList();
			
			this.graphEntity = graphEntity;
			this.objectCache = objectCache;
			
			initLastPrimaryKeyIndex(fieldColumnMappings);
		}
		
		public Object createFromResultSet(ResultSetHelper rs) throws SQLException {
			Object object = newEntityInstance();
			
			for (int i = 0; i < fieldColumnMappings.size(); i++) {
				FieldColumnMapping fieldColumnMapping = fieldColumnMappings.get(i);
				fieldColumnMapping.setFromResultSet(object, rs);
				
				if (i == lastPrimaryKeyIndex) {
					Object existing = objectCache.get(classRowMapping, object);
					
					if (existing != null) {
						return existing;
					}
				}
			}
			
			objectCache.add(classRowMapping, object);
			return object;
		}
		
		private void initLastPrimaryKeyIndex(List<FieldColumnMapping> fieldColumnMappings) {
			int lastPrimaryKeyIndex = 0;
			
			for (int i = 0; i < fieldColumnMappings.size(); i++) {
				FieldColumnMapping fieldColumnMapping = fieldColumnMappings.get(i);
				
				if (!fieldColumnMapping.isPrimaryKey()) {
					continue;
				}
				lastPrimaryKeyIndex = i;
			}
			this.lastPrimaryKeyIndex = lastPrimaryKeyIndex;
		}
		
		private Object newEntityInstance() {
			try {
				return graphEntity.getEntityClass().newInstance();
			} catch (InstantiationException | IllegalAccessException e) {
				return null;
			}
		}
	}
	
	private class EntityQuery {
		private final Connection connection;
		@SuppressWarnings("rawtypes")
		private final Map<GraphEntity, EntityColumns> entityColumnsMap = new HashMap<>();
		private final ObjectCache objectCache;
		
		public EntityQuery(Connection connection) {
			this(connection, null);
		}
		
		public EntityQuery(Connection connection, ObjectCache objectCache) {
			this.connection = connection;
			this.objectCache = objectCache != null ? objectCache : new ObjectCache();
		}
		
		public <T> void resolveRelationships(GraphEntity<T> graphEntity, Collection<T> collection) 
				throws SQLException {
			resolveRelationships(connection, graphEntity, collection);
		}
		
		public <T> Collection<T> toCollection(GraphEntity<T> graphEntity, JdbcStatement statement, Collection<T> collection) 
			throws SQLException {
			PreparedStatement stmt = null;
			ResultSetHelper rs = null;
			
			try {
				stmt = statement.createPreparedStatement(connection);
				rs = new ResultSetHelper(stmt.executeQuery());
				
				EntityColumns entityColumns = new EntityColumns(graphEntity, objectCache, statement);
				entityColumnsMap.put(graphEntity, entityColumns);
				
				while (rs.next()) {
					T entity = graphEntity.getEntityClass().cast(entityColumns.createFromResultSet(rs));
					collection.add(entity);
				}
				
				resolveRelationships(connection, graphEntity, collection);
				return collection;
			} catch (SQLException e) {
				throw e;
			} finally {
				JdbcUtils.closeQuietly(rs);
				JdbcUtils.closeQuietly(stmt);
			}
		}
		
		private EntityColumns createRelatedEntityColumns(Relationship relationship) {
			final GraphEntity<?> relatedEntity = relationship.getRelatedEntity();
			
			if (entityColumnsMap.containsKey(relatedEntity)) {
				logger.error(relatedEntity + " already handled and would have resulted in a circular "
						+ "dependency and a StackOverflowError. Create a new GraphEntity "
						+ "object with the same class but different alias.");
				return null;
			}

			EntityColumns entityColumns = new EntityColumns(relatedEntity, objectCache);
			entityColumnsMap.put(relatedEntity, entityColumns);
			return entityColumns;
		}
		
		@SuppressWarnings("unchecked")
		private <T> void resolveRelationships(Connection connection, GraphEntity<T> graphEntity, Collection<T> collection) 
				throws SQLException {
			for (Relationship relationship : graphEntity.getRelationships()) {
				EntityColumns entityColumns = createRelatedEntityColumns(relationship);
				
				if (entityColumns == null) {
					continue;
				}
				
				RelationshipResolver resolver = new RelationshipResolver(relationship, objectCache);
				Collection<?> relatedObjects = resolver.getRelatedObjects(connection, collection);
				
				resolveRelationships(connection, relationship.getRelatedEntity(), relatedObjects);
			}
		}
	}
	
	private class NToOneConditionStrategy implements JoinConditionStrategy {
		private final String relatedTableAlias;
		
		public NToOneConditionStrategy(String relatedTableAlias) {
			this.relatedTableAlias = relatedTableAlias;
		}
		
		@Override
		public void appendConditions(Relationship relationship, Select select, From from) {
			Where where = new Where();
			
			List<JoinColumn> joinColumns = relationship.getJoinColumns();
			assertSingleJoinColumn(joinColumns);

			JoinColumn joinColumn = joinColumns.get(0);
			appendJoinCondition(joinColumn, where);
			select.where(where);
		}
		
		private void appendJoinCondition(JoinColumn joinColumn, Where where) {
			where.predicate(new Predicate()
				.tableAlias(relatedTableAlias).column(joinColumn.getRelatedClassColumn())
				.in()
				.param(joinColumn.getOwnerClassColumn())
			);
		}
	};
	
	private class OneToManyConditionStrategy implements JoinConditionStrategy {
		private final String ownerTableAlias;
		private final String relatedTableAlias;
		
		public OneToManyConditionStrategy(String ownerTableAlias, String relatedTableAlias) {
			this.ownerTableAlias = ownerTableAlias;
			this.relatedTableAlias = relatedTableAlias;
		}
		
		@Override
		public void appendConditions(Relationship relationship, Select select, From from) {
			Condition joinCondition = new Condition();
			Where where = new Where();
			
			List<JoinColumn> joinColumns = relationship.getJoinColumns();
			assertSingleJoinColumn(joinColumns);
			
			JoinColumn joinColumn = joinColumns.get(0);
			appendJoinCondition(joinColumn, joinCondition, where);
			
			from.on(joinCondition);
			select.where(where);
		}
		
		private void appendJoinCondition(JoinColumn joinColumn, Condition joinCondition, Where where) {
			joinCondition.predicate(new Predicate()
				.tableAlias(relatedTableAlias).column(joinColumn.getRelatedClassColumn())
				.eq()
				.tableAlias(ownerTableAlias).column(joinColumn.getOwnerClassColumn())
			);
			
			where.predicate(new Predicate()
				.tableAlias(ownerTableAlias).column(joinColumn.getOwnerClassColumn())
				.in()
				.param(joinColumn.getOwnerClassColumn())
			);
		}
	};
	
	private class RelatedObjectSelect {
		private List<FieldColumnMapping> fieldColumnMappings;
		@SuppressWarnings("rawtypes")
		private final Class ownerClass;
		private final String ownerTableAlias;
		private int primaryKeyColumnIndex;
		@SuppressWarnings("rawtypes")
		private final Class relatedClass;
		private final String relatedTableAlias;
		private final Relationship relationship;
		private Select select;
		
		public List<FieldColumnMapping> getFieldColumnMappings() { return fieldColumnMappings; }
		
		public int getPrimaryKeyColumnIndex() {
			getSelect();
			return primaryKeyColumnIndex;
		}
		
		public Select getSelect() {
			if (select == null) {
				initSelect();
			}
			return select;
		}
		
		public RelatedObjectSelect(Relationship relationship) {
			GraphEntity<?> ownerEntity = relationship.getOwnerEntity();
			if (Strings.isNullOrEmpty(ownerEntity.getTableAlias())) {
				throw new IllegalStateException(relationship + "'s owner entity is missing its table alias");
			}
			
			GraphEntity<?> relatedEntity = relationship.getRelatedEntity();
			if (Strings.isNullOrEmpty(relatedEntity.getTableAlias())) {
				throw new IllegalStateException(relationship + "'s related entity is missing its table alias");
			}
			
			ownerClass = ownerEntity.getEntityClass();
			ownerTableAlias = ownerEntity.getTableAlias();
			relatedClass = relatedEntity.getEntityClass();
			relatedTableAlias = relatedEntity.getTableAlias();
			this.relationship = relationship;
		}
		
		/* BEGIN Private methods */
		
		private void appendConditions(From from) {
			JoinConditionStrategy strategy = relationship.getType() == Relationship.TYPE_ONE_TO_MANY 
					? new OneToManyConditionStrategy(ownerTableAlias, relatedTableAlias) 
					: new NToOneConditionStrategy(relatedTableAlias);
			strategy.appendConditions(relationship, select, from);
		}
		
		private From createFrom(ClassRowMapping relatedClassMapping, ClassRowMapping ownerClassMapping) {
			From from = new From()
				.tableName(relatedClassMapping.getTableIdentifier())
				.as(relatedTableAlias);

			if (relationship.getType() == Relationship.TYPE_ONE_TO_MANY) {
				from.innerJoin()
					.tableName(ownerClassMapping.getTableIdentifier())
					.as(ownerTableAlias);
			}
			return from;
		}
		
		private SelectList createRelatedSelectList(ClassRowMapping relatedClassMapping) {
			initFieldColumnMappings(relatedClassMapping);
			
			SelectList list = new SelectList();
			for (FieldColumnMapping fieldColumnMapping : fieldColumnMappings) {
				list.tableAlias(relatedTableAlias)
						.column(fieldColumnMapping.getColumn());
			}
			return list;
		}
		
		private void initFieldColumnMappings(ClassRowMapping relatedClassMapping) {
			if (fieldColumnMappings != null) {
				return;
			}

			fieldColumnMappings = relatedClassMapping.getFieldColumnMappingList();
			for (int i = 0; i < fieldColumnMappings.size(); i++) {
				FieldColumnMapping fieldColumnMapping = fieldColumnMappings.get(i);
				
				if (fieldColumnMapping.isPrimaryKey()) {
					// Support for only one primary key for now
					primaryKeyColumnIndex = i;
					break;
				}
			}
		}
		
		private void initOrderBy() {
			if (relationship.getOrderByColumns().isEmpty()) {
				return;
			}
			
			OrderBy orderBy = new OrderBy();
			for (OrderByColumn column : relationship.getOrderByColumns()) {
				orderBy.tableAlias(relatedTableAlias)
						.column(column.getColumn());
				
				if (column.getSortType() == SortType.DESC) {
					orderBy.desc();
				}
			}
			select.orderBy(orderBy);
		}
		
		private void initSelect() {
			ClassRowMapping ownerClassMapping = jdbcMapper.getMappings()
					.get(ownerClass);
	  		ClassRowMapping relatedClassMapping = jdbcMapper.getMappings()
	 				.get(relatedClass);
			
	 		From from = createFrom(relatedClassMapping, ownerClassMapping);
			
			select = new Select()
					.list(createRelatedSelectList(relatedClassMapping))
					.from(from);
			appendConditions(from);
			initOrderBy();
		}
		
		/* END Private methods */
	}
	
	private class Batch {
		private final Map<Object, Object> joinColumnValueOwnerMap = new HashMap<>();
		private Set<Object> joinColumnValues;
		private final MemberAccess ownerFieldAccess;
		private final FieldColumnMapping ownerJoinColumnMapping;
		private final List<Object> owners = new ArrayList<>();
		private final ClassRowMapping relatedClassMapping;
		private final FieldColumnMapping relatedJoinColumnMapping;
		private final MemberAccess relatedMemberAccess;
		private final Relationship relationship;

		public FieldColumnMapping getRelatedJoinColumnMapping() { return relatedJoinColumnMapping; }
		
		public Batch(List<Object> owners, Relationship relationship) {
			this.owners.addAll(owners);
			this.relationship = relationship;
			
			ClassRowMapping ownerClassMapping = jdbcMapper.getMappings()
					.get(relationship.getOwnerEntity().getEntityClass());
			relatedClassMapping = jdbcMapper.getMappings()
					.get(relationship.getRelatedEntity().getEntityClass());
			
			JoinColumn joinColumn = relationship.getJoinColumns().get(0);
			
			ownerFieldAccess = getOwnerFieldAccess(relatedClassMapping);
			
			ownerJoinColumnMapping = ownerClassMapping.getFieldColumnMappings()
					.get(joinColumn.getOwnerClassColumn());
			relatedJoinColumnMapping = relatedClassMapping.getFieldColumnMappings()
					.get(joinColumn.getRelatedClassColumn());

			relatedMemberAccess = ownerClassMapping.getRelatedMemberAccess(relationship);
		}
		
		/**
		 * Does not support multiple join columns yet
		 * @param statement
		 */
		public void setParametersTo(JdbcStatement statement) {
			statement.setParameterList(
					ownerJoinColumnMapping.getColumn(),
					ownerJoinColumnMapping,
					getJoinColumnValues());
		}

		public void setReferences(Object relatedObject, Object joinColumnValue) {
			setReferences(relatedObject, joinColumnValue, joinColumnValueOwnerMap.get(joinColumnValue));
		}

		public void setReferences(Object relatedObject, Object joinColumnValue, CollectionFactory factory) {
			setReferences(relatedObject, joinColumnValue, factory, joinColumnValueOwnerMap.get(joinColumnValue));
		}

		public void setReferences(Object relatedObject, Object joinColumnValue, MapFactory factory) {
			setReferences(relatedObject, joinColumnValue, factory, joinColumnValueOwnerMap.get(joinColumnValue));
		}
		
		protected void addJoinColumnValueToOwner(Object joinColumnValue, Object owner) {
			joinColumnValueOwnerMap.put(joinColumnValue, owner);
		}
		
		protected void setReferences(Object relatedObject, Object joinColumnValue, Object owner) {
			assignOwnerField(relatedObject, owner);
			relatedMemberAccess.set(owner, relatedObject);
		}

		@SuppressWarnings("unchecked")
		protected void setReferences(
				Object relatedObject,
				Object joinColumnValue,
				CollectionFactory factory,
				Object owner) {
			assignOwnerField(relatedObject, owner);

			@SuppressWarnings("rawtypes")
			Collection collection = (Collection)relatedMemberAccess.get(owner);
			if (collection == null) {
				collection = factory.newInstance();
				relatedMemberAccess.set(owner, collection);
			}
			collection.add(relatedObject);
		}

		@SuppressWarnings("unchecked")
		protected void setReferences(
				Object relatedObject,
				Object joinColumnValue,
				MapFactory factory,
				Object owner) {
			assignOwnerField(relatedObject, owner);

			@SuppressWarnings("rawtypes")
			Map map = (Map)relatedMemberAccess.get(owner);
			if (map == null) {
				map = factory.newInstance();
				relatedMemberAccess.set(owner, map);
			}
			Object key = relatedClassMapping.getMapKeyValue(relatedObject, relationship.getMapKeyName());
			map.put(key, relatedObject);
		}
		
		private void assignOwnerField(Object relatedObject, Object owner) {
			if (ownerFieldAccess != null) {
				ownerFieldAccess.set(relatedObject, owner);
			}
		}

		private Set<Object> getJoinColumnValues() {
			if (joinColumnValues != null) {
				return joinColumnValues;
			}
			
			joinColumnValueOwnerMap.clear();
			
			this.joinColumnValues = new HashSet<>();
			
			for (Object owner : owners) {
				Object joinColumnValue = ownerJoinColumnMapping.get(owner);
				
				joinColumnValues.add(joinColumnValue);
				addJoinColumnValueToOwner(joinColumnValue, owner);
			}
			return  joinColumnValues;
		}
		
		private MemberAccess getOwnerFieldAccess(ClassRowMapping relatedClassMapping) {
			if (relationship.getInverseOwnerField() == null) {
				return null;
			}
			
			MemberAccess ownerFieldAccess = relatedClassMapping
					.getOwnerMemberAccess(relationship);
			
			if (ownerFieldAccess == null) {
				StringBuilder warning = new StringBuilder("inverse owner field (")
						.append(relationship.getInverseOwnerField())
						.append(") not found");
				logger.warn(warning.toString());
			}
			return ownerFieldAccess;
		}
	}
	
	private class BatchFactory {
		private final int batchCount;
		private final List<Object> owners = new ArrayList<>();
		private final Relationship relationship;
		
		public int getBatchCount() {
			return batchCount;
		}
		
		public BatchFactory(Relationship relationship, Collection<?> owners) {
			batchCount = (int)Math.ceil((double)owners.size() / relationship.getBatchSize());
			this.owners.addAll(owners);
			this.relationship = relationship;
		}
		
		public Batch createBatch(int batchIndex) {
			if (batchIndex >= getBatchCount()) {
				throw new IndexOutOfBoundsException("batchIndex exceeds the maximum number of batches: " + getBatchCount());
			}
			
			int batchSize = relationship.getBatchSize();
			List<Object> ownersInBatch = new ArrayList<>();
			
			int index = batchIndex * batchSize;
			
			int endIndex = index + batchSize;
			if (endIndex >= owners.size()) {
				endIndex = owners.size();
			}
			
			for (; index < endIndex; index++) {
				ownersInBatch.add(owners.get(index));
			}
			
			return relationship.getType() == Relationship.TYPE_ONE_TO_MANY 
					? new Batch(ownersInBatch, relationship)
					: new RelatedToOneBatch(ownersInBatch, relationship);
		}
	}
	
	/**
	 * Fetching related parent table objects from child records (that are the owner),
	 * we must store the child records in a list because some of them may reference the
	 * same parent object
	 * @author Albert
	 *
	 */
	private class RelatedToOneBatch extends Batch {
		private final Map<Object, List<Object>> joinColumnValueOwnersMap = new HashMap<>();
		
		public RelatedToOneBatch(List<Object> owners, Relationship relationship) {
			super(owners, relationship);
		}
		
		@Override
		protected void addJoinColumnValueToOwner(Object joinColumnValue, Object owner) {
			List<Object> owners = joinColumnValueOwnersMap.get(joinColumnValue);
			if (owners == null) {
				owners = new ArrayList<>();
				joinColumnValueOwnersMap.put(joinColumnValue, owners);
			}
			owners.add(owner);
		}
		
		@Override
		public void setReferences(Object relatedObject, Object joinColumnValue) {
			for (Object owner : joinColumnValueOwnersMap.get(joinColumnValue)) {
				setReferences(relatedObject, joinColumnValue, owner);
			}
		}
		
		@Override
		public void setReferences(Object relatedObject, Object joinColumnValue, CollectionFactory factory) {
			for (Object owner : joinColumnValueOwnersMap.get(joinColumnValue)) {
				setReferences(relatedObject, joinColumnValue, factory, owner);
			}
		}
		
		@Override
		public void setReferences(Object relatedObject, Object joinColumnValue, MapFactory factory) {
			for (Object owner : joinColumnValueOwnersMap.get(joinColumnValue)) {
				setReferences(relatedObject, joinColumnValue, factory, owner);
			}
		}
	}
	
	private class RelationshipResolver {
		private final ObjectCache objectCache;
		private final ClassRowMapping relatedClassMapping;
		private final RelatedObjectSelect relatedObjectsSelect;
		private final Relationship relationship;
		private final JdbcStatement statement;
		
		public RelationshipResolver(Relationship relationship, ObjectCache objectCache) {
			GraphEntity<?> relatedEntity = relationship.getRelatedEntity();
			
			this.objectCache = objectCache;
			
			relatedClassMapping = jdbcMapper.getMappings()
					.get(relatedEntity.getEntityClass());
			
			relatedObjectsSelect = new RelatedObjectSelect(relationship);

			this.relationship = relationship;
			
			statement = jdbcMapper.createQuery(relatedObjectsSelect.getSelect())
					.cachePreparedStatement(true);
		}
		
		@SuppressWarnings("rawtypes")
		public Collection getRelatedObjects(Connection connection, Collection<?> owners) 
				throws SQLException {
			List<?> relatedObjects = null;
			
			try {
				switch (relationship.getFieldType()) {
					case Relationship.FIELD_DEQUE:
						relatedObjects = getCollections(connection, owners, CollectionUtils.FACTORY_DEQUE);
						break;
					case Relationship.FIELD_LINKED_MAP:
						relatedObjects = getMaps(connection, owners, CollectionUtils.FACTORY_LINKED_MAP);
						break;
					case Relationship.FIELD_LINKED_SET:
						relatedObjects = getCollections(connection, owners, CollectionUtils.FACTORY_LINKED_SET);
						break;
					case Relationship.FIELD_LIST:
						relatedObjects = getCollections(connection, owners, CollectionUtils.FACTORY_LIST);
						break;
					case Relationship.FIELD_MAP:
						relatedObjects = getMaps(connection, owners, CollectionUtils.FACTORY_MAP);
						break;
					case Relationship.FIELD_SET:
						relatedObjects = getCollections(connection, owners, CollectionUtils.FACTORY_SET);
						break;
					case Relationship.FIELD_UNIQUE:
						relatedObjects = uniqueResults(connection, owners);
						break;
				}
			} catch (SQLException e) {
				throw e;
			} finally {
				statement.closePreparedStatement();
			}
			
			return relatedObjects;
		}
		
		/* BEGIN Private methods */
		
		@SuppressWarnings("rawtypes")
		private List getCollections(
				Connection connection, 
				Collection<?> owners, 
				CollectionFactory factory) throws SQLException {
			BatchFactory batchFactory = new BatchFactory(relationship, owners);
			List<Object> objects = new ArrayList<>();
			
			for (int i = 0; i < batchFactory.getBatchCount(); i++) {
				Batch batch = batchFactory.createBatch(i);
				batch.setParametersTo(statement);
				queryBatchOfCollections(connection, batch, objects, factory);
			}
			return objects;
		}
		
		@SuppressWarnings("rawtypes")
		private List getMaps(
				Connection connection, 
				Collection<?> owners, 
				MapFactory factory) throws SQLException {
			BatchFactory batchFactory = new BatchFactory(relationship, owners);
			List<Object> mapValues = new ArrayList<>();
			
			for (int i = 0; i < batchFactory.getBatchCount(); i++) {
				Batch batch = batchFactory.createBatch(i);
				batch.setParametersTo(statement);
				queryBatchOfMaps(connection, batch, mapValues, factory);
			}
			return mapValues;
		}
		
		private Object getRelatedObject(ResultSetHelper rs) throws SQLException {
			Object relatedObject = newRelatedObject();
			
			List<FieldColumnMapping> fieldColumnMappings = relatedObjectsSelect.getFieldColumnMappings();
			for (int i = 0; i < fieldColumnMappings.size(); i++) {
				FieldColumnMapping fieldColumnMapping = fieldColumnMappings.get(i);
				Object value = fieldColumnMapping.getFromResultSet(rs);
				fieldColumnMapping.set(relatedObject, value);
				
				if (i != relatedObjectsSelect.getPrimaryKeyColumnIndex()) {
					continue;
				}
				Serializable id = (Serializable)value;
				
				Object existingObject = objectCache.get(relatedClassMapping, id);
				if (existingObject == null) {
					objectCache.add(relatedObject, id);
					continue;
				}
				return existingObject;
			}
			
			return relatedObject;
		}
		
		private Object newRelatedObject() {
			try {
				return relatedClassMapping.getClazz()
						.newInstance();
			} catch (InstantiationException | IllegalAccessException e) {}
			return null;
		}

		private void queryBatchOfCollections(
				Connection connection, 
				Batch batch, 
				List<Object> objects, 
				CollectionFactory factory) throws SQLException {
			ResultSetHelper rs = null;
			
			try {
				PreparedStatement stmt = statement.getPreparedStatement(connection);
				
				rs = new ResultSetHelper(stmt.executeQuery());
				while (rs.next()) {
					FieldColumnMapping joinColumnMapping = batch.getRelatedJoinColumnMapping();
					
					Object joinValue = joinColumnMapping.getFromResultSet(rs);
					Object relatedObject = getRelatedObject(rs);
					
					batch.setReferences(relatedObject, joinValue, factory);
					
					objects.add(relatedObject);
				}
			} catch (SQLException e) {
				throw e;
			} finally {
				JdbcUtils.closeQuietly(rs);
			}
		}
		
		private void queryBatchOfMaps(
				Connection connection, 
				Batch batch, 
				List<Object> mapValues, 
				MapFactory factory) throws SQLException {
			ResultSetHelper rs = null;
			
			try {
				PreparedStatement stmt = statement.getPreparedStatement(connection);
				
				rs = new ResultSetHelper(stmt.executeQuery());
				while (rs.next()) {
					FieldColumnMapping joinColumnMapping = batch.getRelatedJoinColumnMapping();
					
					Object joinValue = joinColumnMapping.getFromResultSet(rs);
					Object relatedObject = getRelatedObject(rs);
					
					batch.setReferences(relatedObject, joinValue, factory);
					
					mapValues.add(relatedObject);
				}
			} catch (SQLException e) {
				throw e;
			} finally {
				JdbcUtils.closeQuietly(rs);
			}
		}
		
		private void queryBatchOfUniques(
				Connection connection, 
				Batch batch, 
				List<Object> uniqueResults) throws SQLException {
			ResultSetHelper rs = null;
			
			try {
				PreparedStatement stmt = statement.getPreparedStatement(connection);
				
				rs = new ResultSetHelper(stmt.executeQuery());
				while (rs.next()) {
					FieldColumnMapping joinColumnMapping = batch.getRelatedJoinColumnMapping();
					
					Object joinValue = joinColumnMapping.getFromResultSet(rs);
					Object relatedObject = getRelatedObject(rs);
					
					batch.setReferences(relatedObject, joinValue);
					
					uniqueResults.add(relatedObject);
				}
			} catch (SQLException e) {
				throw e;
			} finally {
				JdbcUtils.closeQuietly(rs);
			}
		}
		
		@SuppressWarnings("rawtypes")
		private List uniqueResults(
				Connection connection, 
				Collection<?> owners) throws SQLException {
			BatchFactory batchFactory = new BatchFactory(relationship, owners);
			List<Object> uniqueResults = new ArrayList<>();
			
			for (int i = 0; i < batchFactory.getBatchCount(); i++) {
				Batch batch = batchFactory.createBatch(i);
				batch.setParametersTo(statement);
				queryBatchOfUniques(connection, batch, uniqueResults);
			}
			return uniqueResults;
		}
		
		/* END Private methods */
	}
	
	/* END Inner classes */
}