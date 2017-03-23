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

import java.lang.reflect.Field;
import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.Date;

import com.github.javalbert.reflection.ClassAccessFactory;
import com.github.javalbert.reflection.FieldAccess;
import com.github.javalbert.utils.string.Strings;

public class FieldAccessMapping extends FieldColumnMapping {
	private static String initMapKeyName(String column, String alias, Field field) {
		MapKey mapKey = field.getAnnotation(MapKey.class);
		String mapKeyName = mapKey != null ? mapKey.value() : null;
		
		return !Strings.isNullOrEmpty(mapKeyName) ? mapKeyName 
				: !Strings.isNullOrEmpty(alias) ? alias 
				: !Strings.isNullOrEmpty(column) ? column 
				: field.getName();
	}
	
	@SuppressWarnings("rawtypes")
	private final FieldAccess fieldAccess;
	private final int fieldIndex;

	public FieldAccessMapping(
			Class<?> clazz,
			String column,
			String alias, 
			Field field, 
			int jdbcType, 
			boolean primaryKey, 
			boolean autoIncrementId, 
			boolean version) {
		super(column, alias, jdbcType, initMapKeyName(column, alias, field), primaryKey, autoIncrementId, version);
		fieldAccess = ClassAccessFactory.get(clazz);
		fieldIndex = fieldAccess.fieldIndex(field.getName());
	}

	@SuppressWarnings("unchecked")
	@Override
	public Object get(Object instance) {
		return fieldAccess.getField(instance, fieldIndex);
	}

	@SuppressWarnings("unchecked")
	@Override
	public void set(Object instance, Object x) {
		fieldAccess.setField(instance, fieldIndex, x);
	}

	@SuppressWarnings("unchecked")
	@Override
	public void setBoolean(Object instance, boolean x) {
		fieldAccess.setBooleanField(instance, fieldIndex, x);
	}

	@SuppressWarnings("unchecked")
	@Override
	public void setDouble(Object instance, double x) {
		fieldAccess.setDoubleField(instance, fieldIndex, x);
	}

	@SuppressWarnings("unchecked")
	@Override
	public void setFloat(Object instance, float x) {
		fieldAccess.setFloatField(instance, fieldIndex, x);
	}

	@SuppressWarnings("unchecked")
	@Override
	public void setInt(Object instance, int x) {
		fieldAccess.setIntField(instance, fieldIndex, x);
	}

	@SuppressWarnings("unchecked")
	@Override
	public void setLong(Object instance, long x) {
		fieldAccess.setLongField(instance, fieldIndex, x);
	}

	@SuppressWarnings("unchecked")
	@Override
	public void setBoxedBoolean(Object instance, Boolean x) {
		fieldAccess.setBoxedBooleanField(instance, fieldIndex, x);
	}

	@SuppressWarnings("unchecked")
	@Override
	public void setBoxedDouble(Object instance, Double x) {
		fieldAccess.setBoxedDoubleField(instance, fieldIndex, x);
	}

	@SuppressWarnings("unchecked")
	@Override
	public void setBoxedFloat(Object instance, Float x) {
		fieldAccess.setBoxedFloatField(instance, fieldIndex, x);
	}

	@SuppressWarnings("unchecked")
	@Override
	public void setBoxedInt(Object instance, Integer x) {
		fieldAccess.setBoxedIntField(instance, fieldIndex, x);
	}

	@SuppressWarnings("unchecked")
	@Override
	public void setBoxedLong(Object instance, Long x) {
		fieldAccess.setBoxedLongField(instance, fieldIndex, x);
	}

	@SuppressWarnings("unchecked")
	@Override
	public void setBigDecimal(Object instance, BigDecimal x) {
		fieldAccess.setBigDecimalField(instance, fieldIndex, x);
	}

	@SuppressWarnings("unchecked")
	@Override
	public void setDate(Object instance, Date x) {
		fieldAccess.setDateField(instance, fieldIndex, x);
	}
	
	@SuppressWarnings("unchecked")
	@Override
	public void setLocalDate(Object instance, LocalDate x) {
		fieldAccess.setLocalDateField(instance, fieldIndex, x);
	}
	
	@SuppressWarnings("unchecked")
	@Override
	public void setLocalDateTime(Object instance, LocalDateTime x) {
		fieldAccess.setLocalDateTimeField(instance, fieldIndex, x);
	}

	@SuppressWarnings("unchecked")
	@Override
	public void setString(Object instance, String x) {
		fieldAccess.setStringField(instance, fieldIndex, x);
	}
}