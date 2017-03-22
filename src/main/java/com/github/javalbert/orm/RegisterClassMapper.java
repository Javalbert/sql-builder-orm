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
import java.util.Arrays;
import java.util.Optional;

import com.github.javalbert.orm.ClassRowRegistration.ClassMember;
import com.github.javalbert.orm.ClassRowRegistration.ColumnClassMember;
import com.github.javalbert.orm.ClassRowRegistration.IdClassColumn;
import com.github.javalbert.orm.ClassRowRegistration.RelatedEntityClassMember;
import com.github.javalbert.sqlbuilder.vendor.ANSI;
import com.github.javalbert.sqlbuilder.vendor.Vendor;
import com.github.javalbert.utils.reflection.FieldMemberAccess;
import com.github.javalbert.utils.reflection.PropertyMemberAccess;
import com.github.javalbert.utils.string.Strings;

public class RegisterClassMapper extends ClassRowMapper {
	private static boolean memberIsField(ClassMember member) {
		return memberIsType(member, ClassMember.MEMBER_TYPE_FIELD);
	}
	
	private static boolean memberIsProperty(ClassMember member) {
		return memberIsType(member, ClassMember.MEMBER_TYPE_PROPERTY);
	}
	
	private static boolean memberIsType(ClassMember member, int type) {
		return member != null
				&& member.getMemberType() == type;
	}
	
	private final ClassRowRegistration registration;
	
	public RegisterClassMapper(ClassRowRegistration registration) {
		this(registration, ANSI.INSTANCE);
	}
	
	public RegisterClassMapper(ClassRowRegistration registration, Vendor vendor) {
		super(registration.getRegisteringClass(), vendor);
		this.registration = registration;

		catalog = registration.getCatalog();
		schema = registration.getSchema();
		table = registration.getTable();
		tableIdentifier = vendor.createTableIdentifier(catalog, schema, table);
		
		mapIdClass();
	}

	public String getAlias(Field field) {
		ColumnClassMember columnMember = registration.getColumnMemberMap()
				.get(field.getName());
		return memberIsField(columnMember) ? Strings.safeTrim(columnMember.getAlias()) : null;
	}
	
	public String getAlias(PropertyDescriptor propertyDescriptor) {
		ColumnClassMember columnMember = registration.getColumnMemberMap()
				.get(propertyDescriptor.getName());
		return memberIsProperty(columnMember) ? Strings.safeTrim(columnMember.getAlias()) : null;
	}
	
	public String getColumnName(Field field) {
		ColumnClassMember columnMember = registration.getColumnMemberMap()
				.get(field.getName());
		return memberIsField(columnMember) ? Strings.safeTrim(columnMember.getColumn()) : null;
	}
	
	public String getColumnName(PropertyDescriptor propertyDescriptor) {
		ColumnClassMember columnMember = registration.getColumnMemberMap()
				.get(propertyDescriptor.getName());
		return memberIsProperty(columnMember) ? Strings.safeTrim(columnMember.getColumn()) : null;
	}
	
	public int getJdbcType(Field field) {
		ColumnClassMember columnMember = registration.getColumnMemberMap()
				.get(field.getName());
		return getJdbcType(field.getType(), columnMember.isTimestamp());
	}
	
	public int getJdbcType(PropertyDescriptor propertyDescriptor) {
		ColumnClassMember columnMember = registration.getColumnMemberMap()
				.get(propertyDescriptor.getName());
		return getJdbcType(propertyDescriptor.getPropertyType(), columnMember.isTimestamp());
	}
	
	public boolean isAutoIncrement(Field field) {
		return Optional.ofNullable(registration.getColumnMemberMap().get(field.getName()))
				.filter(RegisterClassMapper::memberIsField)
				.filter(ColumnClassMember::isAutoIncrement)
				.isPresent();
	}
	
	public boolean isAutoIncrement(PropertyDescriptor propertyDescriptor) {
		return Optional.ofNullable(registration.getColumnMemberMap().get(propertyDescriptor.getName()))
				.filter(RegisterClassMapper::memberIsProperty)
				.filter(ColumnClassMember::isAutoIncrement)
				.isPresent();
	}
	
	public boolean isPrimaryKeyColumn(Field field) {
		return Optional.ofNullable(registration.getColumnMemberMap().get(field.getName()))
				.filter(RegisterClassMapper::memberIsField)
				.filter(ColumnClassMember::isPrimaryKey)
				.isPresent();
	}
	
