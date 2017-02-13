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
import java.util.Iterator;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;
import java.util.Set;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.github.javalbert.orm.Relationship.JoinColumn;
import com.github.javalbert.orm.Relationship.OrderByColumn;
import com.github.javalbert.sqlbuilder.Condition;
import com.github.javalbert.sqlbuilder.From;
import com.github.javalbert.sqlbuilder.Node;
import com.github.javalbert.sqlbuilder.NodeVisitor;
import com.github.javalbert.sqlbuilder.OrderBy;
import com.github.javalbert.sqlbuilder.Predicate;
import com.github.javalbert.sqlbuilder.Select;
import com.github.javalbert.sqlbuilder.SelectList;
import com.github.javalbert.sqlbuilder.SelectNodeFinder;
import com.github.javalbert.sqlbuilder.SortType;
import com.github.javalbert.sqlbuilder.Where;
import com.github.javalbert.utils.collections.CollectionFactory;
import com.github.javalbert.utils.collections.CollectionUtils;
import com.github.javalbert.utils.collections.MapFactory;
import com.github.javalbert.utils.jdbc.JdbcUtils;
import com.github.javalbert.utils.jdbc.ResultSetHelper;
import com.github.javalbert.utils.reflection.MemberAccess;
import com.github.javalbert.utils.string.Strings;

/**
 * Limitations for <code>CartesianProductResolver.toCollection(...)</code>method:
 * <ul>
 * <li>Ignores ORDER BY clause from <b>statement</b> parameter</li>
 * <li>Copies the WHERE clause from <b>statement</b> parameter</li>
 * <li>Assumes that any columns specified in the WHERE clause in <b>statement</b> parameter 
 * that does <i>not</i> have an table alias, is part of the <b>clazz</b> parameter</li>
 * </ul>
 * @author Albert
 *
 */
public class CartesianProductResolver extends ObjectGraphResolver {
	private static final Logger logger = LoggerFactory.getLogger(CartesianProductResolver.class);
	
	public CartesianProductResolver(JdbcMapper jdbcMapper) {
		super(jdbcMapper);
	}
	
	@Override
	public <T> void resolveRelatedObjects(
			Connection connection, 
			GraphEntity<T> graphEntity, 
			Collection<T> collection) throws SQLException {
		RelatedObjectsQuery query = new RelatedObjectsQuery(graphEntity);
		JdbcStatement statement = query.createStatement(collection);
		toCollection(connection, statement, graphEntity, collection, query.getObjectCache());
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
			Collection<T> collection) throws SQLException {
		return toCollection(connection, statement, graphEntity, collection, null);
	}
	
	@Override
	public <T> Collection<T> toCollection(
			Connection connection, 
			JdbcStatement statement, 
			GraphEntity<T> graphEntity, 
			Collection<T> collection, 
			ObjectCache objectCache) throws SQLException {
		CartesianProductQuery query = new CartesianProductQuery(statement, graphEntity);
		
		PreparedStatement stmt = null;
		ResultSetHelper rs = null;
		try {
			stmt = statement.createPreparedStatement(connection);
			rs = new ResultSetHelper(stmt.executeQuery());
			return query.toCollection(collection, rs, objectCache);
		} catch (SQLException e) {
			throw e;
		} finally {
			JdbcUtils.closeQuietly(rs);
			JdbcUtils.closeQuietly(stmt);
		}
	}
	
	/* BEGIN Inner classes */
	
	private static class CreateObjectResult {
		public static final CreateObjectResult NO_CREATE = new CreateObjectResult(null, false);
		
		private final Object object;
		private final boolean isNew;
		
		public Object getObject() { return object; }
		public boolean isNew() { return isNew; }
		
		public CreateObjectResult(Object object, boolean isNew) {
			this.isNew = isNew;
			this.object = object;
		}
	}
	
	private class CartesianProductQuery {
		@SuppressWarnings("rawtypes")
		private final Map<GraphEntity, ClassColumns> entityColumnsMap = new LinkedHashMap<>();
		private final From from = new From();
		@SuppressWarnings("rawtypes")
		private final GraphEntity mainGraphEntity;
		private int nextColumnStartIndex = 1;
		private OrderBy orderBy = new OrderBy();
		private final SelectList selectList = new SelectList();
		private final SelectNodeFinder selectNodeFinder = new SelectNodeFinder();

