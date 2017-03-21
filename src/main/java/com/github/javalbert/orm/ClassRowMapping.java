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
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import com.github.javalbert.sqlbuilder.ColumnList;
import com.github.javalbert.sqlbuilder.ColumnValues;
import com.github.javalbert.sqlbuilder.Delete;
import com.github.javalbert.sqlbuilder.Expression;
import com.github.javalbert.sqlbuilder.From;
import com.github.javalbert.sqlbuilder.Insert;
import com.github.javalbert.sqlbuilder.Predicate;
import com.github.javalbert.sqlbuilder.Select;
import com.github.javalbert.sqlbuilder.SelectList;
import com.github.javalbert.sqlbuilder.SetValue;
import com.github.javalbert.sqlbuilder.SetValues;
import com.github.javalbert.sqlbuilder.Update;
import com.github.javalbert.sqlbuilder.Where;
import com.github.javalbert.sqlbuilder.vendor.ANSI;
import com.github.javalbert.sqlbuilder.vendor.Vendor;
import com.github.javalbert.utils.jdbc.ResultSetHelper;
import com.github.javalbert.utils.reflection.MemberAccess;
import com.github.javalbert.utils.string.Strings;

public class ClassRowMapping {
	private static void appendColumnEqualsParam(
			Where where,
			FieldColumnMapping fieldColumnMapping,
			boolean appendAnd) {
		if (appendAnd) {
			where.and();
		}
		
		where.predicate(new Predicate()
			.column(fieldColumnMapping.getColumn()).eq().param(fieldColumnMapping.getColumn())
		);
	}
	
	private FieldColumnMapping autoIncrementIdMapping;
	private String catalog;
	@SuppressWarnings("rawtypes")
	private final Class clazz;
	private Delete deleteById;
	private Delete deleteByIdAndVersion;
	private final Map<String, FieldColumnMapping> fieldAliasMappings;
	private final List<FieldColumnMapping> fieldColumnMappingList;
	private final Map<String, FieldColumnMapping> fieldColumnMappings;
	private From from;
	@SuppressWarnings("rawtypes")
	private Class idClass;
	private List<FieldColumnMapping> idClassMappings;
	private Insert insert;
	private final Map<String, FieldColumnMapping> mapKeyMappings = new HashMap<>();
	private final List<FieldColumnMapping> primaryKeyMappings = new ArrayList<>();
	private final Map<String, MemberAccess> relatedMemberAccessMap;
	private String schema;
	private Select select;
	private Select selectById;
	private SelectList selectList;
	private String table;
	private String tableIdentifier;
	private Update updateById;
	private final Vendor vendor;
	private FieldColumnMapping versionColumnMapping;
	
	public String getCatalog() { return catalog; }
	@SuppressWarnings("rawtypes")
	public Class getClazz() { return clazz; }
	public Delete getDeleteById() { return deleteById; }
	public Delete getDeleteByIdAndVersion() { return deleteByIdAndVersion; }
	public Map<String, FieldColumnMapping> getFieldAliasMappings() { return fieldAliasMappings; }
	public List<FieldColumnMapping> getFieldColumnMappingList() { return fieldColumnMappingList; }
	public Map<String, FieldColumnMapping> getFieldColumnMappings() { return fieldColumnMappings; }
	public From getFrom() { return from; }
	public Insert getInsert() { return insert; }
	public List<FieldColumnMapping> getPrimaryKeyMappings() { return Collections.unmodifiableList(primaryKeyMappings); }
	public String getSchema() { return schema; }
	public Select getSelect() { return select; }
	public Select getSelectById() { return selectById; }
	public SelectList getSelectList() { return selectList; }
	public String getTable() { return table; }
	public String getTableIdentifier() { return tableIdentifier; }
	public Update getUpdateById() { return updateById; }
	public Vendor getVendor() { return vendor; }
	
	// Annotation-based registration
	//
	public ClassRowMapping(@SuppressWarnings("rawtypes") Class clazz) {
		this(clazz, ANSI.INSTANCE);
	}
	public ClassRowMapping(@SuppressWarnings("rawtypes") Class clazz, Vendor vendor) {
		this(clazz, vendor, new AnnotatedClassMapper(clazz, vendor));
	}
	
	// Custom registration
	public ClassRowMapping(ClassRowRegistration registration) {
		this(registration, ANSI.INSTANCE);
	}
	public ClassRowMapping(ClassRowRegistration registration, Vendor vendor) {
		this(registration.getRegisteringClass(), vendor, new RegisterClassMapper(registration));
	}
	
