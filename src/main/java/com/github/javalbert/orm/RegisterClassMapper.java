package com.github.javalbert.orm;

import java.beans.PropertyDescriptor;
import java.lang.reflect.Field;
import java.util.Optional;

import com.github.javalbert.orm.ClassRowRegistration.ClassMember;
import com.github.javalbert.orm.ClassRowRegistration.ColumnClassMember;
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
				&& member.getMemberType() == ClassMember.MEMBER_TYPE_FIELD;
	}
	
	private final ClassRowRegistration registration;
	
	public RegisterClassMapper(ClassRowRegistration registration) {
		this(registration, ANSI.INSTANCE);
	}
	
	public RegisterClassMapper(ClassRowRegistration registration, Vendor vendor) {
		super(registration.getRegisteringClass(), vendor);
		this.registration = registration;
	}

	@Override
	public void map() {
		mapFieldsToColumns();
		mapPropertiesToColumns();
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
		return columnMember.isTimestamp()
				? FieldColumnMapping.JDBC_TYPE_TIMESTAMP : FieldColumnMapping.getJdbcType(field.getType());
	}
	
	public int getJdbcType(PropertyDescriptor propertyDescriptor) {
		ColumnClassMember columnMember = registration.getColumnMemberMap()
				.get(propertyDescriptor.getName());
		return columnMember.isTimestamp()
				? FieldColumnMapping.JDBC_TYPE_TIMESTAMP : FieldColumnMapping.getJdbcType(propertyDescriptor.getPropertyType());
	}
	
	public boolean isAutoIncrement(Field field) {
		return Optional.ofNullable(registration.getColumnMemberMap().get(field.getName()))
				.filter(RegisterClassMapper::memberIsField)
				.filter(ColumnClassMember::isAutoIncrement)
				.isPresent();
	}
	
	public boolean isAutoIncrement(PropertyDescriptor propertyDescriptor) {
		return Optional.ofNullable(registration.getColumnMemberMap().get(propertyDescriptor.getName()))
				.filter(RegisterClassMapper::memberIsField)
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
					+ ") cannot be a primary key and a row versioning field at the same time");
		}
		
		final int jdbcType = getJdbcType(field);
		
		return new FieldAccessMapping(
				clazz,
				columnName,
				alias,
				field,
				jdbcType,
				primaryKey,
				primaryKey && isAutoIncrement(field),
				version);
	}

	public void mapPropertiesToColumns() {
		propertyDescriptors.stream()
			.map(this::mapPropertyToColumn)
			.forEach(this::addMapping);
	}
	
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

		final int jdbcType = getJdbcType(propertyDescriptor);
		
//		GeneratedValue generatedValue = Optional.ofNullable(propertyDescriptor.getReadMethod())
//				.map(m -> m.getAnnotation(GeneratedValue.class))
//				.orElse(null);
//		if (generatedValue == null) {
//			generatedValue = Optional.ofNullable(propertyDescriptor.getWriteMethod())
//					.map(m -> m.getAnnotation(GeneratedValue.class))
//					.orElse(null);
//		}
		
		return new PropertyAccessMapping(
				clazz,
				columnName,
				alias,
				propertyDescriptor,
				jdbcType,
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
}