		@SuppressWarnings("rawtypes")
		public Map<GraphEntity, ClassColumns> getEntityColumnsMap() { return entityColumnsMap; }
		@SuppressWarnings("rawtypes")
		public GraphEntity getMainGraphEntity() { return mainGraphEntity; }
		
		public CartesianProductQuery(JdbcStatement statement, GraphEntity<?> mainGraphEntity) {
			this.mainGraphEntity = mainGraphEntity;
			
			statement.getSqlStatement().accept(selectNodeFinder);
			
			ClassRowMapping classRowMapping = jdbcMapper.getMappings()
					.get(mainGraphEntity.getEntityClass());
			
			addMainEntitySelectList(statement);
			ClassColumns classColumns = new ClassColumnsFactory(classRowMapping)
					.create(statement);
			entityColumnsMap.put(mainGraphEntity, classColumns);
			
			from.tableName(classRowMapping.getTableIdentifier())
					.as(mainGraphEntity.getTableAlias());
			
			addMainEntityOrderBy(classRowMapping);
			
			for (Relationship relationship : mainGraphEntity.getRelationships()) {
				addRelationshipToSelect(relationship);
			}
			
			changeSelectStatement(statement);
		}
		
		public <T> Collection<T> toCollection(Collection<T> collection, ResultSetHelper rs, ObjectCache objectCache) throws SQLException {
			CartesianProductResult result = new CartesianProductResult(this, objectCache);
			return result.toCollection(collection, rs);
		}
		
		/* BEGIN Private methods */

		private void addMainEntityOrderBy(ClassRowMapping classRowMapping) {
			OrderBy statementOrderBy = selectNodeFinder.getOrderBy();
			
			if (statementOrderBy != null) {
				for (@SuppressWarnings("rawtypes") Node node : statementOrderBy.getNodes()) {
					if (node.getType() == Node.TYPE_COLUMN) {
						com.github.javalbert.sqlbuilder.Column column = (com.github.javalbert.sqlbuilder.Column)node;
						orderBy.tableAlias(mainGraphEntity.getTableAlias())
								.column(column.getName());
					} else if (node == SortType.DESC) {
						orderBy.desc();
					} else if (node == SortType.ASC) {
						orderBy.asc();
					}
				}
			} else {
				for (FieldColumnMapping primaryKeyMapping : classRowMapping.getPrimaryKeyMappings()) {
					orderBy.tableAlias(mainGraphEntity.getTableAlias())
							.column(primaryKeyMapping.getColumn());
				}
			}
		}
		
		private void addMainEntitySelectList(JdbcStatement statement) {
			SelectList list = selectNodeFinder.getSelectList();
			
			for (@SuppressWarnings("rawtypes") Node node : list.getNodes()) {
				com.github.javalbert.sqlbuilder.Column column = (com.github.javalbert.sqlbuilder.Column)node;
				selectList.tableAlias(mainGraphEntity.getTableAlias())
						.column(column.getName());
			}
		}
		
		private void addRelationshipsOfRelated(Relationship related) {
			@SuppressWarnings("unchecked")
			Set<Relationship> relationships = related.getRelatedEntity()
					.getRelationships();
			for (Relationship relationship : relationships) {
				addRelationshipToSelect(relationship);
			}
		}
		
		private void addRelationshipToSelect(Relationship relationship) {
			boolean added = addSelectColumns(relationship);
			
			if (!added) {
				return;
			}
			appendToFrom(relationship);
			appendToOrderBy(relationship);
			
			addRelationshipsOfRelated(relationship);
		}
		
		private boolean addSelectColumns(Relationship relationship) {
			GraphEntity<?> relatedEntity = relationship.getRelatedEntity();
			
			if (entityColumnsMap.containsKey(relatedEntity)) {
				return false;
			}
			ClassRowMapping classRowMapping = jdbcMapper.getMappings()
					.get(relatedEntity.getEntityClass());
			
			appendColumns(relatedEntity);
			ClassColumns classColumns = new ClassColumnsFactory(classRowMapping)
					.create(relationship);
			entityColumnsMap.put(relatedEntity, classColumns);
			
			return true;
		}
		
