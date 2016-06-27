/*******************************************************************************
 * Copyright (c) 2016 Albert Shun-Dat Chan
 *
 * Licensed under MIT License
 * See LICENSE file at the root of the project or
 * https://github.com/Javalbert/sql-builder-orm/blob/master/LICENSE
 *******************************************************************************/
package chan.shundat.albert.orm;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import chan.shundat.albert.orm.Relationship.JoinColumn;
import chan.shundat.albert.orm.Relationship.OrderByColumn;
import chan.shundat.albert.sqlbuilder.Condition;
import chan.shundat.albert.sqlbuilder.From;
import chan.shundat.albert.sqlbuilder.Node;
import chan.shundat.albert.sqlbuilder.OrderBy;
import chan.shundat.albert.sqlbuilder.Predicate;
import chan.shundat.albert.sqlbuilder.Prefix;
import chan.shundat.albert.sqlbuilder.Select;
import chan.shundat.albert.sqlbuilder.SelectList;
import chan.shundat.albert.sqlbuilder.SortType;
import chan.shundat.albert.sqlbuilder.Where;
import chan.shundat.albert.utils.collections.CollectionFactory;
import chan.shundat.albert.utils.collections.CollectionUtils;
import chan.shundat.albert.utils.collections.MapFactory;
import chan.shundat.albert.utils.jdbc.JdbcUtils;
import chan.shundat.albert.utils.jdbc.ResultSetHelper;
import chan.shundat.albert.utils.reflection.MemberAccess;
import chan.shundat.albert.utils.string.Strings;

