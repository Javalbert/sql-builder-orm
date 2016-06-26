package chan.shundat.albert.orm;

import java.io.Serializable;
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
import chan.shundat.albert.sqlbuilder.OrderBy;
import chan.shundat.albert.sqlbuilder.Predicate;
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
public class BatchResolver extends ObjectGraphResolver {
	/* BEGIN Class members */
	
	private static final Logger logger = LoggerFactory.getLogger(BatchResolver.class);

	private static void assertSingleJoinColumn(List<JoinColumn> joinColumns) {
		if (joinColumns.size() > 1) {
			throw new IllegalArgumentException(BatchResolver.class.getSimpleName() 
					+ " does not support multiple join columns in the relationships");
		}
	}
	
	private static List<FieldColumnMapping> getJoinColumnMappings(
			Relationship relationship, 
			ClassRowMapping ownerClassMapping) {
		List<JoinColumn> joinColumns = relationship.getJoinColumns();
		assertSingleJoinColumn(joinColumns);
		
		JoinColumn joinColumn = joinColumns.get(0);
		FieldColumnMapping joinColumnMapping = ownerClassMapping.getFieldColumnMappings()
				.get(joinColumn.getOwnerClassColumn());

		List<FieldColumnMapping> joinColumnMappings = new ArrayList<>();
		joinColumnMappings.add(joinColumnMapping);
		return Collections.unmodifiableList(joinColumnMappings);
	}

	/* END Class members */
	
	public BatchResolver(JdbcMapper jdbcMapper) {
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
				
				RelationshipResolver resolver = new RelationshipResolver(relationship, objectCache);
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
		private final Class ownerClass;
		private final String ownerTableAlias;
		private int primaryKeyColumnIndex;
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
		private FieldColumnMapping joinColumnMapping;
		private Map<Object, Object> joinColumnValueOwnerMap = new HashMap<>();
		private List<Object> joinColumnValues;
		private final List<Object> owners = new ArrayList<>();

		public FieldColumnMapping getJoinColumnMapping() { return joinColumnMapping; }
		
		public void setJoinColumnMapping(FieldColumnMapping joinColumnMapping) {
			this.joinColumnMapping = joinColumnMapping;
			initJoinColumnValues();
		}

		public List<Object> getJoinColumnValues() {
			return joinColumnValues != null 
					? Collections.unmodifiableList(joinColumnValues) 
					: null;
		}
		
		public Batch(List<Object> owners) {
			this.owners.addAll(owners);
		}
		
		public Object getOwnerOfJoinColumnValue(Object joinColumnValue) {
			return joinColumnValueOwnerMap.get(joinColumnValue);
		}

		private void initJoinColumnValues() {
			joinColumnValueOwnerMap.clear();
			
			List<Object> joinColumnValues = new ArrayList<>();
			
			for (Object owner : owners) {
				Object joinColumnValue = joinColumnMapping.get(owner);
				
				joinColumnValues.add(joinColumnValue);
				joinColumnValueOwnerMap.put(joinColumnValue, owner);
			}
			
			this.joinColumnValues = joinColumnValues;
		}
	}
	
	private class BatchFactory {
		private final int batchCount;
		private final List<Object> owners = new ArrayList<>();
		private final Relationship relationship;
		
		public int getBatchCount() {
			return batchCount;
		}
		
		public BatchFactory(Relationship relationship, Collection owners) {
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
			
			return new Batch(ownersInBatch);
		}
	}
	
	private class RelationshipResolver {
		private final List<FieldColumnMapping> joinColumnMappings;
		private final ObjectCache objectCache;
		private final MemberAccess ownerFieldAccess;
		private final ClassRowMapping relatedClassMapping;
		private final MemberAccess relatedMemberAccess;
		private final RelatedObjectSelect relatedObjectsSelect;
		private final Relationship relationship;
		private final JdbcStatement statement;
		
		public RelationshipResolver(Relationship relationship, ObjectCache objectCache) {
			ClassRowMapping ownerClassMapping = jdbcMapper.getMappings()
					.get(relationship.getOwnerEntity().getClazz());
			GraphEntity relatedEntity = relationship.getRelatedEntity();

			joinColumnMappings = getJoinColumnMappings(relationship, ownerClassMapping);

			this.objectCache = objectCache;
			
			relatedClassMapping = jdbcMapper.getMappings()
					.get(relatedEntity.getClazz());
			
			relatedObjectsSelect = new RelatedObjectSelect(relationship);

			relatedMemberAccess = ownerClassMapping.getRelatedMemberAccess(relationship);
			
			this.relationship = relationship;
			ownerFieldAccess = getOwnerFieldAccess();
			
			statement = jdbcMapper.createQuery(relatedObjectsSelect.getSelect())
					.cachePreparedStatement(true);
		}
		