		private void appendColumns(GraphEntity<?> graphEntity) {
			ClassRowMapping classRowMapping = jdbcMapper.getMappings()
					.get(graphEntity.getEntityClass());
			List<FieldColumnMapping> fieldColumnMappingList = classRowMapping.getFieldColumnMappingList();
			
			for (FieldColumnMapping fieldColumnMapping : fieldColumnMappingList) {
				selectList.tableAlias(graphEntity.getTableAlias())
						.column(fieldColumnMapping.getColumn());
			}
		}
		
		private Where appendTableAlias(Where where) {
			where.accept(new TableAliasAppender());
			return where;
		}
		
		private void appendToFrom(Relationship relationship) {
			Condition joinCondition = new Condition();
			GraphEntity<?> ownerEntity = relationship.getOwnerEntity();
			GraphEntity<?> relatedEntity = relationship.getRelatedEntity();
			
			for (JoinColumn joinColumn : relationship.getJoinColumns()) {
				joinCondition.predicate(new Predicate()
					.tableAlias(ownerEntity.getTableAlias()).column(joinColumn.getOwnerClassColumn())
					.eq()
					.tableAlias(relatedEntity.getTableAlias()).column(joinColumn.getRelatedClassColumn())
				);
			}
			
			ClassRowMapping classRowMapping = jdbcMapper.getMappings()
					.get(relatedEntity.getEntityClass());
			
			from.leftOuterJoin()
			.tableName(classRowMapping.getTableIdentifier())
			.as(relatedEntity.getTableAlias())
			.on(joinCondition);
		}

		private void appendToOrderBy(Relationship relationship) {
			if (relationship.getType() != Relationship.TYPE_ONE_TO_MANY 
					|| relationship.getOrderByColumns().isEmpty()) {
				return;
			}
			GraphEntity<?> relatedEntity = relationship.getRelatedEntity();
			
			for (OrderByColumn column : relationship.getOrderByColumns()) {
				orderBy.tableAlias(relatedEntity.getTableAlias())
						.column(column.getColumn());
				
				if (column.getSortType() == SortType.DESC) {
					orderBy.desc();
				} else {
					orderBy.asc();
				}
			}
		}
		
		private void appendWhereClause(Select select, JdbcStatement statement) {
			if (selectNodeFinder.getWhere() == null) {
				return;
			}
			Where where = appendTableAlias(selectNodeFinder.getWhere().mutable());
			select.where(where);
		}

		private void changeSelectStatement(JdbcStatement statement) {
			Select select = new Select()
			.list(selectList)
			.from(from);
			appendWhereClause(select, statement);
			select.orderBy(orderBy);
			
			statement.sqlStatement(select);
		}
		
		/* END Private methods */
		
		/* BEGIN Inner classes */
		
		private class ClassColumnsFactory {
			private final ClassRowMapping classRowMapping;
			private final int startIndex;
			
			public ClassColumnsFactory(ClassRowMapping classRowMapping) {
				this.classRowMapping = classRowMapping;
				startIndex = nextColumnStartIndex;
			}
			
			public ClassColumns create(Relationship relationship) {
				List<FieldColumnMapping> fieldColumnMappings = classRowMapping.getFieldColumnMappingList();
				return newInstance(fieldColumnMappings, relationship);
			}
			
			public ClassColumns create(JdbcStatement statement) {
				List<FieldColumnMapping> fieldColumnMappings = jdbcMapper.getColumnMappings(classRowMapping, (Select)statement.getSqlStatement());
				return newInstance(fieldColumnMappings);
			}
			
			private ClassColumns newInstance(List<FieldColumnMapping> fieldColumnMappings) {
				return newInstance(fieldColumnMappings, null);
			}
			
			private ClassColumns newInstance(List<FieldColumnMapping> fieldColumnMappings, Relationship relationship) {
				int endIndex = startIndex + fieldColumnMappings.size();
		
				ClassColumns classColumns = new ClassColumns(classRowMapping, fieldColumnMappings, startIndex, endIndex, relationship);
				nextColumnStartIndex = endIndex;
				return classColumns;
			}
		}
		
