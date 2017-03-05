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

import java.beans.BeanInfo;
import java.beans.IntrospectionException;
import java.beans.Introspector;
import java.beans.PropertyDescriptor;
import java.lang.reflect.Field;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Optional;

import com.github.javalbert.utils.reflection.FieldMemberAccess;
import com.github.javalbert.utils.reflection.MemberAccess;
import com.github.javalbert.utils.reflection.PropertyMemberAccess;
import com.github.javalbert.utils.string.Strings;

public class FieldColumnMapper<T> {
	public static String getAlias(Field field) {
		Alias aliasAnnotation = field.getAnnotation(Alias.class);
		return aliasAnnotation != null ? Strings.safeTrim(aliasAnnotation.value()) : null;
	}
	
	public static String getAlias(PropertyDescriptor propertyDescriptor) {
		Alias aliasAnnotation = Optional.ofNullable(propertyDescriptor.getReadMethod())
				.map(m -> m.getAnnotation(Alias.class))
				.orElse(null);
		if (aliasAnnotation == null) {
			aliasAnnotation = Optional.ofNullable(propertyDescriptor.getWriteMethod())
					.map(m -> m.getAnnotation(Alias.class))
					.orElse(null);
		}
		return aliasAnnotation != null ? Strings.safeTrim(aliasAnnotation.value()) : null;
	}
	
