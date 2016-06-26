package chan.shundat.albert.orm;

import java.io.Serializable;
import java.lang.reflect.Field;
import java.lang.reflect.Method;
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
import chan.shundat.albert.sqlbuilder.vendor.Vendor;
import chan.shundat.albert.utils.reflection.FieldMemberAccess;
import chan.shundat.albert.utils.reflection.MemberAccess;
import chan.shundat.albert.utils.reflection.PropertyMemberAccess;
import chan.shundat.albert.utils.string.Strings;

@SuppressWarnings({ "rawtypes", "unchecked" })
public class ClassRowMapping {
	private static void addFieldAccessMappings(Class clazz, List<FieldColumnMapping> fieldColumnMappings) {
		addFieldAccessMappings(clazz, fieldColumnMappings, null, null, null);
	}
	
	private static void addFieldAccessMappings(Class clazz, 
			List<FieldColumnMapping> fieldColumnMappingList, 
			Map<String, FieldColumnMapping> fieldColumnMappings, 
			Map<String, FieldColumnMapping> fieldAliasMappings, 
			Map<String, MemberAccess> relatedMemberAccessMap) {
		Field[] fields = clazz.getDeclaredFields();
		
		for (Field field : fields) {
			field.setAccessible(true);
			
			addRelatedFieldMember(relatedMemberAccessMap, field);
			
			Column column = field.getAnnotation(Column.class);
			Alias aliasAnnotation = field.getAnnotation(Alias.class);
			
			String columnName = column != null ? Strings.safeTrim(column.value()) : null;
			String alias = aliasAnnotation != null ? aliasAnnotation.value() : null;
			
			if (column != null && Strings.isNullOrEmpty(columnName)) {
				columnName = field.getName();
			}
			if (Strings.isNullOrEmpty(columnName) && Strings.isNullOrEmpty(alias)) {
				continue;
			}
			
			boolean primaryKey = field.isAnnotationPresent(Id.class);
			boolean version = field.isAnnotationPresent(Version.class);
			if (primaryKey && primaryKey == version) {
				throw new IllegalArgumentException("field (" + field.getName() 
						+ ") cannot have @Id and @Version annotations at the same time");
			}
			
			int jdbcType = 0;
			
			boolean timestamp = field.isAnnotationPresent(IsTimestamp.class);
			if (timestamp) {
				jdbcType = FieldColumnMapping.JDBC_TYPE_TIMESTAMP;
			} else {
				jdbcType = FieldColumnMapping.getJdbcType(field.getType());
			}
			
			FieldColumnMapping mapping = new FieldAccessMapping(columnName, 
					alias, 
					field, 
					jdbcType, 
					primaryKey, 
					field.getAnnotation(GeneratedValue.class), 
					version);
			
			if (!Strings.isNullOrEmpty(columnName)) {
				fieldColumnMappingList.add(mapping);
				
				if (fieldColumnMappings != null) {
					fieldColumnMappings.put(mapping.getColumn(), mapping);
				}
			}
			if (!Strings.isNullOrEmpty(alias) && fieldAliasMappings != null) {
				fieldAliasMappings.put(alias, mapping);
			}
		}
	}

	private static void addPropertyAccessMappings(Class clazz, List<FieldColumnMapping> fieldColumnMappings) {
		addPropertyAccessMappings(clazz, fieldColumnMappings, null, null, null);
	}
	
	private static void addPropertyAccessMappings(Class clazz, 
			List<FieldColumnMapping> fieldColumnMappingList, 
			Map<String, FieldColumnMapping> fieldColumnMappings, 
			Map<String, FieldColumnMapping> fieldAliasMappings, 
			Map<String, MemberAccess> relatedMemberAccessMap) {
		Method[] methods = clazz.getDeclaredMethods();
		
		for (Method method : methods) {
			method.setAccessible(true);

			Method getter = getPropertyMethod(method, true, clazz);
			Method setter = getPropertyMethod(method, false, clazz);

			addRelatedPropertyMember(relatedMemberAccessMap, method, getter, setter);
			
			Column column = method.getAnnotation(Column.class);
			Alias aliasAnnotation = method.getAnnotation(Alias.class);

			final String columnName = column != null ? Strings.safeTrim(column.value()) : null;
			final String alias = aliasAnnotation != null ? aliasAnnotation.value() : null;
			
			if (column != null && Strings.isNullOrEmpty(columnName)) {
				throw new IllegalArgumentException("property (" + method.getName() 
						+ ")'s @Column annotation's name is null or empty");
			}
			if (Strings.isNullOrEmpty(columnName) && Strings.isNullOrEmpty(alias)) {
				continue;
			}
			
			if (fieldColumnMappings.containsKey(columnName)) {
				throw new IllegalArgumentException("clazz (" + clazz 
						+ ") defines column name (" + columnName + ") twice");
			}
			
			boolean primaryKey = method.isAnnotationPresent(Id.class);
			boolean version = method.isAnnotationPresent(Version.class);
			if (primaryKey && primaryKey == version) {
				throw new IllegalArgumentException("method (" + method.getName() 
						+ ") cannot have @Id and @Version annotations at the same time");
			}
			
			int jdbcType = 0;
			
			boolean timestamp = method.isAnnotationPresent(IsTimestamp.class);
			if (timestamp) {
				jdbcType = FieldColumnMapping.JDBC_TYPE_TIMESTAMP;
			} else {
				jdbcType = FieldColumnMapping.getJdbcType(getter.getReturnType());
			}

			FieldColumnMapping mapping = new PropertyAccessMapping(columnName, 
					alias, 
					getter, 
					setter, 
					jdbcType, 
					primaryKey, 
					method.getAnnotation(GeneratedValue.class), 
					version);
			
			if (!Strings.isNullOrEmpty(columnName)) {
				fieldColumnMappingList.add(mapping);
				
				if (fieldColumnMappings != null) {
					fieldColumnMappings.put(mapping.getColumn(), mapping);
				}
			}
			if (!Strings.isNullOrEmpty(alias)) {
				fieldAliasMappings.put(alias, mapping);
			}
		}
	}
	