		private class TableAliasAppender implements NodeVisitor {
			@Override
			public boolean visit(@SuppressWarnings("rawtypes") Node node) {
				if (node.getType() == Node.TYPE_COLUMN) {
					com.github.javalbert.sqlbuilder.Column column = (com.github.javalbert.sqlbuilder.Column)node;
					appendTableAlias(column);
				}
				return true;
			}
			
			private void appendTableAlias(com.github.javalbert.sqlbuilder.Column column) {
				if (column.getPrefix() != null) {
					return;
				}
				if (Strings.isNullOrEmpty(column.getPrefixValue())) {
					column.setPrefixValue(mainGraphEntity.getTableAlias());
				}
			}
		}
		
		/* END Inner classes */
	}
	
	private class CartesianProductResult {
		private final ObjectCache objectCache;
		private final CartesianProductQuery query;
		
		public CartesianProductResult(CartesianProductQuery query, ObjectCache objectCache) {
			this.objectCache = objectCache != null ? objectCache : new ObjectCache();
			this.query = query;
		}

		@SuppressWarnings("rawtypes")
		public <T> Collection<T> toCollection(Collection<T> collection, ResultSetHelper rs) throws SQLException {
			Map<GraphEntity, ClassColumns> entityColumnsMap = query.getEntityColumnsMap();
			Collection<ClassColumns> classColumnsList = entityColumnsMap.values();
			RelationshipManager relationshipManager = new RelationshipManager(query, classColumnsList.size());
			
			while (rs.next()) {
				for (ClassColumns classColumns : classColumnsList) {
					CreateObjectResult result = createObject(classColumns, rs);
					relationshipManager.manage(classColumns, result);
					addObjectToCollection(collection, result);
				}
			}
			return collection;
		}
		
		/* BEGIN Private methods */
		
		@SuppressWarnings("unchecked")
		private void addObjectToCollection(@SuppressWarnings("rawtypes") Collection collection, CreateObjectResult result) {
			Object object = result.getObject();
			
			if (!result.isNew() 
					|| query.getMainGraphEntity().getEntityClass() != object.getClass()) {
				return;
			}
			collection.add(object);
		}
		
		private Object createInstance(ClassColumns classColumns) {
			try {
				return classColumns.getClassRowMapping()
						.getClazz()
						.newInstance();
			} catch (InstantiationException | IllegalAccessException e) {
				return null;
			}
		}
		
		private CreateObjectResult createObject(ClassColumns classColumns, ResultSetHelper rs) throws SQLException {
			ClassRowMapping classRowMapping = classColumns.getClassRowMapping();
			List<FieldColumnMapping> fieldColumnMappings = classColumns.getFieldColumnMappings();
			Iterator<FieldColumnMapping> fieldColumnMappingIterator = fieldColumnMappings.iterator();
			Object instance = createInstance(classColumns);
			int nullColumns = 0;
			int retrievedColumns = 0;
			
			for (int column = classColumns.getStartIndex(); column < classColumns.getEndIndex(); column++) {
				FieldColumnMapping fieldColumnMapping = fieldColumnMappingIterator.next();
				final Object value = fieldColumnMapping.getFromResultSet(rs, column);
				
				if (value == null) {
					nullColumns++;
				}
				retrievedColumns++;
				
				fieldColumnMapping.set(instance, value);
				
				// Having the primary key as the first fields in the entity class is recommended
				// as a quick way to extract the primary key from the object
				if (column == classColumns.getLastPrimaryKeyColumn()) {
					if (retrievedColumns == nullColumns) {
						return CreateObjectResult.NO_CREATE;
					}
					
					Object existing = objectCache.get(classRowMapping, instance);
					if (existing != null) {
						return new CreateObjectResult(existing, false);
					}
				}
			}

			objectCache.add(classRowMapping, instance);
			return new CreateObjectResult(instance, true);
		}
		
		/* END Private methods */
	}
	
