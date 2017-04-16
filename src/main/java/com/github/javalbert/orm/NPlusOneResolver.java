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
import java.sql.PreparedStatement;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Objects;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.github.javalbert.orm.Relationship.JoinColumn;
import com.github.javalbert.orm.Relationship.OrderByColumn;
import com.github.javalbert.sqlbuilder.Condition;
import com.github.javalbert.sqlbuilder.From;
import com.github.javalbert.sqlbuilder.Node;
import com.github.javalbert.sqlbuilder.OrderBy;
import com.github.javalbert.sqlbuilder.Predicate;
import com.github.javalbert.sqlbuilder.Prefix;
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

public class NPlusOneResolver extends ObjectGraphResolver {
	/* BEGIN Class members */
	
	private static final Logger logger = LoggerFactory.getLogger(NPlusOneResolver.class);
	
	private static List<FieldColumnMapping> getJoinColumnMappings(
			Relationship relationship, 
			ClassRowMapping ownerClassMapping) {
		List<FieldColumnMapping> joinColumnMappings = new ArrayList<>();
		
		for (JoinColumn joinColumn : relationship.getJoinColumns()) {
			FieldColumnMapping joinColumnMapping = ownerClassMapping.getFieldColumnMappings()
					.get(joinColumn.getOwnerClassColumn());
			joinColumnMappings.add(joinColumnMapping);
		}
		return Collections.unmodifiableList(joinColumnMappings);
	}

	/* END Class members */
	
	public NPlusOneResolver(JdbcMapper jdbcMapper) {
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
			Objects.requireNonNull(graphEntity, "graphEntity cannot be null");
			
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
				fieldColumnMappings.get(i).setFromResultSet(object, rs, i + 1);
				
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
		
		public void resolveRelationships(GraphEntity<?> graphEntity, Collection<?> collection) 
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
				return null;
			}

