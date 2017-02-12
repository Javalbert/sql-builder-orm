/*******************************************************************************
 * Copyright 2017 Albert Shun-Dat Chan
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

import java.lang.reflect.Field;
import java.lang.reflect.Method;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import com.github.javalbert.utils.reflection.FieldMemberAccess;
import com.github.javalbert.utils.reflection.MemberAccess;
import com.github.javalbert.utils.reflection.PropertyMemberAccess;
import com.github.javalbert.utils.string.Strings;

@SuppressWarnings({ "rawtypes", "unchecked" })
public class FieldColumnMapper {
	private final Map<String, FieldColumnMapping> fieldAliasMappings = new HashMap<>();
	private final List<FieldColumnMapping> fieldColumnMappingList = new ArrayList<>();
	private final Map<String, FieldColumnMapping> fieldColumnMappings = new HashMap<>();
	private final Map<String, Field> fieldMap = new HashMap<>();
	private final List<Field> fields;
	private final Map<String, Method> methodMap = new HashMap<>();
	private final List<Method> methods;
	private final Map<String, MemberAccess> relatedMemberAccessMap = new HashMap<>();
	
	public Map<String, FieldColumnMapping> getFieldAliasMappings() {
		return Collections.unmodifiableMap(fieldAliasMappings);
	}

	public List<FieldColumnMapping> getFieldColumnMappingList() {
		return Collections.unmodifiableList(fieldColumnMappingList);
	}

	public Map<String, FieldColumnMapping> getFieldColumnMappings() {
		return Collections.unmodifiableMap(fieldColumnMappings);
	}
	
	public Map<String, MemberAccess> getRelatedMemberAccessMap() {
		return Collections.unmodifiableMap(relatedMemberAccessMap);
	}
	
	public FieldColumnMapper(Class clazz) {
		fields = Collections.unmodifiableList(Arrays.asList(clazz.getDeclaredFields()));
		fields.forEach(field -> fieldMap.put(field.getName(), field));
		methods = Collections.unmodifiableList(Arrays.asList(clazz.getDeclaredMethods()));
		methods.forEach(method -> methodMap.put(method.getName(), method));
	}

	public String getAlias(Field field) {
		Alias aliasAnnotation = field.getAnnotation(Alias.class);
		return aliasAnnotation != null ? Strings.safeTrim(aliasAnnotation.value()) : null;
	}
	
	public String getAlias(Method method) {
		Alias aliasAnnotation = method.getAnnotation(Alias.class);
		return aliasAnnotation != null ? Strings.safeTrim(aliasAnnotation.value()) : null;
	}
	
	public String getColumnName(Field field) {
		Column column = field.getAnnotation(Column.class);
		if (column == null) {
			return null;
		}

		String columnName = Strings.safeTrim(column.value());
		if (Strings.isNullOrEmpty(columnName)) {
			columnName = field.getName();
		}
		return columnName;
	}
	
	public String getColumnName(Method method) {
		Column column = method.getAnnotation(Column.class);
		return column != null ? Strings.safeTrim(column.value()) : null;
	}
	
	public Field getField(String name) {
		return fieldMap.get(name);
	}
	
	public int getJdbcType(Field field) {
		boolean timestamp = field.isAnnotationPresent(IsTimestamp.class);
		return timestamp ? FieldColumnMapping.JDBC_TYPE_TIMESTAMP : FieldColumnMapping.getJdbcType(field.getType());
	}
	
	public int getJdbcType(Method getterMethod) {
		boolean timestamp = getterMethod.isAnnotationPresent(IsTimestamp.class);
		return timestamp ? FieldColumnMapping.JDBC_TYPE_TIMESTAMP : FieldColumnMapping.getJdbcType(getterMethod.getReturnType());
	}

	public FieldColumnMapping getMapping(String column) {
		return fieldColumnMappings.get(column);
	}
	
	public Method getMethod(String name) {
		return methodMap.get(name);
	}
	
	public boolean isPrimaryKeyColumn(Field field) {
		return field.isAnnotationPresent(Id.class);
	}
	
	public boolean isPrimaryKeyColumn(Method method) {
		return method.isAnnotationPresent(Id.class);
	}
	
	public boolean isVersionColumn(Field field) {
		return field.isAnnotationPresent(Version.class);
	}
	
	public boolean isVersionColumn(Method method) {
		return method.isAnnotationPresent(Version.class);
	}
	
	public void mapAll() {
		mapFieldsToColumns();
		mapPropertiesToColumns();
	}
	
	public void mapFieldsToColumns() {
		fields.stream()
			.map(this::mapFieldToColumn)
			.forEach(this::addMapping);
	}
	
	public FieldColumnMapping mapFieldToColumn(Field field) {
		field.setAccessible(true);

		addRelatedMemberAccess(field);
		
		final String columnName = getColumnName(field);
		final String alias = getAlias(field);
		
		if (Strings.isNullOrEmpty(columnName) && Strings.isNullOrEmpty(alias)) {
			return null;
		}
		
		final boolean primaryKey = isPrimaryKeyColumn(field);
		final boolean version = isVersionColumn(field);
		if (primaryKey && primaryKey == version) {
			throw new IllegalArgumentException("field (" + field.getName() 
					+ ") cannot have @Id and @Version annotations at the same time");
		}
		
		final int jdbcType = getJdbcType(field);
		
		return new FieldAccessMapping(
				columnName, 
				alias, 
				field, 
				jdbcType, 
				primaryKey, 
				field.getAnnotation(GeneratedValue.class), 
				version);
	}
	
	public FieldColumnMapping mapPropertyToColumn(Method method) {
		method.setAccessible(true);
		
		final Class clazz = method.getDeclaringClass();
		
		final Method getter = getPropertyMethod(method, true, clazz);
		final Method setter = getPropertyMethod(method, false, clazz);
		
		addRelatedPropertyMember(method, getter, setter);
		
		final String columnName = getColumnName(method);
		final String alias = getAlias(method);

		if (method.isAnnotationPresent(Column.class) && Strings.isNullOrEmpty(columnName)) {
			throw new IllegalArgumentException("property (" + method.getName() 
					+ ")'s @Column annotation's name is null or empty");
		}
		if (Strings.isNullOrEmpty(columnName) && Strings.isNullOrEmpty(alias)) {
			return null;
		}
		
		boolean primaryKey = isPrimaryKeyColumn(method);
		boolean version = isVersionColumn(method);

		if (primaryKey && primaryKey == version) {
			throw new IllegalArgumentException("property (" + method.getName()
					+ ") cannot have @Id and @Version annotations at the same time");
		}

		final int jdbcType = getJdbcType(getter);
		
		return new PropertyAccessMapping(
				columnName, 
				alias, 
				getter, 
				setter, 
				jdbcType, 
				primaryKey, 
				method.getAnnotation(GeneratedValue.class), 
				version);
	}
	
	public void mapPropertiesToColumns() {
		methods.stream()
			.map(this::mapPropertyToColumn)
			.forEach(this::addMapping);
	}
	
	void addMapping(FieldColumnMapping mapping) {
		if (mapping == null) {
			return;
		}
		
		if (!Strings.isNullOrEmpty(mapping.getColumn())) {
			if (fieldColumnMappings.containsKey(mapping.getColumn())) {
				throw new IllegalArgumentException("Cannot add mapping because column name (" 
						+ mapping.getColumn() + ") is already defined");
			}
			
			fieldColumnMappingList.add(mapping);
			fieldColumnMappings.put(mapping.getColumn(), mapping);
		}
		if (!Strings.isNullOrEmpty(mapping.getAlias())) {
			fieldAliasMappings.put(mapping.getAlias(), mapping);
		}
	}
	
	private void addRelatedMemberAccess(Field field) {
		Related related = field.getAnnotation(Related.class);
		if (related == null) {
			return;
		}
		
		FieldMemberAccess fieldMember = new FieldMemberAccess(field);
		relatedMemberAccessMap.put(related.value(), fieldMember);
	}
	
	private void addRelatedPropertyMember(
			Method method, 
			Method getter, 
			Method setter) {
		Related related = method.getAnnotation(Related.class);
		if (related == null) {
			return;
		}
		
		PropertyMemberAccess propertyMember = new PropertyMemberAccess(getter, setter);
		relatedMemberAccessMap.put(related.value(), propertyMember);
	}
	
	private Method getPropertyMethod(Method method, boolean findGetter, Class clazz) {
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
}