	/**
	 * Represents the columns and indices to access from the <code>java.sql.ResultSet</code> for a particular class
	 * @author Albert
	 *
	 */
	private class ClassColumns {
		private final ClassRowMapping classRowMapping;
		/**
		 * upper-bound exclusive
		 */
		private final int endIndex;
		private final List<FieldColumnMapping> fieldColumnMappings;
		private int lastPrimaryKeyColumn;
		private final Relationship relationship;
		private final int startIndex;
		
		public ClassRowMapping getClassRowMapping() { return classRowMapping; }
		public int getEndIndex() { return endIndex; }
		public List<FieldColumnMapping> getFieldColumnMappings() { return fieldColumnMappings; }
		public int getLastPrimaryKeyColumn() { return lastPrimaryKeyColumn; }
		public Relationship getRelationship() { return relationship; }
		public int getStartIndex() { return startIndex; }

		public ClassColumns(
				ClassRowMapping classRowMapping, 
				List<FieldColumnMapping> fieldColumnMappings, 
				int startIndex, 
				int endIndex, 
				Relationship relationship) {
			this.classRowMapping = classRowMapping;
			this.endIndex = endIndex;
			this.fieldColumnMappings = Collections.unmodifiableList(fieldColumnMappings);
			this.relationship = relationship;
			this.startIndex = startIndex;
			
			findPrimaryKeyMappings();
		}
		
		private void findPrimaryKeyMappings() {
			List<FieldColumnMapping> fieldColumnMappings = classRowMapping.getFieldColumnMappingList();
			
			for (int i = 0; i < fieldColumnMappings.size(); i++) {
				FieldColumnMapping fieldColumnMapping = fieldColumnMappings.get(i);
				
				if (!fieldColumnMapping.isPrimaryKey()) {
					continue;
				}
				int column = startIndex + i;
				
				if (column > lastPrimaryKeyColumn) {
					lastPrimaryKeyColumn = column;
				}
			}
		}
	}
	
	private class RelatedObjectsQuery {
		private final ClassRowMapping classRowMapping;
		private ObjectCache objectCache;
		private FieldColumnMapping scalarPrimaryKeyMapping;
		private Select select;
		
		public ObjectCache getObjectCache() { return objectCache; }
		
		public RelatedObjectsQuery(GraphEntity<?> graphEntity) {
			classRowMapping = jdbcMapper.getMappings()
					.get(graphEntity.getEntityClass());
			
			Set<String> columnsAdded = new HashSet<>();
			SelectList list = new SelectList();
			Where where = new Where();
			
			addPrimaryKeyColumns(list, where, graphEntity, columnsAdded, classRowMapping);
			addJoinColumns(list, graphEntity, columnsAdded);
			
			select = new Select()
			.list(list)
			.from(new From()
				.tableName(classRowMapping.getTableIdentifier())
				.as(graphEntity.getTableAlias())
			).where(where);
		}
		
		public JdbcStatement createStatement(Collection<?> collection) {
			Collection<?> idList = getIdList(classRowMapping, collection);
			
			return jdbcMapper.createQuery(select)
					.setParameterList(scalarPrimaryKeyMapping.getColumn(), 
							scalarPrimaryKeyMapping, 
							idList);
		}

		/* BEGIN Private methods */
		
		private void addJoinColumns(SelectList list, GraphEntity<?> graphEntity, Set<String> columnsAdded) {
			for (Relationship relationship : graphEntity.getRelationships()) {
				for (JoinColumn joinColumn : relationship.getJoinColumns()) {
					if (columnsAdded.contains(joinColumn.getOwnerClassColumn())) {
						continue;
					}
					
					list.tableAlias(graphEntity.getTableAlias())
							.column(joinColumn.getOwnerClassColumn());
					columnsAdded.add(joinColumn.getOwnerClassColumn());
				}
			}
		}
		