			EntityColumns entityColumns = new EntityColumns(relatedEntity, objectCache);
			entityColumnsMap.put(relatedEntity, entityColumns);
			return entityColumns;
		}
		
		private void resolveRelationships(Connection connection, GraphEntity<?> graphEntity, Collection<?> collection) 
				throws SQLException {
			for (Relationship relationship : graphEntity.getRelationships()) {
				EntityColumns entityColumns = createRelatedEntityColumns(relationship);
				
				if (entityColumns == null) {
					continue;
				}
				
				RelationshipResolver resolver = new RelationshipResolver(entityColumns, relationship);
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
			for (int i = 0; i < joinColumns.size(); i++) {
				if (i > 0) {
					where.and();
				}

				JoinColumn joinColumn = joinColumns.get(i);
				appendJoinCondition(joinColumn, where);
			}
			
			select.where(where);
		}
		
		private void appendJoinCondition(JoinColumn joinColumn, Where where) {
			where.predicate(new Predicate()
				.tableAlias(relatedTableAlias).column(joinColumn.getRelatedClassColumn())
				.eq()
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
			for (int i = 0; i < joinColumns.size(); i++) {
				if (i > 0) {
					joinCondition.and();
					where.and();
				}

				JoinColumn joinColumn = joinColumns.get(i);
				appendJoinCondition(joinColumn, joinCondition, where);
			}
			
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
				.eq()
				.param(joinColumn.getOwnerClassColumn())
			);
		}
	};
	
	private class RelatedObjectSelect {
		@SuppressWarnings("rawtypes")
		private final Class ownerClass;
		private final String ownerTableAlias;
		@SuppressWarnings("rawtypes")
		private final Class relatedClass;
		private final String relatedTableAlias;
		private final Relationship relationship;
		private Select select;
		
		public Select getSelect() {
			if (select == null) {
				initSelect();
			}
			return select;
		}
		
		public RelatedObjectSelect(Relationship relationship) {
			GraphEntity<?> ownerEntity = relationship.getOwnerEntity();
			GraphEntity<?> relatedEntity = relationship.getRelatedEntity();
			
			ownerClass = ownerEntity.getEntityClass();
			ownerTableAlias = Strings.illegalStateOnEmpty(ownerEntity.getTableAlias(),
					relationship + "'s owner entity is missing its table alias");
			relatedClass = relatedEntity.getEntityClass();
			relatedTableAlias = Strings.illegalStateOnEmpty(relatedEntity.getTableAlias(),
					relationship + "'s related entity is missing its table alias");
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
			SelectList list = relatedClassMapping.getSelectList().mutable();
			for (@SuppressWarnings("rawtypes") Node node : list.getNodes()) {
				com.github.javalbert.sqlbuilder.Column column = (com.github.javalbert.sqlbuilder.Column)node;
				column.setPrefix(Prefix.TABLE_ALIAS);
				column.setPrefixValue(relatedTableAlias);
			}
			return list;
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
	
	private class RelationshipResolver {
		private final List<FieldColumnMapping> joinColumnMappings;
		private final MemberAccess ownerFieldAccess;
		private final ClassRowMapping relatedClassMapping;
		private final EntityColumns relatedEntityColumns;
		private final MemberAccess relatedMemberAccess;
		private final Relationship relationship;
		private final JdbcStatement statement;
		
		public RelationshipResolver(EntityColumns relatedEntityColumns, Relationship relationship) {
			GraphEntity<?> relatedEntity = relationship.getRelatedEntity();

			relatedClassMapping = jdbcMapper.getMappings()
					.get(relatedEntity.getEntityClass());
			
			this.relatedEntityColumns = relatedEntityColumns;
			this.relationship = relationship;
			
			ClassRowMapping ownerClassMapping = jdbcMapper.getMappings()
					.get(relationship.getOwnerEntity().getEntityClass());
			
			joinColumnMappings = getJoinColumnMappings(relationship, ownerClassMapping);
			
			ownerFieldAccess = getOwnerFieldAccess();
			
			relatedMemberAccess = ownerClassMapping.getRelatedMemberAccess(relationship);
			
			Select relatedObjectsSelect = new RelatedObjectSelect(relationship)
					.getSelect();
			statement = jdbcMapper.createQuery(relatedObjectsSelect)
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
		
		private void assignOwnerField(Object object, Object owner) {
			if (ownerFieldAccess == null) {
				return;
			}
			
			ownerFieldAccess.set(object, owner);
		}
		
		@SuppressWarnings({ "unchecked", "rawtypes" })
		private List getCollections(
				Connection connection, 
				Collection<?> owners, 
				CollectionFactory factory) throws SQLException {
			List<?> objects = new ArrayList<>();
			
			for (Object owner : owners) {
				setJoinParameters(owner);
				Collection collection = toCollection(connection, owner, factory.newInstance());
				relatedMemberAccess.set(owner, collection);
				objects.addAll(collection);
			}
			return objects;
		}
		
		@SuppressWarnings({ "unchecked", "rawtypes" })
		private List getMaps(
				Connection connection, 
				Collection<?> owners, 
				MapFactory factory) throws SQLException {
			String mapKeyName = relationship.getMapKeyName();
			List mapValues = new ArrayList<>();
			
			for (Object owner : owners) {
				setJoinParameters(owner);
				Map<?, ?> map = toMap(connection, mapKeyName, factory.newInstance());
				relatedMemberAccess.set(owner, map);
				mapValues.addAll(map.values());
			}
			return mapValues;
		}
		
		private MemberAccess getOwnerFieldAccess() {
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
		
		private <T> void setJoinParameters(T owner) {
			for (FieldColumnMapping joinColumnMapping : joinColumnMappings) {
				statement.setParameterFrom(owner, joinColumnMapping);
			}
		}
		
		@SuppressWarnings({ "rawtypes", "unchecked" })
		private Collection toCollection(Connection connection, Object owner, Collection collection) 
				throws SQLException {
			PreparedStatement stmt = null;
			ResultSetHelper rs = null;
			
			try {
				stmt = statement.getPreparedStatement(connection);
				rs = new ResultSetHelper(stmt.executeQuery());
				
				while (rs.next()) {
					Object object = relatedEntityColumns.createFromResultSet(rs);
					collection.add(object);
					assignOwnerField(object, owner);
				}
				
				return collection;
			} catch (SQLException e) {
				throw e;
			} finally {
				JdbcUtils.closeQuietly(rs);
			}
		}
		
		@SuppressWarnings({ "unchecked", "rawtypes" })
		private Map toMap(Connection connection, Object owner, Map map) 
				throws SQLException {
			PreparedStatement stmt = null;
			ResultSetHelper rs = null;
			
			try {
				stmt = statement.getPreparedStatement(connection);
				rs = new ResultSetHelper(stmt.executeQuery());
				
				while (rs.next()) {
					Object object = relatedEntityColumns.createFromResultSet(rs);
					Object key = relatedClassMapping.getMapKeyValue(object, relationship.getMapKeyName());
					map.put(key, object);
					assignOwnerField(object, owner);
				}
				
				return map;
			} catch (SQLException e) {
				throw e;
			} finally {
				JdbcUtils.closeQuietly(rs);
			}
		}
		
		private Object uniqueResult(Connection connection, Object owner) 
				throws SQLException {
			PreparedStatement stmt = null;
			ResultSetHelper rs = null;
			
			try {
				stmt = statement.getPreparedStatement(connection);
				rs = new ResultSetHelper(stmt.executeQuery());
				
				Object object = null;
				if (rs.next()) {
					object = relatedEntityColumns.createFromResultSet(rs);
					assignOwnerField(object, owner);
				}
				
				if (rs.next()) {
					throw new SQLException("non-unique result for " + relationship.getRelatedEntity());
				}
				return object;
			} catch (SQLException e) {
				throw e;
			} finally {
				JdbcUtils.closeQuietly(rs);
			}
		}
		
		@SuppressWarnings({ "unchecked", "rawtypes" })
		private List uniqueResults(
				Connection connection, 
				Collection<?> owners) throws SQLException {
			List uniqueResults = new ArrayList<>();
			
			for (Object owner : owners) {
				setJoinParameters(owner);
				Object relatedObject = uniqueResult(connection, owner);
				relatedMemberAccess.set(owner, relatedObject);
				uniqueResults.add(relatedObject);
			}
			
			return uniqueResults;
		}
		
		/* END Private methods */
	}
	
	/* END Inner classes */
}