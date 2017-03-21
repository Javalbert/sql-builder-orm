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

import java.beans.PropertyDescriptor;
import java.lang.reflect.Field;
import java.util.Optional;

import com.github.javalbert.sqlbuilder.vendor.ANSI;
import com.github.javalbert.sqlbuilder.vendor.Vendor;
import com.github.javalbert.utils.reflection.FieldMemberAccess;
import com.github.javalbert.utils.reflection.PropertyMemberAccess;
import com.github.javalbert.utils.string.Strings;

public class AnnotatedClassMapper extends ClassRowMapper {
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
	
	public AnnotatedClassMapper(Class<?> clazz) {
		this(clazz, ANSI.INSTANCE);
	}
	
	public AnnotatedClassMapper(Class<?> clazz, Vendor vendor) {
		super(clazz, vendor);
		
		Table tableAnno = (Table)clazz.getAnnotation(Table.class);
		if (tableAnno != null) {
			catalog = tableAnno.catalog();
			schema = tableAnno.schema();
			table = tableAnno.name();
			tableIdentifier = vendor.createTableIdentifier(catalog, schema, table);
		}
		
		/* ID class */
		
		IdClass idClassAnno = (IdClass)clazz.getAnnotation(IdClass.class);
		if (idClassAnno != null) {
			Class<?> idClass = idClassAnno.value();
			this.idClass = idClass;
			
			ClassRowMapper fieldColumnMapper = new AnnotatedClassMapper(idClass);
			fieldColumnMapper.map();
			idClassMappings.addAll(fieldColumnMapper.getFieldColumnMappingList());
		}
	}
	
	@Override
	public FieldColumnMapping mapFieldToColumn(Field field) {
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
				clazz,
				columnName,
				alias,
				field,
				jdbcType,
				primaryKey,
				primaryKey && field.isAnnotationPresent(GeneratedValue.class),
				version);
	}
	
	@Override
	public FieldColumnMapping mapPropertyToColumn(PropertyDescriptor propertyDescriptor) {
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
		
		return new PropertyAccessMapping(
				clazz,
				columnName,
				alias,
				propertyDescriptor,
				jdbcType,
				primaryKey,
				primaryKey && generatedValue != null,
				version);
	}
	
	private void addRelatedMemberAccess(Field field) {
		Related related = field.getAnnotation(Related.class);
		if (related != null) {
			FieldMemberAccess fieldMember = new FieldMemberAccess(clazz, field);
			relatedMemberAccessMap.put(related.value(), fieldMember);
		}
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
		
		PropertyMemberAccess propertyMember = new PropertyMemberAccess(clazz, propertyDescriptor);
		relatedMemberAccessMap.put(related.value(), propertyMember);
	}
}