		private void addPrimaryKeyColumns(
				SelectList list, 
				Where where, 
				GraphEntity<?> graphEntity, 
				Set<String> columnsAdded, 
				ClassRowMapping classRowMapping) {
			List<FieldColumnMapping> primaryKeyMappings = classRowMapping.getPrimaryKeyMappings();
			
			if (classRowMapping.isScalarPrimaryKey()) {
				scalarPrimaryKeyMapping = primaryKeyMappings.get(0);
				
				list.tableAlias(graphEntity.getTableAlias())
						.column(scalarPrimaryKeyMapping.getColumn());
				where.predicate(new Predicate()
					.tableAlias(graphEntity.getTableAlias())
					.column(scalarPrimaryKeyMapping.getColumn())
					.in()
					.param(scalarPrimaryKeyMapping.getColumn())
				);
				
				columnsAdded.add(scalarPrimaryKeyMapping.getColumn());
			} else if (classRowMapping.isCompositePrimaryKey()) {
				throw new IllegalArgumentException(CartesianProductResolver.class.getSimpleName() 
						+ " does not support composite keys for resolveRelatedObjects(...) methods");
			} else {
				throw new IllegalArgumentException(graphEntity + " does not have a primary key");
			}
		}
		
		@SuppressWarnings({ "rawtypes", "unchecked" })
		private Collection getIdList(ClassRowMapping classRowMapping, Collection<?> collection) {
			objectCache = new ObjectCache();
			
			List ids = new ArrayList<>();
			for (Object object : collection) {
				Serializable id = classRowMapping.getOrCreateId(object);
				ids.add(id);
				
				objectCache.add(object, id);
			}
			return ids;
		}
		
		/* END Private methods */
	}
	
	private class RelationshipLink {
		@SuppressWarnings("rawtypes")
		private final Class clazz;
		private final Serializable id;
		@SuppressWarnings("rawtypes")
		private final Class ownerClass;
		private final Serializable ownerId;
		
		public RelationshipLink(Class<?> ownerClass, Serializable ownerId, Class<?> clazz, Serializable id) {
			this.clazz = clazz;
			this.id = id;
			this.ownerClass = ownerClass;
			this.ownerId = ownerId;
		}

		@Override
		public int hashCode() {
			final int prime = 31;
			int result = 1;
			result = prime * result + ((clazz == null) ? 0 : clazz.hashCode());
			result = prime * result + ((id == null) ? 0 : id.hashCode());
			result = prime * result + ((ownerClass == null) ? 0 : ownerClass.hashCode());
			result = prime * result + ((ownerId == null) ? 0 : ownerId.hashCode());
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
			RelationshipLink other = (RelationshipLink) obj;
			if (clazz == null) {
				if (other.clazz != null)
					return false;
			} else if (!clazz.equals(other.clazz))
				return false;
			if (id == null) {
				if (other.id != null)
					return false;
			} else if (!id.equals(other.id))
				return false;
			if (ownerClass == null) {
				if (other.ownerClass != null)
					return false;
			} else if (!ownerClass.equals(other.ownerClass))
				return false;
			if (ownerId == null) {
				if (other.ownerId != null)
					return false;
			} else if (!ownerId.equals(other.ownerId))
				return false;
			return true;
		}
	}
	
	private class RelationshipManager {
		private final Map<ClassColumns, Object> classObjects = new HashMap<>();
		@SuppressWarnings("rawtypes")
		private final Map<GraphEntity, ClassColumns> entityColumnsMap;
		private final int objectsPerRow;
		private final Set<RelationshipLink> relationshipLinks = new HashSet<>();
		
		public RelationshipManager(CartesianProductQuery query, int objectsPerRow) {
			entityColumnsMap = query.getEntityColumnsMap();
			this.objectsPerRow = objectsPerRow;
		}

		public void manage(ClassColumns classColumns, CreateObjectResult result) {
			classObjects.put(classColumns, result.getObject());
			
			if (classObjects.size() == objectsPerRow) {
				handleRelationships();
			}
		}
		
		private void handleRelationships() {
			for (Entry<ClassColumns, Object> classObjectEntry : classObjects.entrySet()) {
				Object object = classObjectEntry.getValue();
				
				if (object == null) {
					continue;
				}
				
				ClassColumns classColumns = classObjectEntry.getKey();
				handleRelationship(classColumns, object);
			}
			classObjects.clear();
		}
		