	public boolean isPrimaryKeyColumn(PropertyDescriptor propertyDescriptor) {
		return Optional.ofNullable(registration.getColumnMemberMap().get(propertyDescriptor.getName()))
				.filter(RegisterClassMapper::memberIsProperty)
				.filter(ColumnClassMember::isPrimaryKey)
				.isPresent();
	}
	
	public boolean isVersionColumn(Field field) {
		return Optional.ofNullable(registration.getColumnMemberMap().get(field.getName()))
				.filter(RegisterClassMapper::memberIsField)
				.filter(ColumnClassMember::isVersion)
				.isPresent();
	}
	
	public boolean isVersionColumn(PropertyDescriptor propertyDescriptor) {
		return Optional.ofNullable(registration.getColumnMemberMap().get(propertyDescriptor.getName()))
				.filter(RegisterClassMapper::memberIsProperty)
				.filter(ColumnClassMember::isVersion)
				.isPresent();
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
					+ ") cannot be a primary key and a row versioning field at the same time");
		}
		
		return new FieldAccessMapping(
				clazz,
				columnName,
				alias,
				field,
				getJdbcType(field),
				primaryKey,
				primaryKey && isAutoIncrement(field),
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
					+ ") cannot be a primary key and a row versioning field at the same time");
		}

		return new PropertyAccessMapping(
				clazz,
				columnName,
				alias,
				propertyDescriptor,
				getJdbcType(propertyDescriptor),
				primaryKey,
				primaryKey && isAutoIncrement(propertyDescriptor),
				version);
	}

	private void addRelatedMemberAccess(Field field) {
		RelatedEntityClassMember relatedMember = registration.getRelatedEntityMemberMap()
				.get(field.getName());
		if (memberIsField(relatedMember)) {
			FieldMemberAccess fieldMember = new FieldMemberAccess(clazz, field);
			relatedMemberAccessMap.put(relatedMember.getFieldName(), fieldMember);
		}
	}

	private void addRelatedPropertyMember(PropertyDescriptor propertyDescriptor) {
		RelatedEntityClassMember relatedMember = registration.getRelatedEntityMemberMap()
				.get(propertyDescriptor.getName());
		if (memberIsProperty(relatedMember)) {
			PropertyMemberAccess propertyMember = new PropertyMemberAccess(clazz, propertyDescriptor);
			relatedMemberAccessMap.put(relatedMember.getFieldName(), propertyMember);
		}
	}
	
	private FieldColumnMapping mapFieldToIdColumn(Field field) {
		final String columnName = Optional.ofNullable(
				registration.getIdClassColumnMap()
				.get(field.getName()))
				.map(IdClassColumn::getColumn)
				.orElse(null);
		
		if (Strings.isNullOrEmpty(columnName)) {
			return null;
		}
		
		return new FieldAccessMapping(
				clazz,
				columnName,
				null,
				field,
				getJdbcType(field),
				true,
				false,
				false);
	}
	
	private void mapIdClass() {
		idClass = registration.getIdClass();
		
		if (idClass == null) {
			return;
		}
		
		Arrays.stream(idClass.getDeclaredFields())
				.filter(field -> memberIsField(registration.getIdClassColumnMap()
						.get(field.getName())))
				.map(this::mapFieldToIdColumn)
				.forEach(this.idClassMappings::add);
		
		BeanInfo idClassInfo = null;
		try {
			idClassInfo = Introspector.getBeanInfo(idClass);
		} catch (IntrospectionException e) {
			throw new RuntimeException(e);
		}
		Arrays.stream(idClassInfo.getPropertyDescriptors())
				.filter(property -> memberIsProperty(registration.getIdClassColumnMap()
						.get(property.getName())))
				.map(this::mapPropertyToIdColumn)
				.forEach(this.idClassMappings::add);
	}
	
	private FieldColumnMapping mapPropertyToIdColumn(PropertyDescriptor propertyDescriptor) {
		final String columnName = Optional.ofNullable(
				registration.getIdClassColumnMap()
				.get(propertyDescriptor.getName()))
				.map(IdClassColumn::getColumn)
				.orElse(null);
		
		if (Strings.isNullOrEmpty(columnName)) {
			return null;
		}
		
		return new PropertyAccessMapping(
				clazz,
				columnName,
				null,
				propertyDescriptor,
				getJdbcType(propertyDescriptor),
				true,
				false,
				false);
	}
}