	private static void addRelatedFieldMember(Map<String, MemberAccess> relatedMemberAccessMap, Field field) {
		if (relatedMemberAccessMap == null) {
			return;
		}
		
		Related related = field.getAnnotation(Related.class);
		if (related == null) {
			return;
		}
		
		FieldMemberAccess fieldMember = new FieldMemberAccess(field);
		relatedMemberAccessMap.put(related.value(), fieldMember);
	}
	
	private static void addRelatedPropertyMember(Map<String, MemberAccess> relatedMemberAccessMap, 
			Method method, 
			Method getter, 
			Method setter) {
		if (relatedMemberAccessMap == null) {
			return;
		}
		
		Related related = method.getAnnotation(Related.class);
		if (related == null) {
			return;
		}
		
		PropertyMemberAccess propertyMember = new PropertyMemberAccess(getter, setter);
		relatedMemberAccessMap.put(related.value(), propertyMember);
	}
	
	private static void appendColumnEqualsParam(Where where, FieldColumnMapping fieldColumnMapping, boolean appendAnd) {
		if (appendAnd) {
			where.and();
		}
		
		where.predicate(new Predicate()
			.column(fieldColumnMapping.getColumn()).eq().param(fieldColumnMapping.getColumn())
		);
	}
	
	private static Method getPropertyMethod(Method method, boolean findGetter, Class clazz) {
		String methodName = method.getName();
		String methodPrefixToFind = findGetter ? "get" : "set";
		
		boolean found = methodName.startsWith(methodPrefixToFind);
		if (found) {
			return method;
		}
		
		try {
			String prefixReplacement = findGetter ? "set" : "get";
			methodName = methodPrefixToFind + methodName.replaceFirst(prefixReplacement, "");
			
			method = findGetter ? clazz.getDeclaredMethod(methodName) 
					: clazz.getDeclaredMethod(methodName, method.getReturnType());
			method.setAccessible(true);
			return method;
		} catch (NoSuchMethodException | SecurityException e) {
			return null;
		}
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
	
	public ClassRowMapping(Class clazz, Vendor vendor) {
		this.clazz = clazz;
		this.vendor = vendor;

		List<FieldColumnMapping> fieldColumnMappingList = new ArrayList<>();
		Map<String, FieldColumnMapping> fieldColumnMappings = new HashMap<>();
		Map<String, FieldColumnMapping> fieldAliasMappings = new HashMap<>();
		Map<String, MemberAccess> relatedMemberAccessMap = new HashMap<>();
		
		addFieldAccessMappings(clazz, 
				fieldColumnMappingList, 
				fieldColumnMappings, 
				fieldAliasMappings, 
				relatedMemberAccessMap);
		addPropertyAccessMappings(clazz, 
				fieldColumnMappingList, 
				fieldColumnMappings, 
				fieldAliasMappings, 
				relatedMemberAccessMap);
		
		this.fieldAliasMappings = Collections.unmodifiableMap(fieldAliasMappings);
		this.fieldColumnMappingList = Collections.unmodifiableList(fieldColumnMappingList);
		this.fieldColumnMappings = Collections.unmodifiableMap(fieldColumnMappings);
		this.relatedMemberAccessMap = Collections.unmodifiableMap(relatedMemberAccessMap);
		
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

	public <K> K getMapKeyValue(String mapKeyName, Object object) {
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
		
		idClassMappings = new ArrayList<>();
		addFieldAccessMappings(idClass, idClassMappings);
		addPropertyAccessMappings(idClass, idClassMappings);
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