		private void handleRelationship(ClassColumns classColumns, Object object) {
			Relationship relationship = classColumns.getRelationship();
			
			if (relationship == null) {
				return;
			}
			
			ClassColumns ownerColumns = entityColumnsMap.get(relationship.getOwnerEntity());
			Object owner = classObjects.get(ownerColumns);
			
			if (isRelationshipEstablished(owner, ownerColumns, object, classColumns)) {
				return;
			}
			
			switch (relationship.getFieldType()) {
				case Relationship.FIELD_DEQUE:
					addToCollection(ownerColumns, owner, classColumns, object, CollectionUtils.FACTORY_DEQUE);
					break;
				case Relationship.FIELD_LINKED_MAP:
					addToMap(relationship, ownerColumns, owner, classColumns, object, CollectionUtils.FACTORY_LINKED_MAP);
					break;
				case Relationship.FIELD_LINKED_SET:
					addToCollection(ownerColumns, owner, classColumns, object, CollectionUtils.FACTORY_LINKED_SET);
					break;
				case Relationship.FIELD_LIST:
					addToCollection(ownerColumns, owner, classColumns, object, CollectionUtils.FACTORY_LIST);
					break;
				case Relationship.FIELD_MAP:
					addToMap(relationship, ownerColumns, owner, classColumns, object, CollectionUtils.FACTORY_MAP);
					break;
				case Relationship.FIELD_SET:
					addToCollection(ownerColumns, owner, classColumns, object, CollectionUtils.FACTORY_SET);
					break;
				case Relationship.FIELD_UNIQUE:
					addToField(ownerColumns, owner, classColumns, object);
					break;
			}
			
			assignOwnerField(relationship, classColumns, object, owner);
		}

		private void assignOwnerField(Relationship relationship, ClassColumns classColumns, Object object, Object owner) {
			if (relationship.getInverseOwnerField() == null) {
				return;
			}
			
			MemberAccess ownerField = classColumns.getClassRowMapping()
					.getOwnerMemberAccess(relationship);
			if (ownerField != null) {
				ownerField.set(object, owner);
			} else {
				StringBuilder warning = new StringBuilder("inverse owner field (")
						.append(relationship.getInverseOwnerField())
						.append(") not found");
				logger.warn(warning.toString());
			}
		}

		@SuppressWarnings("unchecked")
		private void addToCollection(ClassColumns ownerColumns, 
				Object owner, 
				ClassColumns classColumns, 
				Object object, 
				CollectionFactory collectionFactory) {
			MemberAccess collectionMember = ownerColumns.getClassRowMapping()
					.getRelatedMemberAccess(classColumns.getRelationship());
			
			@SuppressWarnings("rawtypes")
			Collection collection = (Collection)collectionMember.get(owner);
			if (collection == null) {
				collection = collectionFactory.newInstance();
				collectionMember.set(owner, collection);
			}
			collection.add(object);
		}
		
		private void addToField(ClassColumns ownerColumns, Object owner, ClassColumns classColumns, Object object) {
			MemberAccess fieldMember = ownerColumns.getClassRowMapping()
					.getRelatedMemberAccess(classColumns.getRelationship());
			fieldMember.set(owner, object);
		}
		
		@SuppressWarnings("unchecked")
		private void addToMap(Relationship relationship, 
				ClassColumns ownerColumns, 
				Object owner, 
				ClassColumns classColumns, 
				Object object, 
				MapFactory mapFactory) {
			MemberAccess mapMember = ownerColumns.getClassRowMapping()
					.getRelatedMemberAccess(classColumns.getRelationship());
			
			@SuppressWarnings("rawtypes")
			Map map = (Map)mapMember.get(owner);
			if (map == null) {
				map = mapFactory.newInstance();
				mapMember.set(owner, map);
			}

			Object key = classColumns.getClassRowMapping()
					.getMapKeyValue(object, relationship.getMapKeyName());
			map.put(key, object);
		}
		
		private boolean isRelationshipEstablished(Object owner, 
				ClassColumns ownerColumns, 
				Object object, 
				ClassColumns classColumns) {
			Serializable ownerId = ownerColumns.getClassRowMapping()
					.getOrCreateId(owner);
			Serializable id = classColumns.getClassRowMapping()
					.getOrCreateId(object);
			
			RelationshipLink link = new RelationshipLink(owner.getClass(), ownerId, object.getClass(), id);
			return !relationshipLinks.add(link);
		}
	}
	
	/* END Inner classes */
}