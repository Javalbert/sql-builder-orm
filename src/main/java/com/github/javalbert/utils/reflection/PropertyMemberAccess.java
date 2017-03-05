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

public class PropertyMemberAccess<T> implements MemberAccess<T> {
	private final PropertyAccess<T> propertyAccess;
	private final int propertyIndex;
	
	public PropertyMemberAccess(Class<T> clazz, PropertyDescriptor propertyDescriptor) {
		propertyAccess = ClassAccessFactory.get(clazz);
		propertyIndex = propertyAccess.propertyIndex(propertyDescriptor.getName());
	}
	
	@Override
	public Object get(T instance) {
		return propertyAccess.getProperty(instance, propertyIndex);
	}
	
	@Override
	public void set(T instance, Object x) {
		propertyAccess.setProperty(instance, propertyIndex, x);
	}

	@Override
	public void setBoolean(T instance, boolean x) {
		propertyAccess.setBooleanProperty(instance, propertyIndex, x);
	}

	@Override
	public void setDouble(T instance, double x) {
		propertyAccess.setDoubleProperty(instance, propertyIndex, x);
	}

	@Override
	public void setFloat(T instance, float x) {
		propertyAccess.setFloatProperty(instance, propertyIndex, x);
	}

	@Override
	public void setInt(T instance, int x) {
		propertyAccess.setIntProperty(instance, propertyIndex, x);
	}

	@Override
	public void setLong(T instance, long x) {
		propertyAccess.setLongProperty(instance, propertyIndex, x);
	}

	@Override
	public void setBoxedBoolean(T instance, Boolean x) {
		propertyAccess.setBoxedBooleanProperty(instance, propertyIndex, x);
	}

	@Override
	public void setBoxedDouble(T instance, Double x) {
		propertyAccess.setBoxedDoubleProperty(instance, propertyIndex, x);
	}

	@Override
	public void setBoxedFloat(T instance, Float x) {
		propertyAccess.setBoxedFloatProperty(instance, propertyIndex, x);
	}

	@Override
	public void setBoxedInt(T instance, Integer x) {
		propertyAccess.setBoxedIntProperty(instance, propertyIndex, x);
	}

	@Override
	public void setBoxedLong(T instance, Long x) {
		propertyAccess.setBoxedLongProperty(instance, propertyIndex, x);
	}

	@Override
	public void setBigDecimal(T instance, BigDecimal x) {
		propertyAccess.setBigDecimalProperty(instance, propertyIndex, x);
	}

	@Override
	public void setDate(T instance, Date x) {
		propertyAccess.setDateProperty(instance, propertyIndex, x);
	}

	@Override
	public void setString(T instance, String x) {
		propertyAccess.setStringProperty(instance, propertyIndex, x);
	}
}