	// The real constructor
	private ClassRowMapping(
			@SuppressWarnings("rawtypes") Class clazz,
			Vendor vendor,
			ClassRowMapper classRowMapper) {
		this.clazz = clazz;
		this.vendor = vendor;

		classRowMapper.map();
		
		this.fieldAliasMappings = classRowMapper.getFieldAliasMappings();
		this.fieldColumnMappingList = classRowMapper.getFieldColumnMappingList();
		this.fieldColumnMappings = classRowMapper.getFieldColumnMappings();
		this.idClass = classRowMapper.getIdClass();
		this.idClassMappings = classRowMapper.getIdClassMappings();
		this.relatedMemberAccessMap = classRowMapper.getRelatedMemberAccessMap();
		
		this.catalog = classRowMapper.getCatalog();
		this.schema = classRowMapper.getSchema();
		this.table = classRowMapper.getTable();
		this.tableIdentifier = classRowMapper.getTableIdentifier();
		
		initPrimaryKeyData();
		initMapKeys();
		
		initDeleteById();
		initInsert();
		initSelectStatements();
		initUpdateById();
	}

	public Object getMapKeyValue(Object object) {
		return getMapKeyValue(object, null);
	}
	
	public Object getMapKeyValue(Object object, String mapKeyName) {
		boolean pkMapKey = mapKeyName == null;
		
		if (pkMapKey) {
			return getOrCreateId(object);
		}
		FieldColumnMapping mapKeyMapping = mapKeyMappings.get(mapKeyName);
		
		if (mapKeyMapping == null) {
			throw new IllegalStateException("mapKeyName (" + mapKeyName 
					+ ") does not exist for object class (" + object.getClass() + ")");
		}
		return mapKeyMapping.get(object);
	}

	public Serializable getOrCreateId(Object object) {
		if (isScalarPrimaryKey()) {
			FieldColumnMapping primaryKeyMapping = primaryKeyMappings.get(0);
			return (Serializable)primaryKeyMapping.get(object);
		} else if (isCompositePrimaryKey()) {
			return createId(object);
		} else {
			throw createInvalidPrimaryKeyStateException();
		}
	}
	
	public MemberAccess getOwnerMemberAccess(Relationship relationship) {
		return relatedMemberAccessMap.get(relationship.getInverseOwnerField());
	}

	public MemberAccess getRelatedMemberAccess(Relationship relationship) {
		return relatedMemberAccessMap.get(relationship.getFieldName());
	}
	
	public boolean hasVersionControl() {
		return versionColumnMapping != null;
	}
	
	public void incrementVersion(Object object) {
		if (hasVersionControl()) {
			Integer version = (Integer)versionColumnMapping.get(object);
			versionColumnMapping.set(object, ++version);
		}
	}
	
	public boolean isAutoIncrementId() {
		return autoIncrementIdMapping != null;
	}
	
	public boolean isCompositePrimaryKey() {
		return idClass != null;
	}
	
	public boolean isScalarPrimaryKey() {
		return primaryKeyMappings.size() == 1;
	}
	
	/**
	 * ResultSet cursor will be moved to next
	 * @param object
	 * @param generatedKeys {@link ResultSet} from calling {@link PreparedStatement#getGeneratedKeys()}
	 * @throws SQLException 
	 */
	public void setAutoIncrementId(Object object, ResultSetHelper generatedKeys) throws SQLException {
		if (generatedKeys.next()) {
			autoIncrementIdMapping.setFromResultSet(object, generatedKeys, 1);
		}
	}

	public void setIdParameters(JdbcStatement statement, Serializable id) {
		if (isScalarPrimaryKey()) {
			FieldColumnMapping primaryKeyMapping = primaryKeyMappings.get(0);
			statement.setParameter(primaryKeyMapping, id);
		} else if (id.getClass() == idClass) {
			for (FieldColumnMapping idClassMapping : idClassMappings) {
				Object key = idClassMapping.get(id);
				statement.setParameter(idClassMapping, key);
			}
		} else {
			throw createInvalidPrimaryKeyStateException();
		}
	}

	public void setIdParameters(JdbcStatement statement, Object object) {
		for (FieldColumnMapping primaryKeyMapping : primaryKeyMappings) {
			Object key = primaryKeyMapping.get(object);
			statement.setParameter(primaryKeyMapping, key);
		}
	}

	public void setVersion(Object object) {
		if (hasVersionControl()) {
			versionColumnMapping.set(object, 0);
		}
	}
	
	public void setVersionParameter(JdbcStatement statement, Object object) {
		if (hasVersionControl()) {
			Object version = versionColumnMapping.get(object);
			statement.setParameter(versionColumnMapping, version);
		}
	}
	
	/* BEGIN Private methods */
 
	private Serializable createId(Object object) {
		Serializable id = null;
		try {
			id = (Serializable)idClass.newInstance();
		} catch (InstantiationException | IllegalAccessException e) {}
		
		for (int i = 0; i < primaryKeyMappings.size(); i++) {
			FieldColumnMapping primaryKeyMapping = primaryKeyMappings.get(i);
			Object key = primaryKeyMapping.get(object);
			
			FieldColumnMapping idClassMapping = idClassMappings.get(i);
			idClassMapping.set(id, key);
		}
		return id;
	}
	
	private RuntimeException createInvalidPrimaryKeyStateException() {
		return new IllegalStateException("There is 0 PK columns, or there are at least 2 or more PK columns and class ("
				+ clazz + ") is missing ID class data");
	}

	private Where createWhereById() {
		return createWhereById(false);
	}
	
