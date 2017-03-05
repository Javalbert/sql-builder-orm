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
import java.util.Date;

import com.github.javalbert.reflection.ClassAccessFactory;
import com.github.javalbert.reflection.FieldAccess;
import com.github.javalbert.utils.string.Strings;

public class FieldAccessMapping<T> extends FieldColumnMapping<T> {
	private static String initMapKeyName(String column, String alias, Field field) {
		MapKey mapKey = field.getAnnotation(MapKey.class);
		String mapKeyName = mapKey != null ? mapKey.value() : null;
		
		return !Strings.isNullOrEmpty(mapKeyName) ? mapKeyName 
				: !Strings.isNullOrEmpty(alias) ? alias 
				: !Strings.isNullOrEmpty(column) ? column 
				: field.getName();
	}
	
	private final FieldAccess<T> fieldAccess;
	private final int fieldIndex;

	public FieldAccessMapping(
			Class<T> clazz,
			String column, 
			String alias, 
			Field field, 
			int jdbcType, 
			boolean primaryKey, 
			GeneratedValue generatedValue, 
			boolean version) {
		super(column, alias, jdbcType, initMapKeyName(column, alias, field), primaryKey, generatedValue, version);
		fieldAccess = ClassAccessFactory.get(clazz);
		fieldIndex = fieldAccess.fieldIndex(field.getName());
	}

	@Override
	public Object get(T instance) {
		return fieldAccess.getField(instance, fieldIndex);
	}

	@Override
	public void set(T instance, Object x) {
		fieldAccess.setField(instance, fieldIndex, x);
	}

	@Override
	public void setBoolean(T instance, boolean x) {
		fieldAccess.setBooleanField(instance, fieldIndex, x);
	}

	@Override
	public void setDouble(T instance, double x) {
		fieldAccess.setDoubleField(instance, fieldIndex, x);
	}

	@Override
	public void setFloat(T instance, float x) {
		fieldAccess.setFloatField(instance, fieldIndex, x);
	}

	@Override
	public void setInt(T instance, int x) {
		fieldAccess.setIntField(instance, fieldIndex, x);
	}

	@Override
	public void setLong(T instance, long x) {
		fieldAccess.setLongField(instance, fieldIndex, x);
	}

	@Override
	public void setBoxedBoolean(T instance, Boolean x) {
		fieldAccess.setBoxedBooleanField(instance, fieldIndex, x);
	}

	@Override
	public void setBoxedDouble(T instance, Double x) {
		fieldAccess.setBoxedDoubleField(instance, fieldIndex, x);
	}

	@Override
	public void setBoxedFloat(T instance, Float x) {
		fieldAccess.setBoxedFloatField(instance, fieldIndex, x);
	}

	@Override
	public void setBoxedInt(T instance, Integer x) {
		fieldAccess.setBoxedIntField(instance, fieldIndex, x);
	}

	@Override
	public void setBoxedLong(T instance, Long x) {
		fieldAccess.setBoxedLongField(instance, fieldIndex, x);
	}

	@Override
	public void setBigDecimal(T instance, BigDecimal x) {
		fieldAccess.setBigDecimalField(instance, fieldIndex, x);
	}

	@Override
	public void setDate(T instance, Date x) {
		fieldAccess.setDateField(instance, fieldIndex, x);
	}

	@Override
	public void setString(T instance, String x) {
		fieldAccess.setStringField(instance, fieldIndex, x);
	}
}