		public Collection getRelatedObjects(Connection connection, Collection owners) 
				throws SQLException {
			List<Object> relatedObjects = null;
			
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
			BatchFactory batchFactory = new BatchFactory(relationship, owners);
			List<Object> objects = new ArrayList<>();
			
			for (int i = 0; i < batchFactory.getBatchCount(); i++) {
				Batch batch = batchFactory.createBatch(i);
				setJoinParameters(batch);
				queryBatchOfCollections(connection, batch, objects, factory);
			}
			return objects;
		}
		
		private List getMaps(Connection connection, 
				Collection owners, 
				MapFactory factory) throws SQLException {
			BatchFactory batchFactory = new BatchFactory(relationship, owners);
			List<Object> mapValues = new ArrayList<>();
			
			for (int i = 0; i < batchFactory.getBatchCount(); i++) {
				Batch batch = batchFactory.createBatch(i);
				setJoinParameters(batch);
				queryBatchOfMaps(connection, batch, mapValues, factory);
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

		private void queryBatchOfCollections(Connection connection, 
				Batch batch, 
				List<Object> objects, 
				CollectionFactory factory) throws SQLException {
			ResultSetHelper rs = null;
			
			try {
				PreparedStatement stmt = statement.getPreparedStatement(connection);
				
				rs = new ResultSetHelper(stmt.executeQuery());
				while (rs.next()) {
					FieldColumnMapping joinColumnMapping = batch.getJoinColumnMapping();
					
					Object joinValue = joinColumnMapping.getFromResultSet(rs);
					Object relatedObject = getRelatedObject(rs);
					
					Object owner = batch.getOwnerOfJoinColumnValue(joinValue);
					
					assignOwnerField(relatedObject, owner);

					Collection collection = (Collection)relatedMemberAccess.get(owner);
					if (collection == null) {
						collection = factory.newInstance();
						relatedMemberAccess.set(owner, collection);
					}
					collection.add(relatedObject);
					
					objects.add(relatedObject);
				}
			} catch (SQLException e) {
				throw e;
			} finally {
				JdbcUtils.closeQuietly(rs);
			}
		}
		
		private void queryBatchOfMaps(Connection connection, 
				Batch batch, 
				List<Object> mapValues, 
				MapFactory factory) throws SQLException {
			ResultSetHelper rs = null;
			
			try {
				PreparedStatement stmt = statement.getPreparedStatement(connection);
				
				rs = new ResultSetHelper(stmt.executeQuery());
				while (rs.next()) {
					FieldColumnMapping joinColumnMapping = batch.getJoinColumnMapping();
					
					Object joinValue = joinColumnMapping.getFromResultSet(rs);
					Object relatedObject = getRelatedObject(rs);
					
					Object owner = batch.getOwnerOfJoinColumnValue(joinValue);
					
					assignOwnerField(relatedObject, owner);

					Map map = (Map)relatedMemberAccess.get(owner);
					if (map == null) {
						map = factory.newInstance();
						relatedMemberAccess.set(owner, map);
					}
					Object key = relatedClassMapping.getMapKeyValue(relationship.getMapKeyName(), relatedObject);
					map.put(key, relatedObject);
					
					mapValues.add(relatedObject);
				}
			} catch (SQLException e) {
				throw e;
			} finally {
				JdbcUtils.closeQuietly(rs);
			}
		}
		
		private void queryBatchOfUniques(Connection connection, 
				Batch batch, 
				List<Object> uniqueResults) throws SQLException {
			ResultSetHelper rs = null;
			
			try {
				PreparedStatement stmt = statement.getPreparedStatement(connection);
				
				rs = new ResultSetHelper(stmt.executeQuery());
				while (rs.next()) {
					FieldColumnMapping joinColumnMapping = batch.getJoinColumnMapping();
					
					Object joinValue = joinColumnMapping.getFromResultSet(rs);
					Object relatedObject = getRelatedObject(rs);
					
					Object owner = batch.getOwnerOfJoinColumnValue(joinValue);
					
					assignOwnerField(relatedObject, owner);

					relatedMemberAccess.set(owner, relatedObject);
					uniqueResults.add(relatedObject);
				}
			} catch (SQLException e) {
				throw e;
			} finally {
				JdbcUtils.closeQuietly(rs);
			}
		}
		
		/**
		 * Does not support multiple join columns yet
		 * @param batch
		 */
		private void setJoinParameters(Batch batch) {
			FieldColumnMapping joinColumnMapping = joinColumnMappings.get(0);
			batch.setJoinColumnMapping(joinColumnMapping);
			statement.setParameterList(joinColumnMapping.getColumn(), joinColumnMapping, batch.getJoinColumnValues());
		}
		
		private List uniqueResults(Connection connection, 
				Collection owners) throws SQLException {
			BatchFactory batchFactory = new BatchFactory(relationship, owners);
			List<Object> uniqueResults = new ArrayList<>();
			
			for (int i = 0; i < batchFactory.getBatchCount(); i++) {
				Batch batch = batchFactory.createBatch(i);
				setJoinParameters(batch);
				queryBatchOfUniques(connection, batch, uniqueResults);
			}
			return uniqueResults;
		}
		
		/* END Private methods */
	}
	
	/* END Inner classes */
}