	private Where createWhereById(boolean appendVersion) {
		Where where = new Where();
		
		for (int i = 0; i < primaryKeyMappings.size(); i++) {
			FieldColumnMapping primaryKeyMapping = primaryKeyMappings.get(i);
			appendColumnEqualsParam(where, primaryKeyMapping, i > 0);
		}
		
		if (appendVersion) {
			appendVersionToWhere(where);
		}
		return where;
	}
	
	private void appendVersionToWhere(Where where) {
		if (!hasVersionControl()) {
			return;
		}
		
		appendColumnEqualsParam(where, versionColumnMapping, true);
	}
	
	private SetValues createUpdateByIdSetValues() {
		SetValues values = new SetValues();
		
		for (FieldColumnMapping fieldColumnMapping : fieldColumnMappingList) {
			if (fieldColumnMapping.isAutoIncrementId()) {
				continue;
			}
			
			SetValue value = new SetValue()
					.column(fieldColumnMapping.getColumn());
			if (fieldColumnMapping.isVersion()) {
				value.expression(new Expression()
					.column(fieldColumnMapping.getColumn()).plus().literal(1)
				);
			} else {
				value.param(fieldColumnMapping.getColumn());
			}
			values.add(value);
		}
		
		return values;
	}
	
	private void initDeleteById() {
		deleteById = new Delete()
		.tableName(tableIdentifier)
		// DELETE FROM JOIN syntax not ANSI standard
//		.from(new From()
//			.tableName(tableIdentifier)
//		)
		.where(createWhereById())
		.immutable();
		
		deleteByIdAndVersion = new Delete()
		.tableName(tableIdentifier)
		.where(createWhereById(true))
		.immutable();
	}
	
	private void initInsert() {
		if (Strings.isNullOrEmpty(tableIdentifier)) {
			return;
		}
		
		insert = new Insert().into(tableIdentifier);
		setInsertColumnsAndValues(insert);
		insert = insert.immutable();
	}

	/**
	 * Must be called after <code>initPrimaryKeyData()</code> to register primary key 
	 * into <code>mapKeyMappings</code> with null key
	 */
	private void initMapKeys() {
		for (FieldColumnMapping fieldColumnMapping : fieldColumnMappingList) {
			mapKeyMappings.put(fieldColumnMapping.getMapKeyName(), fieldColumnMapping);
		}
		for (FieldColumnMapping fieldAliasMapping : fieldAliasMappings.values()) {
			mapKeyMappings.put(fieldAliasMapping.getMapKeyName(), fieldAliasMapping);
		}
		
		if (isScalarPrimaryKey()) {
			FieldColumnMapping fieldColumnMapping = primaryKeyMappings.get(0);
			mapKeyMappings.put(null, fieldColumnMapping);
		}
	}

	private void initPrimaryKeyData() {
		for (FieldColumnMapping fieldColumnMapping : fieldColumnMappingList) {
			initPrimaryKeyData(fieldColumnMapping);
			initVersionColumn(fieldColumnMapping);
		}
	}

	private void initPrimaryKeyData(FieldColumnMapping fieldColumnMapping) {
		if (!fieldColumnMapping.isPrimaryKey()) {
			return;
		}
		
		if (fieldColumnMapping.isAutoIncrementId()) {
			if (autoIncrementIdMapping == null) {
				autoIncrementIdMapping = fieldColumnMapping;
			} else {
				throw new IllegalStateException("class (" + clazz + ") has two auto-increment fields");
			}
		}
		
		primaryKeyMappings.add(fieldColumnMapping);
	}
	
	private void initSelectList() {
		selectList = new SelectList();
		for (FieldColumnMapping fieldColumnMapping : fieldColumnMappingList) {
			selectList.column(fieldColumnMapping.getColumn());
		}
		selectList = selectList.immutable();
	}
	
	private void initSelectStatements() {
		initSelectList();
		from = new From().tableName(tableIdentifier)
				.immutable();
		select = new Select().list(selectList)
				.from(from)
				.immutable();
		selectById = select.mutable().where(createWhereById())
				.immutable();
	}
	
	private void initUpdateById() {
		updateById = new Update()
		.tableName(tableIdentifier)
		.set(createUpdateByIdSetValues())
		.where(createWhereById(true))
		.immutable();
	}
	
	private void initVersionColumn(FieldColumnMapping fieldColumnMapping) {
		if (!fieldColumnMapping.isVersion()) {
			return;
		}
		
		if (versionColumnMapping != null) {
			throw new IllegalStateException("class (" + clazz + ") should only have one row versioning column");
		}
		
		versionColumnMapping = fieldColumnMapping;
	}
	
	private void setInsertColumnsAndValues(Insert insert) {
		ColumnList columns = new ColumnList();
		ColumnValues values = new ColumnValues();
		insert.columns(columns).values(values);
		
		for (FieldColumnMapping fieldColumnMapping : fieldColumnMappingList) {
			if (fieldColumnMapping.isAutoIncrementId()) {
				continue;
			}
			
			String name = fieldColumnMapping.getColumn();
			columns.column(name);
			values.param(name);
		}
	}
	
	/* END Private methods */
}