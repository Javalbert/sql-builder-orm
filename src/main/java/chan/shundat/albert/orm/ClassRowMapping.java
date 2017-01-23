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

import java.io.Serializable;
import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import chan.shundat.albert.sqlbuilder.ColumnList;
import chan.shundat.albert.sqlbuilder.ColumnValues;
import chan.shundat.albert.sqlbuilder.Delete;
import chan.shundat.albert.sqlbuilder.Expression;
import chan.shundat.albert.sqlbuilder.From;
import chan.shundat.albert.sqlbuilder.Insert;
import chan.shundat.albert.sqlbuilder.Predicate;
import chan.shundat.albert.sqlbuilder.Select;
import chan.shundat.albert.sqlbuilder.SelectList;
import chan.shundat.albert.sqlbuilder.SetValue;
import chan.shundat.albert.sqlbuilder.SetValues;
import chan.shundat.albert.sqlbuilder.Update;
import chan.shundat.albert.sqlbuilder.Where;
import chan.shundat.albert.sqlbuilder.vendor.ANSI;
import chan.shundat.albert.sqlbuilder.vendor.Vendor;
import chan.shundat.albert.utils.reflection.MemberAccess;
import chan.shundat.albert.utils.string.Strings;

@SuppressWarnings({ "rawtypes", "unchecked" })
public class ClassRowMapping {
	private static void appendColumnEqualsParam(Where where, FieldColumnMapping fieldColumnMapping, boolean appendAnd) {
		if (appendAnd) {
			where.and();
		}
		
		where.predicate(new Predicate()
			.column(fieldColumnMapping.getColumn()).eq().param(fieldColumnMapping.getColumn())
		);
	}
	
	private FieldColumnMapping autoIncrementIdMapping;
	private String catalog;
	private final Class clazz;
	private Delete deleteById;
	private final Map<String, FieldColumnMapping> fieldAliasMappings;
	private final List<FieldColumnMapping> fieldColumnMappingList;
	private final Map<String, FieldColumnMapping> fieldColumnMappings;
	private From from;
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
	public Class getClazz() { return clazz; }
	public Delete getDeleteById() { return deleteById; }
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
	
	public ClassRowMapping(Class clazz) {
		this(clazz, ANSI.INSTANCE);
	}
	
	public ClassRowMapping(Class clazz, Vendor vendor) {
		this.clazz = clazz;
		this.vendor = vendor;

		FieldColumnMapper fieldColumnMapper = new FieldColumnMapper(clazz);
		fieldColumnMapper.mapAll();
		
		this.fieldAliasMappings = fieldColumnMapper.getFieldAliasMappings();
		this.fieldColumnMappingList = fieldColumnMapper.getFieldColumnMappingList();
		this.fieldColumnMappings = fieldColumnMapper.getFieldColumnMappings();
		this.relatedMemberAccessMap = fieldColumnMapper.getRelatedMemberAccessMap();
		
		Table tableAnno = (Table)clazz.getAnnotation(Table.class);
		if (tableAnno != null) {
			catalog = tableAnno.catalog();
			schema = tableAnno.schema();
			table = tableAnno.name();
			tableIdentifier = vendor.createTableIdentifier(tableAnno);
		}
		
		initPrimaryKeyData();
		initMapKeys();
		
		initDeleteById();
		initInsert();
		initSelectStatements();
		initUpdateById();
	}

	public <K> K getMapKeyValue(Object object) {
		return getMapKeyValue(object, null);
	}
	
	public <K> K getMapKeyValue(Object object, String mapKeyName) {
		boolean pkMapKey = mapKeyName == null;
		
		if (pkMapKey) {
			return (K)getOrCreateId(object);
		}
		FieldColumnMapping mapKeyMapping = mapKeyMappings.get(mapKeyName);
		
		if (mapKeyMapping == null) {
			throw new IllegalStateException("mapKeyName (" + mapKeyName 
					+ ") does not exist for object class (" + object.getClass() + ")");
		}
		return (K)mapKeyMapping.get(object);
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
	
	public boolean isAutoIncrementId() {
		return autoIncrementIdMapping != null;
	}
	
	public boolean isCompositePrimaryKey() {
		return idClass != null;
	}
	
	public boolean isScalarPrimaryKey() {
		return primaryKeyMappings.size() == 1;
	}
	
	public void setAutoIncrementId(Object object, int id) {
		autoIncrementIdMapping.set(object, id);
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
		if (!hasVersionControl()) {
			return;
		}
		versionColumnMapping.set(object, 0);
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
				+ clazz + ") is missing @IdClass annotation");
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
		// DELETE FROM syntax not ANSI standard
//		.from(new From()
//			.tableName(tableIdentifier)
//		)
		.where(createWhereById(true))
		.immutable();
	}
	
	private void initIdClassData() {
		IdClass idClassAnno = (IdClass)clazz.getAnnotation(IdClass.class);
		
		if (idClassAnno == null) {
			return;
		}
		Class idClass = idClassAnno.value();
		this.idClass = idClass;
		
		FieldColumnMapper fieldColumnMapper = new FieldColumnMapper(idClass);
		fieldColumnMapper.mapAll();
		idClassMappings = fieldColumnMapper.getFieldColumnMappingList();
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
		
		initIdClassData();
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
			throw new IllegalStateException("class (" + clazz + ") should only have one @Version column");
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