@SuppressWarnings({ "rawtypes", "unchecked" })
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
	public <T> void resolveRelatedObjects(Connection connection, 
			GraphEntity graphEntity, 
			Collection<T> collection) throws SQLException {
		EntityQuery query = new EntityQuery(connection);
		query.resolveRelationships(graphEntity, collection);
	}
	
	@Override
	public void resolveRelatedObjects(Connection connection, 
			GraphEntity graphEntity, 
			Object object) throws SQLException {
		resolveRelatedObjects(connection, graphEntity, Collections.singletonList(object));
	}
	
	@Override
	public <T, C extends Collection<T>> C toCollection(
			Connection connection, 
			JdbcStatement statement, 
			GraphEntity graphEntity, 
			C collection) 
			throws SQLException {
		return toCollection(connection, statement, graphEntity, collection, null);
	}
	
	@Override
	public <T, C extends Collection<T>> C toCollection(Connection connection, 
			JdbcStatement statement,
			GraphEntity graphEntity, 
			C collection, 
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
		private final GraphEntity graphEntity;
		private int lastPrimaryKeyIndex;
		private final ObjectCache objectCache;
		
		public EntityColumns(GraphEntity graphEntity, ObjectCache objectCache) {
			this(graphEntity, objectCache, null);
		}
		
		public EntityColumns(GraphEntity graphEntity, ObjectCache objectCache, JdbcStatement statement) {
			if (graphEntity == null) {
				throw new NullPointerException("graphEntity cannot be null");
			}
			
			classRowMapping = jdbcMapper.getMappings()
					.get(graphEntity.getClazz());
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
				return graphEntity.getClazz().newInstance();
			} catch (InstantiationException | IllegalAccessException e) {
				return null;
			}
		}
	}
	
	private class EntityQuery {
		private final Connection connection;
		private final Map<GraphEntity, EntityColumns> entityColumnsMap = new HashMap<>();
		private final ObjectCache objectCache;
		
		public EntityQuery(Connection connection) {
			this(connection, null);
		}
		
		public EntityQuery(Connection connection, ObjectCache objectCache) {
			this.connection = connection;
			this.objectCache = objectCache != null ? objectCache : new ObjectCache();
		}
		
		public void resolveRelationships(GraphEntity graphEntity, Collection collection) 
				throws SQLException {
			resolveRelationships(connection, graphEntity, collection);
		}
		
		public <C extends Collection<T>, T> C toCollection(GraphEntity graphEntity, JdbcStatement statement, C collection) 
			throws SQLException {
			PreparedStatement stmt = null;
			ResultSetHelper rs = null;
			
			try {
				stmt = statement.createPreparedStatement(connection);
				rs = new ResultSetHelper(stmt.executeQuery());
				
				EntityColumns entityColumns = new EntityColumns(graphEntity, objectCache, statement);
				entityColumnsMap.put(graphEntity, entityColumns);
				
				while (rs.next()) {
					T entity = (T)entityColumns.createFromResultSet(rs);
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
			final GraphEntity relatedEntity = relationship.getRelatedEntity();
			
			if (entityColumnsMap.containsKey(relatedEntity)) {
				return null;
			}

			EntityColumns entityColumns = new EntityColumns(relatedEntity, objectCache);
			entityColumnsMap.put(relatedEntity, entityColumns);
			return entityColumns;
		}
		
		private void resolveRelationships(Connection connection, GraphEntity graphEntity, Collection collection) 
				throws SQLException {
			for (Relationship relationship : graphEntity.getRelationships()) {
				EntityColumns entityColumns = createRelatedEntityColumns(relationship);
				
				if (entityColumns == null) {
					continue;
				}
				
				RelationshipResolver resolver = new RelationshipResolver(entityColumns, relationship);
				Collection relatedObjects = resolver.getRelatedObjects(connection, collection);
				
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
		private final Class ownerClass;
		private final String ownerTableAlias;
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
			GraphEntity ownerEntity = relationship.getOwnerEntity();
			if (Strings.isNullOrEmpty(ownerEntity.getTableAlias())) {
				throw new IllegalStateException(relationship + "'s owner entity is missing its table alias");
			}
			
			GraphEntity relatedEntity = relationship.getRelatedEntity();
			if (Strings.isNullOrEmpty(relatedEntity.getTableAlias())) {
				throw new IllegalStateException(relationship + "'s related entity is missing its table alias");
			}
			
			ownerClass = ownerEntity.getClazz();
			ownerTableAlias = ownerEntity.getTableAlias();
			relatedClass = relatedEntity.getClazz();
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
			SelectList list = relatedClassMapping.getSelectList().mutable();
			for (Node node : list.getNodes()) {
				chan.shundat.albert.sqlbuilder.Column column = (chan.shundat.albert.sqlbuilder.Column)node;
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
			GraphEntity relatedEntity = relationship.getRelatedEntity();

			relatedClassMapping = jdbcMapper.getMappings()
					.get(relatedEntity.getClazz());
			
			this.relatedEntityColumns = relatedEntityColumns;
			this.relationship = relationship;
			
			ClassRowMapping ownerClassMapping = jdbcMapper.getMappings()
					.get(relationship.getOwnerEntity().getClazz());
			
			joinColumnMappings = getJoinColumnMappings(relationship, ownerClassMapping);
			
			ownerFieldAccess = getOwnerFieldAccess();
			
			relatedMemberAccess = ownerClassMapping.getRelatedMemberAccess(relationship);
			
			Select relatedObjectsSelect = new RelatedObjectSelect(relationship)
					.getSelect();
			statement = jdbcMapper.createQuery(relatedObjectsSelect)
					.cachePreparedStatement(true);
		}
		
		public Collection getRelatedObjects(Connection connection, Collection owners) 
				throws SQLException {
			List relatedObjects = null;
			
			try {
				switch (relationship.getFieldType()) {
					case Relationship.FIELD_LINKED_LIST:
						relatedObjects = getCollections(connection, owners, CollectionUtils.FACTORY_LINKED_LIST);
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
					case Relationship.FIELD_STACK:
						relatedObjects = getCollections(connection, owners, CollectionUtils.FACTORY_STACK);
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
		
		private List getCollections(Connection connection, 
				Collection owners, 
				CollectionFactory factory) throws SQLException {
			List objects = new ArrayList<>();
			
			for (Object owner : owners) {
				setJoinParameters(owner);
				Collection collection = toCollection(connection, owner, factory.newInstance());
				relatedMemberAccess.set(owner, collection);
				objects.addAll(collection);
			}
			return objects;
		}
		
		private List getMaps(Connection connection, 
				Collection owners, 
				MapFactory factory) throws SQLException {
			String mapKeyName = relationship.getMapKeyName();
			List mapValues = new ArrayList<>();
			
			for (Object owner : owners) {
				setJoinParameters(owner);
				Map map = toMap(connection, mapKeyName, factory.newInstance());
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
		
		private Map toMap(Connection connection, Object owner, Map map) 
				throws SQLException {
			PreparedStatement stmt = null;
			ResultSetHelper rs = null;
			
			try {
				stmt = statement.getPreparedStatement(connection);
				rs = new ResultSetHelper(stmt.executeQuery());
				
				while (rs.next()) {
					Object object = relatedEntityColumns.createFromResultSet(rs);
					Object key = relatedClassMapping.getMapKeyValue(relationship.getMapKeyName(), object);
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
		
		private List uniqueResults(Connection connection, 
				Collection owners) throws SQLException {
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