	public static String getColumnName(Field field) {
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
	
	public static String getColumnName(PropertyDescriptor propertyDescriptor) {
		Column column = Optional.ofNullable(propertyDescriptor.getReadMethod())
				.map(m -> m.getAnnotation(Column.class))
				.orElse(null);
		if (column == null) {
			column = Optional.ofNullable(propertyDescriptor.getWriteMethod())
					.map(m -> m.getAnnotation(Column.class))
					.orElse(null);
		}
		if (column == null) {
			return null;
		}
		
		String columnName = Strings.safeTrim(column.value());
		if (Strings.isNullOrEmpty(columnName)) {
			columnName = propertyDescriptor.getName();
		}
		return columnName;
	}
	
	public static int getJdbcType(Field field) {
		boolean timestamp = field.isAnnotationPresent(IsTimestamp.class);
		return timestamp ? FieldColumnMapping.JDBC_TYPE_TIMESTAMP : FieldColumnMapping.getJdbcType(field.getType());
	}
	
	public static int getJdbcType(PropertyDescriptor propertyDescriptor) {
		boolean timestamp = Optional.ofNullable(propertyDescriptor.getReadMethod())
				.map(m -> m.isAnnotationPresent(IsTimestamp.class))
				.orElse(false) || Optional.ofNullable(propertyDescriptor.getWriteMethod())
				.map(m -> m.isAnnotationPresent(IsTimestamp.class))
				.orElse(false);
		return timestamp ? FieldColumnMapping.JDBC_TYPE_TIMESTAMP : FieldColumnMapping.getJdbcType(propertyDescriptor.getPropertyType());
	}
	
	public static boolean isPrimaryKeyColumn(Field field) {
		return field.isAnnotationPresent(Id.class);
	}
	
	public static boolean isPrimaryKeyColumn(PropertyDescriptor propertyDescriptor) {
		return Optional.ofNullable(propertyDescriptor.getReadMethod())
				.map(m -> m.isAnnotationPresent(Id.class))
				.orElse(false) || Optional.ofNullable(propertyDescriptor.getWriteMethod())
				.map(m -> m.isAnnotationPresent(Id.class))
				.orElse(false);
	}
	
	public static boolean isVersionColumn(Field field) {
		return field.isAnnotationPresent(Version.class);
	}
	
	public static boolean isVersionColumn(PropertyDescriptor propertyDescriptor) {
		return Optional.ofNullable(propertyDescriptor.getReadMethod())
				.map(m -> m.isAnnotationPresent(Version.class))
				.orElse(false) || Optional.ofNullable(propertyDescriptor.getWriteMethod())
				.map(m -> m.isAnnotationPresent(Version.class))
				.orElse(false);
	}
	
	private final Class<T> clazz;
	private final Map<String, FieldColumnMapping<T>> fieldAliasMappings = new HashMap<>();
	private final List<FieldColumnMapping<T>> fieldColumnMappingList = new ArrayList<>();
	private final Map<String, FieldColumnMapping<T>> fieldColumnMappings = new HashMap<>();
	private final Map<String, Field> fieldMap = new HashMap<>();
	private final List<Field> fields;
	private final Map<String, PropertyDescriptor> propertyDescriptorMap = new HashMap<>();
	private final List<PropertyDescriptor> propertyDescriptors;
	private final Map<String, MemberAccess<T>> relatedMemberAccessMap = new HashMap<>();
	
	public Map<String, FieldColumnMapping<T>> getFieldAliasMappings() {
		return Collections.unmodifiableMap(fieldAliasMappings);
	}

	public List<FieldColumnMapping<T>> getFieldColumnMappingList() {
		return Collections.unmodifiableList(fieldColumnMappingList);
	}

	public Map<String, FieldColumnMapping<T>> getFieldColumnMappings() {
		return Collections.unmodifiableMap(fieldColumnMappings);
	}
	
	public Map<String, MemberAccess<T>> getRelatedMemberAccessMap() {
		return Collections.unmodifiableMap(relatedMemberAccessMap);
	}
	
	public FieldColumnMapper(Class<T> clazz) {
		this.clazz = clazz;
		
		fields = Collections.unmodifiableList(Arrays.asList(clazz.getDeclaredFields()));
		fields.forEach(field -> fieldMap.put(field.getName(), field));
		
		try {
			BeanInfo beanInfo = Introspector.getBeanInfo(clazz);
			propertyDescriptors = Collections.unmodifiableList(Arrays.asList(beanInfo.getPropertyDescriptors()));
			propertyDescriptors.forEach(prop -> propertyDescriptorMap.put(prop.getName(), prop));
		} catch (IntrospectionException e) {
			throw new RuntimeException(e);
		}
	}
	
	public Field getField(String name) {
		return fieldMap.get(name);
	}

	public FieldColumnMapping<T> getMapping(String column) {
		return fieldColumnMappings.get(column);
	}
	
	public PropertyDescriptor getProperty(String name) {
		return propertyDescriptorMap.get(name);
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
	
	public FieldColumnMapping<T> mapFieldToColumn(Field field) {
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
		
		return new FieldAccessMapping<>(
				clazz,
				columnName,
				alias,
				field,
				jdbcType,
				primaryKey,
				field.getAnnotation(GeneratedValue.class),
				version);
	}
	
	public FieldColumnMapping<T> mapPropertyToColumn(PropertyDescriptor propertyDescriptor) {
		addRelatedPropertyMember(propertyDescriptor);
		
		final String columnName = getColumnName(propertyDescriptor);
		final String alias = getAlias(propertyDescriptor);
		
		if (Strings.isNullOrEmpty(columnName) && Strings.isNullOrEmpty(alias)) {
			return null;
		}
		
		boolean primaryKey = isPrimaryKeyColumn(propertyDescriptor);
		boolean version = isVersionColumn(propertyDescriptor);

		if (primaryKey && primaryKey == version) {
			throw new IllegalArgumentException("property (" + propertyDescriptor.getName()
					+ ") cannot have @Id and @Version annotations at the same time");
		}

		final int jdbcType = getJdbcType(propertyDescriptor);
		
		GeneratedValue generatedValue = Optional.ofNullable(propertyDescriptor.getReadMethod())
				.map(m -> m.getAnnotation(GeneratedValue.class))
				.orElse(null);
		if (generatedValue == null) {
			generatedValue = Optional.ofNullable(propertyDescriptor.getWriteMethod())
					.map(m -> m.getAnnotation(GeneratedValue.class))
					.orElse(null);
		}
		
		return new PropertyAccessMapping<T>(
				clazz,
				columnName,
				alias,
				propertyDescriptor,
				jdbcType,
				primaryKey,
				generatedValue,
				version);
	}
	
	public void mapPropertiesToColumns() {
		propertyDescriptors.stream()
			.map(this::mapPropertyToColumn)
			.forEach(this::addMapping);
	}
	
	private void addMapping(FieldColumnMapping<T> mapping) {
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
		
		FieldMemberAccess<T> fieldMember = new FieldMemberAccess<>(clazz, field);
		relatedMemberAccessMap.put(related.value(), fieldMember);
	}
	
	private void addRelatedPropertyMember(PropertyDescriptor propertyDescriptor) {
		Related related = Optional.ofNullable(propertyDescriptor.getReadMethod())
				.map(m -> m.getAnnotation(Related.class))
				.orElse(null);
		
		if (related == null) {
			related = Optional.ofNullable(propertyDescriptor.getWriteMethod())
					.map(m -> m.getAnnotation(Related.class))
					.orElse(null);
		}
		if (related == null) {
			return;
		}
		
		PropertyMemberAccess<T> propertyMember = new PropertyMemberAccess<>(clazz, propertyDescriptor);
		relatedMemberAccessMap.put(related.value(), propertyMember);
	}
}