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
package com.github.javalbert.utils.reflection;

import java.beans.PropertyDescriptor;
import java.math.BigDecimal;
import java.util.Date;

import com.github.javalbert.reflection.ClassAccessFactory;
import com.github.javalbert.reflection.PropertyAccess;

public class PropertyMemberAccess implements MemberAccess {
	@SuppressWarnings("rawtypes")
	private final PropertyAccess propertyAccess;
	private final int propertyIndex;
	
	public PropertyMemberAccess(Class<?> clazz, PropertyDescriptor propertyDescriptor) {
		propertyAccess = ClassAccessFactory.get(clazz);
		propertyIndex = propertyAccess.propertyIndex(propertyDescriptor.getName());
	}

	@SuppressWarnings("unchecked")
	@Override
	public Object get(Object instance) {
		return propertyAccess.getProperty(instance, propertyIndex);
	}

	@SuppressWarnings("unchecked")
	@Override
	public void set(Object instance, Object x) {
		propertyAccess.setProperty(instance, propertyIndex, x);
	}

	@SuppressWarnings("unchecked")
	@Override
	public void setBoolean(Object instance, boolean x) {
		propertyAccess.setBooleanProperty(instance, propertyIndex, x);
	}

	@SuppressWarnings("unchecked")
	@Override
	public void setDouble(Object instance, double x) {
		propertyAccess.setDoubleProperty(instance, propertyIndex, x);
	}

	@SuppressWarnings("unchecked")
	@Override
	public void setFloat(Object instance, float x) {
		propertyAccess.setFloatProperty(instance, propertyIndex, x);
	}

	@SuppressWarnings("unchecked")
	@Override
	public void setInt(Object instance, int x) {
		propertyAccess.setIntProperty(instance, propertyIndex, x);
	}

	@SuppressWarnings("unchecked")
	@Override
	public void setLong(Object instance, long x) {
		propertyAccess.setLongProperty(instance, propertyIndex, x);
	}

	@SuppressWarnings("unchecked")
	@Override
	public void setBoxedBoolean(Object instance, Boolean x) {
		propertyAccess.setBoxedBooleanProperty(instance, propertyIndex, x);
	}

	@SuppressWarnings("unchecked")
	@Override
	public void setBoxedDouble(Object instance, Double x) {
		propertyAccess.setBoxedDoubleProperty(instance, propertyIndex, x);
	}

	@SuppressWarnings("unchecked")
	@Override
	public void setBoxedFloat(Object instance, Float x) {
		propertyAccess.setBoxedFloatProperty(instance, propertyIndex, x);
	}

	@SuppressWarnings("unchecked")
	@Override
	public void setBoxedInt(Object instance, Integer x) {
		propertyAccess.setBoxedIntProperty(instance, propertyIndex, x);
	}

	@SuppressWarnings("unchecked")
	@Override
	public void setBoxedLong(Object instance, Long x) {
		propertyAccess.setBoxedLongProperty(instance, propertyIndex, x);
	}

	@SuppressWarnings("unchecked")
	@Override
	public void setBigDecimal(Object instance, BigDecimal x) {
		propertyAccess.setBigDecimalProperty(instance, propertyIndex, x);
	}

	@SuppressWarnings("unchecked")
	@Override
	public void setDate(Object instance, Date x) {
		propertyAccess.setDateProperty(instance, propertyIndex, x);
	}

	@SuppressWarnings("unchecked")
	@Override
	public void setString(Object instance, String x) {
		propertyAccess.setStringProperty(instance, propertyIndex, x);
	}
}